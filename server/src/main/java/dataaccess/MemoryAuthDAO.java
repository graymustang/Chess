package dataaccess;
import model.AuthData;

public class MemoryAuthDAO implements AuthDAO {
    private final MemoryDatabase db;

    public MemoryAuthDAO(MemoryDatabase db) {
        this.db = db;
    }

    @Override
    public void createAuth(AuthData auth) {
        db.auths.put(auth.authToken(), auth);
    }

    @Override
    public AuthData getAuth(String authToken) {
        return db.auths.get(authToken);
    }

    @Override
    public void deleteAuth(String authToken) {
        db.auths.remove(authToken);
    }

    @Override
    public void clear() {
        db.auths.clear();
    }
}
