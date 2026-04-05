package server;

import io.javalin.Javalin;
import dataaccess.*;
import service.*;

public class Server {

    private final Javalin javalin;

    public Server() {
        javalin = Javalin.create(config -> config.staticFiles.add("web"));

        try {
            MySqlDatabase.init();
        } catch (DataAccessException e) {
            throw new RuntimeException(e);
        }
        UserDAO userDAO = new MySqlUserDAO();
        AuthDAO authDAO = new MySqlAuthDAO();
        GameDAO gameDAO = new MySqlGameDAO();

        UserService userService = new UserService(userDAO, authDAO);
        GameService gameService = new GameService(gameDAO, authDAO);
        ClearService clearService = new ClearService(userDAO, authDAO, gameDAO);

        UserHandler userHandler = new UserHandler(userService);
        GameHandler gameHandler = new GameHandler(gameService);
        ClearHandler clearHandler = new ClearHandler(clearService);

        //web socket handler endpoint
        WebSocketHandler webSocketHandler = new WebSocketHandler(authDAO, gameDAO);

        javalin.ws("/ws", ws -> {
            ws.onConnect(webSocketHandler::onConnect);
            ws.onMessage(webSocketHandler::onMessage);
            ws.onClose(webSocketHandler::onClose);
        });

        javalin.delete("/db", clearHandler::clear);

        javalin.post("/user", userHandler::register);

        javalin.post("/session", userHandler::login);
        javalin.delete("/session", userHandler::logout);

        javalin.get("/game", gameHandler::list);
        javalin.post("/game", gameHandler::create);
        javalin.put("/game", gameHandler::join);

        javalin.exception(ServiceException.class, (e, ctx) ->
                ctx.status(e.status()).result(JsonUtil.GSON.toJson(new ErrorResult(e.getMessage()))));

        javalin.exception(Exception.class, (e, ctx) ->
                ctx.status(500).result(JsonUtil.GSON.toJson(new ErrorResult("Error: " + e.getMessage()))));

    }

    public int run(int desiredPort) {
        javalin.start(desiredPort);
        return javalin.port();
    }

    public void stop() {
        javalin.stop();
    }
}