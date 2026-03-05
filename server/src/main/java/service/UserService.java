package service;

import dataaccess.AuthDAO;
import dataaccess.UserDAO;
import model.AuthData;
import model.UserData;
import org.mindrot.jbcrypt.BCrypt;

import java.util.UUID;

public class UserService {
    private final UserDAO users;
    private final AuthDAO auths;

    public UserService(UserDAO users, AuthDAO auths) {
        this.users = users;
        this.auths = auths;
    }

    public AuthData register(UserData req) throws Exception {
        if (req.username() == null || req.password() == null || req.email() == null) {
            throw new ServiceException(400, "Error: bad request");
        }
        if (users.getUser(req.username()) != null) {
            throw new ServiceException(403, "Error: already taken");
        }

        // bcrypt hash the password before storing
        String hashed = BCrypt.hashpw(req.password(), BCrypt.gensalt());
        users.createUser(new UserData(req.username(), hashed, req.email()));

        AuthData auth = new AuthData(UUID.randomUUID().toString(), req.username());
        auths.createAuth(auth);
        return auth;
    }

    public AuthData login(UserData req) throws Exception {
        if (req.username() == null || req.password() == null) {
            throw new ServiceException(400, "Error: bad request");
        }

        UserData existing = users.getUser(req.username());
        if (existing == null || existing.password() == null || !BCrypt.checkpw(req.password(), existing.password())) {
            throw new ServiceException(401, "Error: unauthorized");
        }

        AuthData auth = new AuthData(UUID.randomUUID().toString(), req.username());
        auths.createAuth(auth);
        return auth;
    }

    public void logout(String authToken) throws Exception {
        if (authToken == null) {
            throw new ServiceException(401, "Error: unauthorized");
        }

        if (auths.getAuth(authToken) == null) {
            throw new ServiceException(401, "Error: unauthorized");
        }

        auths.deleteAuth(authToken);
    }
}