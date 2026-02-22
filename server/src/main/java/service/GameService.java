package service;

import chess.ChessGame;
import dataaccess.AuthDAO;
import dataaccess.GameDAO;
import model.AuthData;
import model.GameData;

import java.util.Collection;

public class GameService{
    private final GameDAO games;
    private final AuthDAO auths;

    public GameService(GameDAO games, AuthDAO auths){
        this.games = games;
        this.auths = auths;
    }

    private AuthData requireAuth(String token) throws Exception{
        if (token == null) throw new ServiceException(401, "Error: unauthorized");
        AuthData auth = auths.getAuth(token);
        if (auth == null) throw new ServiceException(401, "Error: unauthorized");
        return auth;
    }

    public int createGame(String authToken, String gameName) throws Exception{
        requireAuth(authToken);
        if (gameName == null) throw new ServiceException(400, "Error, bad request");

        ChessGame game = new ChessGame();
        return games.createGame(new GameData(0, null, null, gameName, game));
    }

    public Collection<GameData> listGames(String authToken) throws Exception{
        requireAuth(authToken);
        return games.listGames();
    }

    public void joinGame(String authToken, String playerColor, Integer gameID) throws Exception{
        AuthData auth = requireAuth(authToken);
        if (gameID == null) throw new ServiceException(400, "Error: bad request");

        GameData g = games.getGame(gameID);
        if (g == null) throw new ServiceException(400, "Error: bad request");

        if (playerColor == null){
            return;
        }

        String color = playerColor.toUpperCase();
        if (!color.equals("WHITE") && !color.equals("BLACK")){
            throw new ServiceException(400, "Error: bad request");
        }

        if (color.equals("WHITE")){
            if (g.whiteUsername() != null) throw new ServiceException(403, "Error: already taken");
            games.updateGame(new GameData(g.gameID(), auth.username(), g.blackUsername(), g.gameName(), g.game()));
        } else {
            if (g.blackUsername() != null) throw new ServiceException(403, "Error: already taken");
            games.updateGame(new GameData(g.gameID(), g.whiteUsername(), auth.username(), g.gameName(), g.game()));

        }
    }
}