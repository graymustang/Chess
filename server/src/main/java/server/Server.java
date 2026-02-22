package server;

import io.javalin.Javalin;
import dataaccess.*;
import service.*;


public class Server {

    private final Javalin javalin;

    public Server() {
        javalin = Javalin.create(config -> config.staticFiles.add("web"));

        // Register your endpoints and exception handlers here.
        //in memory db and DAOs
        MemoryDatabase mem = new MemoryDatabase();
        UserDAO userDAO = new MemoryUserDAO(mem);
        AuthDAO authDAO = new MemoryAuthDAO(mem);
        GameDAO gameDAO = new MemoryGameDAO(mem);

        //services
        UserService userService = new UserService(userDAO, authDAO);
        GameService gameService = new GameService(gameDAO, authDAO);
        ClearService clearService = new ClearService(userDAO, authDAO, gameDAO);

        //handlers
        UserHandler userHandler = new UserHandler(userService);
        GameHandler gameHandler = new GameHandler(gameService);
        ClearHandler clearHandler = new ClearHandler(clearService);

        //endpoints
        javalin.delete("/db", clearHandler::clear);

        javalin.post("/user", userHandler::register);

        javalin.post("/session", userHandler::login);
        javalin.delete("/session", userHandler::logout);

        javalin.get("/game", gameHandler::list);
        javalin.post("/game", gameHandler::create);
        javalin.put("/game", gameHandler::join);

        //exception handling
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
