package server;

import io.javalin.http.Context;
import model.GameData;
import service.GameService;

import java.util.Collection;

public class GameHandler{
    private final GameService service;

    public GameHandler(GameService service){
        this.service = service;
    }

    public void list(Context ctx) throws Exception{
        Collection<GameData> games = service.listGames(getAuthToken(ctx));
        ctx.status(200).result(JsonUtil.GSON.toJson(new ListGamesResult(games)));
    }

    public void create(Context ctx) throws Exception{
        CreateGameRequest req = JsonUtil.GSON.fromJson(ctx.body(), CreateGameRequest.class);
        int id = service.createGame(getAuthToken(ctx), req.gameName());
        ctx.status(200).result(JsonUtil.GSON.toJson(new CreateGameResult(id)));
    }

    public void join(Context ctx) throws Exception{
        JoinGameRequest req = JsonUtil.GSON.fromJson(ctx.body(), JoinGameRequest.class);
        service.joinGame(getAuthToken(ctx), req.playerColor(), req.gameID());
        ctx.status(200).result("{}");
    }

    private String getAuthToken(Context ctx){
        String token = ctx.header("authorization");
        if (token == null) token = ctx.header("Authorization");
        return token;
    }

    public record CreateGameRequest(String gameName){}
    public record CreateGameResult(int gameID){}
    public record JoinGameRequest(String playerColor, Integer gameID){}
    public record ListGamesResult(Collection<GameData> games){}
}