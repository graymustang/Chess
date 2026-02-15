package dataaccess;

import model.UserData;

public class MemoryUserDAO implements UserDAO {
    private final MemoryDatabase db;

    public MemoryUserDAO(MemoryDatabase db) { this.db = db; }

    @Override
    public UserData getUser(String username) {
        return db.users.get(username);
    }

    @Override
    public void createUser(UserData user) throws DataAccessException {
        if (db.users.containsKey(user.username())) {
            throw new DataAccessException("Error: already taken");
        }
        db.users.put(user.username(), user);
    }

    @Override
    public void clear() {
        db.users.clear();
    }
}
