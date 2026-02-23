package service;

import dataaccess.*;
import model.AuthData;
import model.UserData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class UserServiceTest {

    private UserService userService;
    private AuthDAO authDAO;

    @BeforeEach
    void setUp() {
        MemoryDatabase mem = new MemoryDatabase();
        UserDAO userDAO = new MemoryUserDAO(mem);
        authDAO = new MemoryAuthDAO(mem);
        userService = new UserService(userDAO, authDAO);
    }

    @Test
    void registerSuccess() throws Exception {
        UserData user = new UserData("bob", "pw", "bob@email.com");

        AuthData auth = userService.register(user);

        assertNotNull(auth);
        assertEquals("bob", auth.username());
        assertNotNull(auth.authToken());
        assertNotNull(authDAO.getAuth(auth.authToken()));
    }

    @Test
    void registerBadRequestMissingField() {
        UserData bad = new UserData("bob", "pw", null);

        ServiceException ex = assertThrows(ServiceException.class, () -> userService.register(bad));
        assertEquals(400, ex.status());
    }

    @Test
    void loginSuccess() throws Exception {
        userService.register(new UserData("bob", "pw", "bob@email.com"));

        AuthData auth = userService.login(new UserData("bob", "pw", null));

        assertNotNull(auth);
        assertEquals("bob", auth.username());
        assertNotNull(auth.authToken());
    }

    @Test
    void loginUnauthorizedWrongPassword() throws Exception {
        userService.register(new UserData("bob", "pw", "bob@email.com"));

        ServiceException ex = assertThrows(ServiceException.class,
                () -> userService.login(new UserData("bob", "wrong", null)));

        assertEquals(401, ex.status());
    }

    @Test
    void logoutSuccess() throws Exception {
        AuthData auth = userService.register(new UserData("bob", "pw", "bob@email.com"));

        userService.logout(auth.authToken());

        assertNull(authDAO.getAuth(auth.authToken()));
    }

    @Test
    void logoutUnauthorizedBadToken() {
        ServiceException ex = assertThrows(ServiceException.class, () -> userService.logout("not-a-real-token"));
        assertEquals(401, ex.status());
    }
}