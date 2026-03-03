package dataaccess;

import model.AuthData;
import java.sql.SQLException;

public class MySqlAuthDAO implements AuthDAO {

    @Override
    public void createAuth(AuthData auth) throws DataAccessException {
        if (auth == null) throw new DataAccessException("auth is null");

        try (var conn = DatabaseManager.getConnection();
             var ps = conn.prepareStatement("INSERT INTO auth (authToken, username) VALUES (?, ?)")) {
            ps.setString(1, auth.authToken());
            ps.setString(2, auth.username());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("failed to create auth", e);
        }
    }

    @Override
    public AuthData getAuth(String authToken) throws DataAccessException {
        if (authToken == null) return null;

        try (var conn = DatabaseManager.getConnection();
             var ps = conn.prepareStatement("SELECT authToken, username FROM auth WHERE authToken=?")) {
            ps.setString(1, authToken);
            try (var rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                return new AuthData(rs.getString("authToken"), rs.getString("username"));
            }
        } catch (SQLException e) {
            throw new DataAccessException("failed to get auth", e);
        }
    }

    @Override
    public void deleteAuth(String authToken) throws DataAccessException {
        if (authToken == null) return;

        try (var conn = DatabaseManager.getConnection();
             var ps = conn.prepareStatement("DELETE FROM auth WHERE authToken=?")) {
            ps.setString(1, authToken);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("failed to delete auth", e);
        }
    }

    @Override
    public void clear() throws DataAccessException {
        try (var conn = DatabaseManager.getConnection();
             var ps = conn.prepareStatement("TRUNCATE TABLE auth")) {
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("failed to clear auth", e);
        }
    }
}