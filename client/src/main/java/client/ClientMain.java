package client;

import chess.*;

public class ClientMain {

    public static void main(String[] args) {
        // port 8080 was having some weird issues, so switched to 8000
        var port = 8000;
        if (args.length > 0) {
            port = Integer.parseInt(args[0]);
        }

        var serverFacade = new ServerFacade(port);
        var repl = new Repl(serverFacade);
        repl.run();
    }
}
