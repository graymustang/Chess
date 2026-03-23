package client;

import chess.ChessGame;
import model.GameData;

import java.util.ArrayList;
import java.util.Scanner;
import java.util.List;

public class Repl {
    private final ServerFacade server;
    private String authToken = null;

    public Repl(ServerFacade server) {
        this.server = server;
    }

    public void run() {
        System.out.println("Welcome to Chess!");
        System.out.println("Type 'help' to see commands.");

        Scanner scanner = new Scanner(System.in);

        while (true) {
            System.out.print("> ");
            String input = scanner.nextLine();

            try {
                if (authToken == null) {
                    prelogin(input);
                } else {
                    postlogin(input);
                }
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        }
    }

    private void prelogin(String input) throws Exception {
        var tokens = input.trim().split("\\s+");

        switch (tokens[0]) {
            case "help" -> printPreloginHelp();
            case "register" -> {
                if (tokens.length != 4){
                    System.out.println("Correct Usage: register <username> <password> <email>");
                    return;
                }
                var auth = server.register(tokens[1], tokens[2], tokens[3]);
                authToken = auth.authToken();
                System.out.println("Registered and logged in.");
            }
            case "login" -> {
                if (tokens.length != 3){
                    System.out.println("Correct Usage: login <username> <password>");
                    return;
                }

                var auth = server.login(tokens[1], tokens[2]);
                authToken = auth.authToken();
                System.out.println("Logged in.");
            }
            case "quit" -> System.exit(0);

            default -> System.out.println("Unknown command, type help.");
        }
    }

    private void postlogin(String input) throws Exception {
        var tokens = input.trim().split("\\s+");

        switch (tokens[0]) {
            case "help" -> printPostloginHelp();
            case "logout" -> {
                if (tokens.length != 1){
                    System.out.println("Correct Usage: logout");
                    return;
                }
                server.logout(authToken);
                authToken = null;
                System.out.println("Logged out.");
            }
            case "create" -> {
                if (tokens.length < 2){
                    System.out.println("Correct Usage: create <game name>");
                    return;
                }
                String gameName = input.substring("create".length()).trim();
                if(gameName.isEmpty()){
                    System.out.println("Usage: create <game name>");
                    return;
                }
                var id = server.createGame(gameName, authToken);
                System.out.println("Game created.");
            }
            case "list" -> {
                if (tokens.length != 1){
                    System.out.println("Correct Usage: list");
                    return;
                }
                lastGameList = new ArrayList<>(server.listGames(authToken));
                int i = 1;
                for (GameData g : lastGameList) {
                    String white = (g.whiteUsername() == null) ? "-" : g.whiteUsername();
                    String black = (g.blackUsername() == null) ? "-" : g.blackUsername();
                    System.out.println(i + ". " + g.gameName() + " (white: " + white + ", black: " + black + ")");
                    i++;
                }
            }

            case "play" ->{

                if (tokens.length != 3){
                    System.out.println("Correct usage: play <game number> <white|black>");
                    return;
                }

                int index;
                try {
                    index = Integer.parseInt(tokens[1]) - 1;
                } catch (NumberFormatException e){
                    System.out.println("Game number must be an number");
                    return;
                }

                if (index < 0 || index >= lastGameList.size()){
                    System.out.println("Invalid game number.");
                    return;
                }

                String color = tokens[2].toUpperCase();
                if (!color.equals("WHITE") && !color.equals("BLACK")){
                    System.out.println("Color must be WHITE or BLACK");
                    return;
                }

                GameData  game = lastGameList.get(index);
                server.joinGame(color, game.gameID(), authToken);

                System.out.println("Joined game " + game.gameName());

                ChessGame chessGame = new ChessGame();
                boolean whitePerspective = color.equals("WHITE");
                BoardPrinter.printBoard(chessGame, whitePerspective);
            }

            case "observe" -> {

                if (tokens.length != 2){
                    System.out.println("Correct usage: observe <game number>");
                    return;
                }
                int index;
                try {
                    index = Integer.parseInt(tokens[1]) - 1;
                } catch (NumberFormatException e){
                    System.out.println("Game number must be an number");
                    return;
                }

                if (index < 0 || index >= lastGameList.size()){
                    System.out.println("Invalid game number.");
                    return;
                }
                GameData  game = lastGameList.get(index);

                System.out.println("Observing game " + game.gameName());

                ChessGame chessGame = new ChessGame();
                BoardPrinter.printBoard(chessGame, true);
            }

            case "quit" -> System.exit(0);

            default -> System.out.println("Unknown command, type help.");
        }
    }

    private void printPreloginHelp() {
        System.out.println("register <username> <password> <email>");
        System.out.println("login <username> <password>");
        System.out.println("help");
        System.out.println("quit");
    }

    private void printPostloginHelp() {
        System.out.println("create <game name>");
        System.out.println("observe");
        System.out.println("list");
        System.out.println("play <game number> <white|black>");
        System.out.println("logout");
        System.out.println("help");
        System.out.println("quit");
    }
    private List<GameData> lastGameList = new ArrayList<>();
}