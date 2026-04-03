package client;

import chess.ChessGame;
import com.google.gson.Gson;
import websocket.commands.UserGameCommand;

import java.util.Scanner;

public class GameClient {
    private final String serverUrl;
    private final String authToken;
    private final int gameID;
    private final boolean whitePerspective;
    private final Gson gson = new Gson();

    private WebSocketCommunicator ws;
    private ChessGame currentGame;

    public GameClient(String serverUrl, String authToken, int gameID, boolean whitePerspective) {
        this.serverUrl = serverUrl;
        this.authToken = authToken;
        this.gameID = gameID;
        this.whitePerspective = whitePerspective;
    }

    public void run() throws Exception {
        ws = new WebSocketCommunicator(serverUrl, this::handleMessage);

        var connect = new UserGameCommand(UserGameCommand.CommandType.CONNECT, authToken, gameID);
        ws.send(gson.toJson(connect));

        printHelp();

        Scanner scanner = new Scanner(System.in);
        while (true) {
            System.out.print("[game] > ");
            String input = scanner.nextLine().trim();

            if (input.equals("help")) {
                printHelp();
            } else if (input.equals("redraw")) {
                redraw();
            } else if (input.equals("leave")) {
                leave();
                return;
            } else if (input.equals("resign")) {
                resign(scanner);
            } else if (input.startsWith("move ")) {
                System.out.println("move command not added yet");
            } else if (input.startsWith("highlight ")) {
                System.out.println("highlight command not added yet");
            } else {
                System.out.println("Unknown command");
            }
        }
    }

    private void handleMessage(String message) {
        System.out.println();
        System.out.println("WS: " + message);
    }

    private void redraw() {
        if (currentGame != null) {
            BoardPrinter.printBoard(currentGame, whitePerspective);
        } else {
            System.out.println("Game not loaded yet.");
        }
    }

    private void leave() throws Exception {
        var command = new UserGameCommand(UserGameCommand.CommandType.LEAVE, authToken, gameID);
        ws.send(gson.toJson(command));
        System.out.println("Left game.");
    }

    private void resign(Scanner scanner) throws Exception {
        System.out.print("Are you sure you want to resign? (yes/no): ");
        String answer = scanner.nextLine().trim().toLowerCase();
        if (answer.equals("yes")) {
            var command = new UserGameCommand(UserGameCommand.CommandType.RESIGN, authToken, gameID);
            ws.send(gson.toJson(command));
        }
    }

    private void printHelp() {
        System.out.println("help");
        System.out.println("redraw");
        System.out.println("leave");
        System.out.println("resign");
        System.out.println("move <from> <to> [promotion]");
        System.out.println("highlight <square>");
    }
}