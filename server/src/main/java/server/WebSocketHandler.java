package server;

import chess.ChessGame;
import chess.ChessMove;
import chess.ChessPiece;
import chess.InvalidMoveException;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dataaccess.AuthDAO;
import dataaccess.GameDAO;
import io.javalin.websocket.WsContext;
import model.AuthData;
import model.GameData;
import websocket.commands.MakeMoveCommand;
import websocket.commands.UserGameCommand;
import websocket.messages.ErrorMessage;
import websocket.messages.LoadGameMessage;
import websocket.messages.NotificationMessage;

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

    }

    public void onMessage(WsContext ctx) {
        try {
            JsonObject json = JsonParser.parseString(ctx.message()).getAsJsonObject();
            String type = json.get("commandType").getAsString();

            switch (type) {
                case "CONNECT" -> handleConnect(ctx, JsonUtil.GSON.fromJson(json, UserGameCommand.class));
                case "MAKE_MOVE" -> handleMakeMove(ctx, JsonUtil.GSON.fromJson(json, MakeMoveCommand.class));
                case "LEAVE" -> handleLeave(ctx, JsonUtil.GSON.fromJson(json, UserGameCommand.class));
                case "RESIGN" -> handleResign(ctx, JsonUtil.GSON.fromJson(json, UserGameCommand.class));
                default -> sendError(ctx, "Error: unknown command");
            }
        } catch (Exception e) {
            sendError(ctx, "Error: " + e.getMessage());
        }
    }

    private void handleConnect(WsContext ctx, UserGameCommand cmd) throws Exception {
        AuthData auth = requireAuth(cmd.getAuthToken());
        GameData gameData = requireGame(cmd.getGameID());

        String username = auth.username();
        connections.add(cmd.getGameID(), username, ctx);

        ctx.attribute("username", username);
        ctx.attribute("gameID", cmd.getGameID());

        send(ctx, new LoadGameMessage(gameData));

        String role = "observer";
        if (username.equals(gameData.whiteUsername())) {
            role = "white";
        } else if (username.equals(gameData.blackUsername())) {
            role = "black";
        }

        String message = role.equals("observer")
                ? username + " connected as an observer"
                : username + " connected as " + role;

        connections.broadcastExcept(cmd.getGameID(), username, JsonUtil.GSON.toJson(new NotificationMessage(message)));
    }

    private void handleMakeMove(WsContext ctx, MakeMoveCommand cmd) throws Exception {
        AuthData auth = requireAuth(cmd.getAuthToken());
        GameData gameData = requireGame(cmd.getGameID());
        String username = auth.username();

        ChessGame game = gameData.game();
        if (isGameOver(game)) {
            throw new Exception("Error: game is over");
        }

        ChessMove move = cmd.getMove();
        if (move == null) {
            throw new Exception("Error: missing move");
        }

        ChessPiece piece = game.getBoard().getPiece(move.getStartPosition());
        if (piece == null) {
            throw new Exception("Error: no piece at start position");
        }

        ChessGame.TeamColor playerColor = getPlayerColor(username, gameData);
        if (playerColor == null) {
            throw new Exception("Error: observers cannot make moves");
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

        String loadJson = JsonUtil.GSON.toJson(new LoadGameMessage(updated));
        connections.broadcast(cmd.getGameID(), loadJson);

        String moveText = username + " moved from " +
                square(move.getStartPosition().getRow(), move.getStartPosition().getColumn()) +
                " to " +
                square(move.getEndPosition().getRow(), move.getEndPosition().getColumn());

        connections.broadcastExcept(cmd.getGameID(), username,
                JsonUtil.GSON.toJson(new NotificationMessage(moveText)));

        ChessGame.TeamColor sideToMove = game.getTeamTurn();
        String checkedPlayer = sideToMove == ChessGame.TeamColor.WHITE ? updated.whiteUsername() : updated.blackUsername();

        if (game.isInCheckmate(sideToMove)) {
            setGameOver(game, true);
            gameDAO.updateGame(updated);
            connections.broadcast(cmd.getGameID(),
                    JsonUtil.GSON.toJson(new NotificationMessage(checkedPlayer + " is in checkmate")));
        } else if (game.isInStalemate(sideToMove)) {
            setGameOver(game, true);
            gameDAO.updateGame(updated);
            connections.broadcast(cmd.getGameID(),
                    JsonUtil.GSON.toJson(new NotificationMessage("Stalemate")));
        } else if (game.isInCheck(sideToMove)) {
            connections.broadcast(cmd.getGameID(),
                    JsonUtil.GSON.toJson(new NotificationMessage(checkedPlayer + " is in check")));
        }
    }

    private void handleLeave(WsContext ctx, UserGameCommand cmd) throws Exception {
        AuthData auth = requireAuth(cmd.getAuthToken());
        GameData gameData = requireGame(cmd.getGameID());
        String username = auth.username();

        GameData updated = gameData;

        if (username.equals(gameData.whiteUsername())) {
            updated = new GameData(gameData.gameID(), null, gameData.blackUsername(), gameData.gameName(), gameData.game());
            gameDAO.updateGame(updated);
        } else if (username.equals(gameData.blackUsername())) {
            updated = new GameData(gameData.gameID(), gameData.whiteUsername(), null, gameData.gameName(), gameData.game());
            gameDAO.updateGame(updated);
        }

        connections.remove(cmd.getGameID(), username);

        connections.broadcast(cmd.getGameID(),
                JsonUtil.GSON.toJson(new NotificationMessage(username + " left the game")));
    }

    private void handleResign(WsContext ctx, UserGameCommand cmd) throws Exception {
        AuthData auth = requireAuth(cmd.getAuthToken());
        GameData gameData = requireGame(cmd.getGameID());
        String username = auth.username();

        ChessGame.TeamColor playerColor = getPlayerColor(username, gameData);
        if (playerColor == null) {
            throw new Exception("Error: observers cannot resign");
        }

        ChessGame game = gameData.game();
        if (isGameOver(game)) {
            throw new Exception("Error: game is already over");
        }

        setGameOver(game, true);
        gameDAO.updateGame(gameData);

        connections.broadcast(cmd.getGameID(),
                JsonUtil.GSON.toJson(new NotificationMessage(username + " resigned")));
    }

    private AuthData requireAuth(String authToken) throws Exception {
        AuthData auth = authDAO.getAuth(authToken);
        if (auth == null) {
            throw new Exception("Error: unauthorized");
        }
        return auth;
    }

    private GameData requireGame(Integer gameID) throws Exception {
        if (gameID == null) {
            throw new Exception("Error: bad request");
        }
        GameData game = gameDAO.getGame(gameID);
        if (game == null) {
            throw new Exception("Error: bad request");
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
    private void sendError(WsContext ctx, String error) {
        ctx.send(JsonUtil.GSON.toJson(new ErrorMessage(error)));
    }
    private String square(int row, int col) {
        char file = (char) ('a' + col - 1);
        return "" + file + row;
    }

    private boolean isGameOver(ChessGame game) {
        try {
            return (boolean) game.getClass().getMethod("isGameOver").invoke(game);
        } catch (Exception e) {
            return false;
        }
    }

    private void setGameOver(ChessGame game, boolean value) {
        try {
            game.getClass().getMethod("setGameOver", boolean.class).invoke(game, value);
        } catch (Exception ignored) {
        }
    }
}