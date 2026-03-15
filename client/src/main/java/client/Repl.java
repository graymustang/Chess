package client;

import model.GameData;

import java.util.ArrayList;
import java.util.Scanner;

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
        var tokens = input.split(" ");

        switch (tokens[0]) {
            case "help" -> printPreloginHelp();
            case "register" -> {
                var auth = server.register(tokens[1], tokens[2], tokens[3]);
                authToken = auth.authToken();
                System.out.println("Registered and logged in.");
            }
            case "login" -> {
                var auth = server.login(tokens[1], tokens[2]);
                authToken = auth.authToken();
                System.out.println("Logged in.");
            }
            case "quit" -> System.exit(0);

            default -> System.out.println("Unknown command, type help.");
        }
    }

    private void postlogin(String input) throws Exception {
        var tokens = input.split(" ");

        switch (tokens[0]) {
            case "help" -> printPostloginHelp();
            case "logout" -> {
                server.logout(authToken);
                authToken = null;
                System.out.println("Logged out.");
            }
            case "create" -> {
                var id = server.createGame(tokens[1], authToken);
                System.out.println("Game created with ID " + id);
            }
            case "list" -> {
                var games = server.listGames(authToken);
                lastGameList = new ArrayList<>(server.listGames(authToken));
                int i = 1;
                for (GameData g : lastGameList) {
                    System.out.println(g.gameID() + ": " + g.gameName());
                    i++;
                }
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
        System.out.println("list");
        System.out.println("logout");
        System.out.println("help");
        System.out.println("quit");
    }
    private List<GameData> lastGameList = new ArrayList<>();
}