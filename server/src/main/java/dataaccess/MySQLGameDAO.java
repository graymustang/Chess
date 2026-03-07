package dataaccess;

import chess.ChessGame;
import model.GameData;
import server.JsonUtil;

import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;

public class MySqlGameDAO implements GameDAO {
    @Override
    public int createGame(GameData game) throws DataAccessException {
        if (game == null) throw new DataAccessException("game is null");

        String gameJson = JsonUtil.GSON.toJson(game.game());

        try (var conn = DatabaseManager.getConnection();
             var ps = conn.prepareStatement(
                     "INSERT INTO game (whiteUsername, blackUsername, gameName, gameState) VALUES (?, ?, ?, ?)",
                     Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, game.whiteUsername());
            ps.setString(2, game.blackUsername());
            ps.setString(3, game.gameName());
            ps.setString(4, gameJson);

            ps.executeUpdate();

            try (var rs = ps.getGeneratedKeys()) {
                if (!rs.next()) throw new DataAccessException("failed to get generated game id");
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            throw new DataAccessException("failed to create game", e);
        }
    }

    @Override
    public GameData getGame(int gameID) throws DataAccessException {
        try (var conn = DatabaseManager.getConnection();
             var ps = conn.prepareStatement(
                     "SELECT gameID, whiteUsername, blackUsername, gameName, gameState FROM game WHERE gameID=?")) {

            ps.setInt(1, gameID);
            try (var rs = ps.executeQuery()) {
                if (!rs.next()) return null;

                ChessGame gameObj = JsonUtil.GSON.fromJson(rs.getString("gameState"), ChessGame.class);
                return new GameData(
                        rs.getInt("gameID"),
                        rs.getString("whiteUsername"),
                        rs.getString("blackUsername"),
                        rs.getString("gameName"),
                        gameObj
                );
            }
        } catch (SQLException e) {
            throw new DataAccessException("failed to get game", e);
        }
    }

    @Override
    public Collection<GameData> listGames() throws DataAccessException {
        try (var conn = DatabaseManager.getConnection();
             var ps = conn.prepareStatement("SELECT gameID, whiteUsername, blackUsername, gameName, gameState FROM game");
             var rs = ps.executeQuery()) {

            Collection<GameData> games = new ArrayList<>();
            while (rs.next()) {
                ChessGame gameObj = JsonUtil.GSON.fromJson(rs.getString("gameState"), ChessGame.class);
                games.add(new GameData(
                        rs.getInt("gameID"),
                        rs.getString("whiteUsername"),
                        rs.getString("blackUsername"),
                        rs.getString("gameName"),
                        gameObj
                ));
            }
            return games;
        } catch (SQLException e) {
            throw new DataAccessException("failed to list games", e);
        }
    }

    @Override
    public void updateGame(GameData game) throws DataAccessException {
        if (game == null) throw new DataAccessException("game is null");

        String gameJson = JsonUtil.GSON.toJson(game.game());

        try (var conn = DatabaseManager.getConnection();
             var ps = conn.prepareStatement(
                     "UPDATE game SET whiteUsername=?, blackUsername=?, gameName=?, gameState=? WHERE gameID=?")) {

            ps.setString(1, game.whiteUsername());
            ps.setString(2, game.blackUsername());
            ps.setString(3, game.gameName());
            ps.setString(4, gameJson);
            ps.setInt(5, game.gameID());

            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("failed to update game", e);
        }
    }

    @Override
    public void clear() throws DataAccessException {
        try (var conn = DatabaseManager.getConnection();
             var ps = conn.prepareStatement("DELETE FROM game")) {
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("failed to clear games", e);
        }
    }
}