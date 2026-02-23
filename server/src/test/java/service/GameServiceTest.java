package service;

import model.AuthData;
import model.GameData;
import model.UserData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collection;

import static org.junit.jupiter.api.Assertions.*;

public class GameServiceTest {

    private UserService userService;
    private GameService gameService;

    @BeforeEach
    void setUp() {
        userService = TestDAOs.createUserService();
        gameService = TestDAOs.createGameService();
    }

    private String registerAndGetToken() throws Exception {
        AuthData auth = userService.register(new UserData("bob", "pw", "bob@email.com"));
        return auth.authToken();
    }

    @Test
    void createGameSuccess() throws Exception {
        String token = registerAndGetToken();

        int id = gameService.createGame(token, "test game");

        assertTrue(id > 0);
    }

    @Test
    void createGameUnauthorized() {
        ServiceException ex = assertThrows(ServiceException.class,
                () -> gameService.createGame("bad-token", "test"));

        assertEquals(401, ex.status());
    }

    @Test
    void listGamesSuccess() throws Exception {
        String token = registerAndGetToken();
        gameService.createGame(token, "game 1");

        Collection<GameData> games = gameService.listGames(token);

        assertNotNull(games);
        assertFalse(games.isEmpty());
    }

    @Test
    void listGamesUnauthorized() {
        ServiceException ex = assertThrows(ServiceException.class,
                () -> gameService.listGames("bad-token"));

        assertEquals(401, ex.status());
    }

    @Test
    void joinGameBadColor() throws Exception {
        String token = registerAndGetToken();
        int gameID = gameService.createGame(token, "game");

        ServiceException ex = assertThrows(ServiceException.class,
                () -> gameService.joinGame(token, "PURPLE", gameID));

        assertEquals(400, ex.status());
    }

    @Test
    void joinGameUnauthorized() {
        ServiceException ex = assertThrows(ServiceException.class,
                () -> gameService.joinGame("bad-token", "WHITE", 1));

        assertEquals(401, ex.status());
    }
}