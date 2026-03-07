package dataaccess;

import model.UserData;
import java.sql.SQLException;

public class MySqlUserDAO implements UserDAO {
    @Override
    public UserData getUser(String username) throws DataAccessException{
        if (username == null){
            return null;
        }

        try (var conn = DatabaseManager.getConnection();
            var ps = conn.prepareStatement("SELECT username, password, email FROM `user` WHERE username=?")) {

            ps.setString(1, username);
            try (var rs = ps.executeQuery()){
                if (!rs.next()) {
                    return null;
                }
                return new UserData(rs.getString("username"), rs.getString("password"), rs.getString("email"));
            }
        } catch (SQLException e){
            throw new DataAccessException("failed to get user", e);
        }
    }

    @Override
    public void createUser(UserData user) throws DataAccessException {
        if (user == null) {
            throw new DataAccessException("user is null");
        }

        try (var conn = DatabaseManager.getConnection();
             var ps = conn.prepareStatement("INSERT INTO `user` (username, password, email) VALUES (?, ?, ?)")) {
            ps.setString(1, user.username());
            ps.setString(2, user.password());
            ps.setString(3, user.email());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("failed to create user", e);
        }
    }

    @Override
    public void clear() throws DataAccessException {
        try (var conn = DatabaseManager.getConnection();
             var ps = conn.prepareStatement("DELETE FROM `user`")) {
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("failed to clear users", e);
        }
    }
}