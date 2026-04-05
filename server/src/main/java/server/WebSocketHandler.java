package server;

import chess.ChessGame;
import chess.ChessMove;
import chess.ChessPiece;
import chess.InvalidMoveException;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dataaccess.AuthDAO;
import dataaccess.DataAccessException;
import dataaccess.GameDAO;
import io.javalin.websocket.WsContext;
import model.AuthData;
import model.GameData;
import websocket.commands.MakeMoveCommand;
import websocket.commands.UserGameCommand;
import websocket.messages.ErrorMessage;
import websocket.messages.LoadGameMessage;
import websocket.messages.NotificationMessage;
import io.javalin.websocket.WsMessageContext;

public class WebSocketHandler {

    private final AuthDAO authDAO;
    private final GameDAO gameDAO;
    private final ConnectionManager connections = new ConnectionManager();

    public WebSocketHandler(AuthDAO authDAO, GameDAO gameDAO) {
        this.authDAO = authDAO;
        this.gameDAO = gameDAO;
    }

    public void onConnect(WsContext ctx) {
    }

    public void onClose(WsContext ctx) {
        String username = ctx.attribute("username");
        Integer gameID = ctx.attribute("gameID");

        if (username != null && gameID != null) {
            connections.remove(gameID, username);
        }
    }

    public void onMessage(WsMessageContext ctx) {
        try {
            JsonObject json = JsonParser.parseString(ctx.message()).getAsJsonObject();
            String commandType = json.get("commandType").getAsString();

            switch (commandType) {
                case "CONNECT" -> {
                    UserGameCommand command = JsonUtil.GSON.fromJson(json, UserGameCommand.class);
                    connect(ctx, command);
                }
                case "MAKE_MOVE" -> {
                    MakeMoveCommand command = JsonUtil.GSON.fromJson(json, MakeMoveCommand.class);
                    makeMove(ctx, command);
                }
                case "LEAVE" -> {
                    UserGameCommand command = JsonUtil.GSON.fromJson(json, UserGameCommand.class);
                    leave(ctx, command);
                }
                case "RESIGN" -> {
                    UserGameCommand command = JsonUtil.GSON.fromJson(json, UserGameCommand.class);
                    resign(ctx, command);
                }
                default -> sendError(ctx, "Error: unknown command");
            }
        } catch (Exception e) {
            sendError(ctx, "Error: " + e.getMessage());
        }
    }

    private void connect(WsContext ctx, UserGameCommand command) throws Exception {
        AuthData auth = requireAuth(command.getAuthToken());
        GameData gameData = requireGame(command.getGameID());

        String username = auth.username();
        connections.add(command.getGameID(), username, ctx);
        ctx.attribute("username", username);
        ctx.attribute("gameID", command.getGameID());

        send(ctx, new LoadGameMessage(gameData));

        String notification;
        if (username.equals(gameData.whiteUsername())) {
            notification = username + " connected as white";
        } else if (username.equals(gameData.blackUsername())) {
            notification = username + " connected as black";
        } else {
            notification = username + " connected as an observer";
        }

        connections.broadcastExcept(
                command.getGameID(),
                username,
                JsonUtil.GSON.toJson(new NotificationMessage(notification))
        );
    }

    private void makeMove(WsContext ctx, MakeMoveCommand command) throws Exception {
        AuthData auth = requireAuth(command.getAuthToken());
        GameData gameData = requireGame(command.getGameID());
        String username = auth.username();

        ChessGame game = gameData.game();

        if (game.isGameOver()) {
            throw new Exception("Error: game is already over");
        }

        ChessGame.TeamColor playerColor = getPlayerColor(username, gameData);
        if (playerColor == null) {
            throw new Exception("Error: observers cannot make moves");
        }

        ChessMove move = command.getMove();
        if (move == null) {
            throw new Exception("Error: missing move");
        }

        ChessPiece piece = game.getBoard().getPiece(move.getStartPosition());
        if (piece == null) {
            throw new Exception("Error: no piece at start square");
        }

        if (piece.getTeamColor() != playerColor) {
            throw new Exception("Error: cannot move opponent piece");
        }

        try {
            game.makeMove(move);
        } catch (InvalidMoveException e) {
            throw new Exception("Error: invalid move");
        }

        GameData updated = new GameData(
                gameData.gameID(),
                gameData.whiteUsername(),
                gameData.blackUsername(),
                gameData.gameName(),
                game
        );
        gameDAO.updateGame(updated);

        String loadGameJson = JsonUtil.GSON.toJson(new LoadGameMessage(updated));
        connections.broadcast(command.getGameID(), loadGameJson);

        String moveMessage = username + " made move " +
                square(move.getStartPosition().getRow(), move.getStartPosition().getColumn()) +
                " to " +
                square(move.getEndPosition().getRow(), move.getEndPosition().getColumn());

        connections.broadcastExcept(
                command.getGameID(),
                username,
                JsonUtil.GSON.toJson(new NotificationMessage(moveMessage))
        );

        ChessGame.TeamColor teamTurn = game.getTeamTurn();

        if (game.isInCheckmate(teamTurn)) {
            game.setGameOver(true);
            gameDAO.updateGame(updated);

            String checkedName = teamTurn == ChessGame.TeamColor.WHITE
                    ? updated.whiteUsername()
                    : updated.blackUsername();

            connections.broadcast(
                    command.getGameID(),
                    JsonUtil.GSON.toJson(new NotificationMessage(checkedName + " is in checkmate"))
            );
        } else if (game.isInStalemate(teamTurn)) {
            game.setGameOver(true);
            gameDAO.updateGame(updated);

            connections.broadcast(
                    command.getGameID(),
                    JsonUtil.GSON.toJson(new NotificationMessage("Stalemate"))
            );
        } else if (game.isInCheck(teamTurn)) {
            String checkedName = teamTurn == ChessGame.TeamColor.WHITE
                    ? updated.whiteUsername()
                    : updated.blackUsername();

            connections.broadcast(
                    command.getGameID(),
                    JsonUtil.GSON.toJson(new NotificationMessage(checkedName + " is in check"))
            );
        }
    }

    private void leave(WsContext ctx, UserGameCommand command) throws Exception {
        AuthData auth = requireAuth(command.getAuthToken());
        GameData gameData = requireGame(command.getGameID());
        String username = auth.username();

        if (username.equals(gameData.whiteUsername())) {
            gameData = new GameData(
                    gameData.gameID(),
                    null,
                    gameData.blackUsername(),
                    gameData.gameName(),
                    gameData.game()
            );
            gameDAO.updateGame(gameData);
        } else if (username.equals(gameData.blackUsername())) {
            gameData = new GameData(
                    gameData.gameID(),
                    gameData.whiteUsername(),
                    null,
                    gameData.gameName(),
                    gameData.game()
            );
            gameDAO.updateGame(gameData);
        }

        connections.remove(command.getGameID(), username);

        connections.broadcast(
                command.getGameID(),
                JsonUtil.GSON.toJson(new NotificationMessage(username + " left the game"))
        );
    }

    private void resign(WsContext ctx, UserGameCommand command) throws Exception {
        AuthData auth = requireAuth(command.getAuthToken());
        GameData gameData = requireGame(command.getGameID());
        String username = auth.username();

        ChessGame.TeamColor playerColor = getPlayerColor(username, gameData);
        if (playerColor == null) {
            throw new Exception("Error: observers cannot resign");
        }

        if (gameData.game().isGameOver()) {
            throw new Exception("Error: game is already over");
        }

        gameData.game().setGameOver(true);
        gameDAO.updateGame(gameData);

        connections.broadcast(
                command.getGameID(),
                JsonUtil.GSON.toJson(new NotificationMessage(username + " resigned"))
        );
    }

    private AuthData requireAuth(String authToken) throws DataAccessException {
        AuthData auth = authDAO.getAuth(authToken);
        if (auth == null) {
            throw new DataAccessException("Error: unauthorized");
        }
        return auth;
    }

    private GameData requireGame(Integer gameID) throws DataAccessException {
        GameData game = gameDAO.getGame(gameID);
        if (game == null) {
            throw new DataAccessException("Error: game not found");
        }
        return game;
    }

    private ChessGame.TeamColor getPlayerColor(String username, GameData gameData) {
        if (username.equals(gameData.whiteUsername())) {
            return ChessGame.TeamColor.WHITE;
        }
        if (username.equals(gameData.blackUsername())) {
            return ChessGame.TeamColor.BLACK;
        }
        return null;
    }
    private void send(WsContext ctx, Object message) {
        ctx.send(JsonUtil.GSON.toJson(message));
    }

    private void sendError(WsContext ctx, String errorMessage) {
        ctx.send(JsonUtil.GSON.toJson(new ErrorMessage(errorMessage)));
    }
    private String square(int row, int col) {
        char file = (char) ('a' + col - 1);
        return "" + file + row;
    }
}