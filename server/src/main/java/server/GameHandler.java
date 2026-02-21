package server;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
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

    public void create(Context ctx) throws Exception {
        CreateGameRequest req = JsonUtil.GSON.fromJson(ctx.body(), CreateGameRequest.class);
        if (req == null || req.gameName == null) {
            ctx.status(400).result(JsonUtil.GSON.toJson(new ErrorResult("Error: bad request")));
            return;
        }
        int id = service.createGame(getAuthToken(ctx), req.gameName);
        ctx.status(200).result(JsonUtil.GSON.toJson(new CreateGameResult(id)));
    }

    public void join(Context ctx) throws Exception {
        JsonObject obj;

        try {
            obj = JsonParser.parseString(ctx.body()).getAsJsonObject();
        } catch (Exception e) {
            ctx.status(400).result(JsonUtil.GSON.toJson(new ErrorResult("Error: bad request")));
            return;
        }
        //game id needs to be a number
        if (!obj.has("gameID") || obj.get("gameID") == null || obj.get("gameID").isJsonNull()) {
            ctx.status(400).result(JsonUtil.GSON.toJson(new ErrorResult("Error: bad request")));
            return;
        }

        int gameID;
        try {
            gameID = obj.get("gameID").getAsInt();
        } catch (Exception e) {
            ctx.status(400).result(JsonUtil.GSON.toJson(new ErrorResult("Error: bad request")));
            return;
        }

        if (!obj.has("playerColor") || obj.get("playerColor") == null || obj.get("playerColor").isJsonNull()) {
            ctx.status(400).result(JsonUtil.GSON.toJson(new ErrorResult("Error: bad request")));
            return;
        }

        String colorStr;
        try {
            colorStr = obj.get("playerColor").getAsString();
        } catch (Exception e) {
            ctx.status(400).result(JsonUtil.GSON.toJson(new ErrorResult("Error: bad request")));
            return;
        }

        colorStr = colorStr.trim().toUpperCase();
        if (!colorStr.equals("WHITE") && !colorStr.equals("BLACK")) {
            ctx.status(400).result(JsonUtil.GSON.toJson(new ErrorResult("Error: bad request")));
            return;
        }

        service.joinGame(getAuthToken(ctx), colorStr, gameID);
        ctx.status(200).result("{}");
    }

    private String getAuthToken(Context ctx){
        String token = ctx.header("authorization");
        if (token == null) token = ctx.header("Authorization");
        return token;
    }

    public record CreateGameResult(int gameID){}
    public record ListGamesResult(Collection<GameData> games){}

    public static class CreateGameRequest {
        public String gameName;
    }

}