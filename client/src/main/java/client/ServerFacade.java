package client;

import com.google.gson.Gson;
import model.AuthData;
import model.GameData;
import model.UserData;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.util.Collection;

public class ServerFacade {
    private final String serverUrl;
    private final Gson gson = new Gson();

    public ServerFacade(int port) {
        this.serverUrl = "http://localhost:" + port;
    }

    public AuthData register(String username, String password, String email) throws Exception {
        var request = new UserData(username, password, email);
        return makeRequest("POST", "/user", request, null, AuthData.class);
    }

    public AuthData login(String username, String password) throws Exception {
        var request = new UserData(username, password, null);
        return makeRequest("POST", "/session", request, null, AuthData.class);
    }

    public void logout(String authToken) throws Exception {
        makeRequest("DELETE", "/session", null, authToken, null);
    }

    public int createGame(String gameName, String authToken) throws Exception {
        var request = new CreateGameRequest(gameName);
        var response = makeRequest("POST", "/game", request, authToken, CreateGameResponse.class);
        return response.gameID();
    }

    public Collection<GameData> listGames(String authToken) throws Exception {
        var response = makeRequest("GET", "/game", null, authToken, ListGamesResponse.class);
        return response.games();
    }

    public void joinGame(String playerColor, int gameID, String authToken) throws Exception {
        var request = new JoinGameRequest(playerColor, gameID);
        makeRequest("PUT", "/game", request, authToken, null);
    }

    private <T> T makeRequest(String method, String path, Object request, String authToken, Class<T> responseClass)
            throws Exception {
        URI uri = new URI(serverUrl + path);
        HttpURLConnection http = (HttpURLConnection) uri.toURL().openConnection();
        http.setRequestMethod(method);
        http.setDoOutput(true);

        http.addRequestProperty("Content-Type", "application/json");
        http.addRequestProperty("Accept", "application/json");

        if (authToken != null) {
            http.addRequestProperty("Authorization", authToken);
        }

        if (request != null) {
            String reqData = gson.toJson(request);
            try (OutputStream body = http.getOutputStream()) {
                body.write(reqData.getBytes());
            }
        }

        http.connect();
        throwIfNotSuccessful(http);

        if (responseClass == null) {
            return null;
        }

        try (InputStream body = http.getInputStream()) {
            var reader = new java.io.InputStreamReader(body);
            return gson.fromJson(reader, responseClass);
        }
    }
}