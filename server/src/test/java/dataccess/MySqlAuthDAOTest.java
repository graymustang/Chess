package dataaccess;

import model.AuthData;
import model.UserData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class MySqlAuthDAOTest {

    private MySqlAuthDAO authDAO;
    private MySqlUserDAO userDAO;

    @BeforeEach
    void setup() throws Exception {
        MySqlDatabase.init();
        authDAO = new MySqlAuthDAO();
        userDAO = new MySqlUserDAO();

        authDAO.clear();
        new MySqlGameDAO().clear();
        userDAO.clear();

        userDAO.createUser(new UserData("bob", "pw", "bob@email.com"));
    }

    @Test
    void createAuthPositive() throws Exception {
        AuthData auth = new AuthData("token123", "bob");

        authDAO.createAuth(auth);
        AuthData result = authDAO.getAuth("token123");

        assertNotNull(result);
        assertEquals("bob", result.username());
    }

    @Test
    void createAuthNegative() {
        assertThrows(DataAccessException.class, () -> authDAO.createAuth(null));
    }

    @Test
    void getAuthPositive() throws Exception {
        authDAO.createAuth(new AuthData("token123", "bob"));

        AuthData result = authDAO.getAuth("token123");

        assertNotNull(result);
        assertEquals("token123", result.authToken());
    }

    @Test
    void getAuthNegative() throws Exception {
        AuthData result = authDAO.getAuth("badToken");

        assertNull(result);
    }

    @Test
    void deleteAuthPositive() throws Exception {
        authDAO.createAuth(new AuthData("token123", "bob"));

        authDAO.deleteAuth("token123");

        assertNull(authDAO.getAuth("token123"));
    }

    @Test
    void deleteAuthNegative() {
        assertDoesNotThrow(() -> authDAO.deleteAuth("notReal"));
    }

    @Test
    void clearPositive() throws Exception {
        authDAO.createAuth(new AuthData("token123", "bob"));

        authDAO.clear();

        assertNull(authDAO.getAuth("token123"));
    }

    @Test
    void clearNegative() {
        assertDoesNotThrow(() -> authDAO.clear());
    }
}