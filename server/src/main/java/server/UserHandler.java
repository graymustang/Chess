package server;

import io.javalin.http.Context;
import model.AuthData;
import model.UserData;
import service.UserService;

public class UserHandler{
    private final UserService service;

    public UserHandler(UserService service){
        this.service = service;
    }

    public void register(Context ctx) throws Exception{
        UserData req = JsonUtil.GSON.fromJson(ctx.body(), UserData.class);
        AuthData auth = service.register(req);
        ctx.status(200).result(JsonUtil.GSON.toJson(new RegisterResult(auth.username(), auth.authToken())));
    }

    public void login(Context ctx) throws Exception {
        UserData req = JsonUtil.GSON.fromJson(ctx.body(), UserData.class);
        AuthData auth = service.login(req);
        ctx.status(200).result(JsonUtil.GSON.toJson(new LoginResult(auth.username(), auth.authToken())));
    }

    public void logout(Context ctx) throws Exception{
        service.logout(getAuthToken(ctx));
        ctx.status(200).result("{}");
    }

    private String getAuthToken(Context ctx){
        String token = ctx.header("authorization");
        if (token == null) token = ctx.header("Authorization");
        return token;
    }

    public record RegisterResult(String username, String authToken){}
    public record LoginResult(String username, String authToken){}
}