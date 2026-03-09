package dataaccess;

import chess.ChessGame;
import model.GameData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collection;

import static org.junit.jupiter.api.Assertions.*;

public class MySqlGameDAOTest {

    private MySqlGameDAO gameDAO;

    @BeforeEach
    void setup() throws Exception {
        MySqlDatabase.init();
        gameDAO = new MySqlGameDAO();

        new MySqlAuthDAO().clear();
        gameDAO.clear();
        new MySqlUserDAO().clear();
    }

    private GameData makeGame() {
        return new GameData(0, null, null, "Test Game", new ChessGame());
    }

    @Test
    void createGamePositive() throws Exception {
        int id = gameDAO.createGame(makeGame());

        assertTrue(id > 0);
    }

    @Test
    void createGameNegative() {
        assertThrows(DataAccessException.class, () -> gameDAO.createGame(null));
    }

    @Test
    void getGamePositive() throws Exception {
        int id = gameDAO.createGame(makeGame());

        GameData result = gameDAO.getGame(id);

        assertNotNull(result);
        assertEquals("Test Game", result.gameName());
    }

    @Test
    void getGameNegative() throws Exception {
        GameData result = gameDAO.getGame(999999);

        assertNull(result);
    }

    @Test
    void listGamesPositive() throws Exception {
        gameDAO.createGame(makeGame());

        Collection<GameData> games = gameDAO.listGames();

        assertEquals(1, games.size());
    }

    @Test
    void listGamesNegative() throws Exception {
        Collection<GameData> games = gameDAO.listGames();

        assertEquals(0, games.size());
    }

    @Test
    void updateGamePositive() throws Exception {
        int id = gameDAO.createGame(makeGame());
        GameData updated = new GameData(id, "whitePlayer", null, "Test Game", new ChessGame());

        gameDAO.updateGame(updated);
        GameData result = gameDAO.getGame(id);

        assertEquals("whitePlayer", result.whiteUsername());
    }

    @Test
    void updateGameNegative() {
        assertThrows(DataAccessException.class, () -> gameDAO.updateGame(null));
    }

    @Test
    void clearPositive() throws Exception {
        gameDAO.createGame(makeGame());

        gameDAO.clear();

        assertEquals(0, gameDAO.listGames().size());
    }

    @Test
    void clearNegative() {
        assertDoesNotThrow(() -> gameDAO.clear());
    }
}