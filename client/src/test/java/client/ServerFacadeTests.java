package client;

import model.AuthData;
import model.GameData;
import org.junit.jupiter.api.*;
import server.Server;

import java.util.Collection;

import static org.junit.jupiter.api.Assertions.*;

public class ServerFacadeTests {

    private static Server server;
    private static ServerFacade facade;

    @BeforeAll
    public static void init() throws Exception {
        server = new Server();
        int port = server.run(0);
        facade = new ServerFacade(port);
        facade.clear();
    }

    @BeforeEach
    public void setup() throws Exception {
        facade.clear();
    }

    @AfterAll
    public static void stopServer() {
        server.stop();
    }

    @Test
    public void clearPositive() {
        assertDoesNotThrow(() -> facade.clear());
    }

    @Test
    public void clearNegative() throws Exception {
        facade.register("bob", "pass", "bob@email.com");
        assertDoesNotThrow(() -> facade.clear());
    }

    @Test
    public void registerPositive() throws Exception {
        AuthData auth = facade.register("bob", "pass", "bob@email.com");
        assertNotNull(auth);
        assertNotNull(auth.authToken());
        assertEquals("bob", auth.username());
    }

    @Test
    public void registerNegative() throws Exception {
        facade.register("bob", "pass", "bob@email.com");
        assertThrows(ResponseException.class, () ->
                facade.register("bob", "pass", "bob@email.com"));
    }

    @Test
    public void loginPositive() throws Exception {
        facade.register("bob", "pass", "bob@email.com");
        AuthData auth = facade.login("bob", "pass");
        assertNotNull(auth);
        assertEquals("bob", auth.username());
    }

    @Test
    public void loginNegative() {
        assertThrows(ResponseException.class, () ->
                facade.login("bob", "wrongpass"));
    }

    @Test
    public void logoutPositive() throws Exception {
        AuthData auth = facade.register("bob", "pass", "bob@email.com");
        assertDoesNotThrow(() -> facade.logout(auth.authToken()));
    }

    @Test
    public void logoutNegative() {
        assertThrows(ResponseException.class, () ->
                facade.logout("bad-token"));
    }

    @Test
    public void createGamePositive() throws Exception {
        AuthData auth = facade.register("bob", "pass", "bob@email.com");
        int gameID = facade.createGame("Test Game", auth.authToken());
        assertTrue(gameID > 0);
    }

    @Test
    public void createGameNegative() {
        assertThrows(ResponseException.class, () ->
                facade.createGame("Test Game", "bad-token"));
    }

    @Test
    public void listGamesPositive() throws Exception {
        AuthData auth = facade.register("bob", "pass", "bob@email.com");
        facade.createGame("Test Game", auth.authToken());

        Collection<GameData> games = facade.listGames(auth.authToken());

        assertNotNull(games);
        assertEquals(1, games.size());
    }

    @Test
    public void listGamesNegative() {
        assertThrows(ResponseException.class, () ->
                facade.listGames("bad-token"));
    }

    @Test
    public void joinGamePositive() throws Exception {
        AuthData auth = facade.register("bob", "pass", "bob@email.com");
        int gameID = facade.createGame("Test Game", auth.authToken());

        assertDoesNotThrow(() ->
                facade.joinGame("WHITE", gameID, auth.authToken()));
    }

    @Test
    public void joinGameNegative() throws Exception {
        AuthData auth = facade.register("bob", "pass", "bob@email.com");
        int gameID = facade.createGame("Test Game", auth.authToken());

        assertThrows(ResponseException.class, () ->
                facade.joinGame("WHITE", gameID, "bad-token"));
    }
}