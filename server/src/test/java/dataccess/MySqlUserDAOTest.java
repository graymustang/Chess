package dataaccess;

import model.UserData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class MySqlUserDAOTest {

    private MySqlUserDAO userDAO;

    @BeforeEach
    void setup() throws Exception {
        MySqlDatabase.init();
        userDAO = new MySqlUserDAO();

        new MySqlAuthDAO().clear();
        new MySqlGameDAO().clear();
        userDAO.clear();
    }

    @Test
    void createUserPositive() throws Exception {
        UserData user = new UserData("bob", "hashedPassword", "bob@email.com");

        userDAO.createUser(user);
        UserData result = userDAO.getUser("bob");

        assertNotNull(result);
        assertEquals("bob", result.username());
    }

    @Test
    void createUserNegative() {
        assertThrows(DataAccessException.class, () -> userDAO.createUser(null));
    }

    @Test
    void getUserPositive() throws Exception {
        UserData user = new UserData("alice", "pw", "alice@email.com");
        userDAO.createUser(user);

        UserData result = userDAO.getUser("alice");

        assertNotNull(result);
        assertEquals("alice", result.username());
    }

    @Test
    void getUserNegative() throws Exception {
        UserData result = userDAO.getUser("doesNotExist");

        assertNull(result);
    }

    @Test
    void clearPositive() throws Exception {
        userDAO.createUser(new UserData("sam", "pw", "sam@email.com"));

        userDAO.clear();

        assertNull(userDAO.getUser("sam"));
    }

    @Test
    void clearNegative() {
        assertDoesNotThrow(() -> userDAO.clear());
    }
}