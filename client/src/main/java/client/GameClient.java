package client;

import chess.ChessGame;
import chess.ChessMove;
import chess.ChessPiece;
import chess.ChessPosition;
import com.google.gson.Gson;
import websocket.commands.MakeMoveCommand;
import websocket.commands.UserGameCommand;
import websocket.messages.ErrorMessage;
import websocket.messages.LoadGameMessage;
import websocket.messages.NotificationMessage;
import websocket.messages.ServerMessage;

import java.util.Collection;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;

public class GameClient {
    private final String serverUrl;
    private final String authToken;
    private final int gameID;
    private final boolean whitePerspective;
    private final boolean observer;

    private final Gson gson = new Gson();
    private WebSocketCommunicator ws;
    private ChessGame currentGame;
    private boolean running = true;

    public GameClient(String serverUrl, String authToken, int gameID, boolean whitePerspective, boolean observer) {
        this.serverUrl = serverUrl;
        this.authToken = authToken;
        this.gameID = gameID;
        this.whitePerspective = whitePerspective;
        this.observer = observer;
    }

    public void run() throws Exception {
        ws = new WebSocketCommunicator(serverUrl, this::handleMessage);

        var connectCommand = new UserGameCommand(UserGameCommand.CommandType.CONNECT, authToken, gameID);
        ws.send(gson.toJson(connectCommand));

        printOutHelp();

        Scanner scanner = new Scanner(System.in);

        while (running) {
            System.out.print("[game] > ");
            String input = scanner.nextLine().trim();

            if (input.isEmpty()) {
                continue;
            }

            try {
                executeCommand(input, scanner);
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        }
    }

    private void executeCommand(String input, Scanner scanner) throws Exception {
        String[] tokens = input.split("\\s+");
        String command = tokens[0].toLowerCase();

        switch (command) {
            case "help" -> printOutHelp();
            case "redraw" -> redraw();
            case "leave" -> leave();
            case "resign" -> resign(scanner);
            case "move" -> makeMove(tokens);
            case "highlight" -> highlight(tokens);
            default -> System.out.println("Unknown command");
        }
    }

    private void handleMessage(String message) {
        try {
            ServerMessage base = gson.fromJson(message, ServerMessage.class);

            switch (base.getServerMessageType()) {
                case LOAD_GAME -> {
                    LoadGameMessage loadMessage = gson.fromJson(message, LoadGameMessage.class);
                    currentGame = loadMessage.getGame().game();
                    BoardPrinter.printBoard(currentGame, whitePerspective);
                }
                case NOTIFICATION -> {
                    NotificationMessage notification = gson.fromJson(message, NotificationMessage.class);
                    System.out.println();
                    System.out.println(notification.getMessage());
                }
                case ERROR -> {
                    ErrorMessage error = gson.fromJson(message, ErrorMessage.class);
                    System.out.println();
                    System.out.println(error.getErrorMessage());
                }
            }
        } catch (Exception e) {
            System.out.println();
            System.out.println("Error reading server message: " + e.getMessage());
        }
    }

    private void redraw() {
        if (currentGame == null) {
            System.out.println("Game has not loaded yet.");
            return;
        }
        BoardPrinter.printBoard(currentGame, whitePerspective);
    }

    private void leave() throws Exception {
        var leaveCommand = new UserGameCommand(UserGameCommand.CommandType.LEAVE, authToken, gameID);
        ws.send(gson.toJson(leaveCommand));
        running = false;
        System.out.println("Leaving game...");
    }

    private void resign(Scanner scanner) throws Exception {
        if (observer) {
            System.out.println("Observers cannot resign.");
            return;
        }

        System.out.print("Are you sure you want to resign? (yes/no): ");
        String answer = scanner.nextLine().trim().toLowerCase();

        if (answer.equals("yes")) {
            var resignCommand = new UserGameCommand(UserGameCommand.CommandType.RESIGN, authToken, gameID);
            ws.send(gson.toJson(resignCommand));
        } else {
            System.out.println("Resign cancelled.");
        }
    }

    private void makeMove(String[] tokens) throws Exception {
        if (observer) {
            System.out.println("Observers cannot make moves.");
            return;
        }

        if (currentGame == null) {
            System.out.println("Game has not loaded yet.");
            return;
        }

        if (tokens.length < 3 || tokens.length > 4) {
            System.out.println("Usage: move <from> <to> [promotion]");
            return;
        }

        ChessPosition start = parsePosition(tokens[1]);
        ChessPosition end = parsePosition(tokens[2]);

        ChessPiece.PieceType promotion = null;
        if (tokens.length == 4) {
            promotion = parsePromotion(tokens[3]);
        }

        ChessMove move = new ChessMove(start, end, promotion);
        MakeMoveCommand command = new MakeMoveCommand(authToken, gameID, move);
        ws.send(gson.toJson(command));
    }

    private void highlight(String[] tokens) {
        if (currentGame == null) {
            System.out.println("Game has not loaded yet.");
            return;
        }

        if (tokens.length != 2) {
            System.out.println("Usage: highlight <square>");
            return;
        }

        ChessPosition position = parsePosition(tokens[1]);
        Collection<ChessMove> moves = currentGame.validMoves(position);

        if (moves == null) {
            System.out.println("No piece at that square.");
            return;
        }

        Set<ChessPosition> highlighted = new HashSet<>();
        highlighted.add(position);

        for (ChessMove move : moves) {
            highlighted.add(move.getEndPosition());
        }

        BoardPrinter.printBoardWithHighlights(currentGame, whitePerspective, highlighted);
    }

    private ChessPosition parsePosition(String text) {
        if (text.length() != 2) {
            throw new IllegalArgumentException("Invalid square: " + text);
        }

        text = text.toLowerCase();
        char file = text.charAt(0);
        char rank = text.charAt(1);

        if (file < 'a' || file > 'h' || rank < '1' || rank > '8') {
            throw new IllegalArgumentException("Invalid square: " + text);
        }

        int col = file - 'a' + 1;
        int row = rank - '0';

        return new ChessPosition(row, col);
    }

    private ChessPiece.PieceType parsePromotion(String text) {
        return switch (text.toLowerCase()) {
            case "queen", "q" -> ChessPiece.PieceType.QUEEN;
            case "rook", "r" -> ChessPiece.PieceType.ROOK;
            case "bishop", "b" -> ChessPiece.PieceType.BISHOP;
            case "knight", "n" -> ChessPiece.PieceType.KNIGHT;
            default -> throw new IllegalArgumentException("Invalid promotion piece");
        };
    }

    private void printOutHelp() {
        System.out.println("help");
        System.out.println("redraw");
        System.out.println("leave");
        if (!observer) {
            System.out.println("move <from> <to> [promotion]");
            System.out.println("resign");
        }
        System.out.println("highlight <square>");
    }
}