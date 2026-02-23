package service;

import dataaccess.*;
import model.AuthData;
import model.UserData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ClearServiceTest {

    private UserService userService;
    private GameService gameService;
    private ClearService clearService;

    @BeforeEach
    void setup() {
        MemoryDatabase mem = new MemoryDatabase();
        UserDAO userDAO = new MemoryUserDAO(mem);
        AuthDAO authDAO = new MemoryAuthDAO(mem);
        GameDAO gameDAO = new MemoryGameDAO(mem);

        userService = new UserService(userDAO, authDAO);
        gameService = new GameService(gameDAO, authDAO);
        clearService = new ClearService(userDAO, authDAO, gameDAO);
    }

    @Test
    void clearSuccessWipesData() throws Exception {
        AuthData auth = userService.register(new UserData("bob", "pass", "m@email.com"));
        String token = auth.authToken();

        gameService.createGame(token, "Game Before Clear");

        clearService.clear();

        assertThrows(ServiceException.class,
                () -> userService.login(new UserData("bob", "pass", null)));
    }

    @Test
    void clearOnEmptyDoesNotThrow() {
        assertDoesNotThrow(() -> clearService.clear());
    }
}