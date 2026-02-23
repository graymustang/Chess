package service;

import dataaccess.*;

public class TestDAOs {

    private static MemoryDatabase mem = new MemoryDatabase();
    private static UserDAO userDAO = new MemoryUserDAO(mem);
    private static AuthDAO authDAO = new MemoryAuthDAO(mem);
    private static GameDAO gameDAO = new MemoryGameDAO(mem);

    public static UserService createUserService() {
        return new UserService(userDAO, authDAO);
    }

    public static GameService createGameService() {
        return new GameService(gameDAO, authDAO);
    }

    public static ClearService createClearService() {
        return new ClearService(userDAO, authDAO, gameDAO);
    }

    //reset the tests
    public static void reset() {
        mem = new MemoryDatabase();
        userDAO = new MemoryUserDAO(mem);
        authDAO = new MemoryAuthDAO(mem);
        gameDAO = new MemoryGameDAO(mem);
    }
}