package client;

import chess.*;

public class ClientMain {

    public static void main(String[] args) {
        var port = 8080;
        if (args.length > 0) {
            port = Integer.parseInt(args[0]);
        }

        var serverFacade = new ServerFacade(port);
        var repl = new Repl(serverFacade);
        repl.run();
    }
}
