package java.service;

import dataaccess.AuthDAO;
import dataaccess.GameDAO;
import dataaccess.UserDAO;
import model.AuthData;
import model.GameData;
import model.UserData;

import java.util.*;

public class TestDAOs {

    public static class FakeUserDAO implements UserDAO {
        private final Map<String, UserData> users = new HashMap<>();

        @Override
        public UserData getUser(String username) {
            return users.get(username);
        }

        @Override
        public void createUser(UserData user) {
            users.put(user.username(), user);
        }

        @Override
        public void clear() {
            users.clear();
        }
    }

    public static class FakeAuthDAO implements AuthDAO {
        private final Map<String, AuthData> auths = new HashMap<>();

        @Override
        public AuthData getAuth(String token) {
            return auths.get(token);
        }

        @Override
        public void createAuth(AuthData auth) {
            auths.put(auth.authToken(), auth);
        }

        @Override
        public void deleteAuth(String token) {
            auths.remove(token);
        }

        @Override
        public void clear() {
            auths.clear();
        }
    }

    public static class FakeGameDAO implements GameDAO {
        private final Map<Integer, GameData> games = new HashMap<>();
        private int nextId = 1;

        @Override
        public int createGame(GameData game) {
            int id = nextId++;
            GameData stored = new GameData(id, game.whiteUsername(), game.blackUsername(), game.gameName(), game.game());
            games.put(id, stored);
            return id;
        }

        @Override
        public Collection<GameData> listGames() {
            return new ArrayList<>(games.values());
        }

        @Override
        public GameData getGame(int gameID) {
            return games.get(gameID);
        }

        @Override
        public void updateGame(GameData game) {
            games.put(game.gameID(), game);
        }

        @Override
        public void clear() {
            games.clear();
            nextId = 1;
        }
    }
}