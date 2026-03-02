package dataaccess;

import java.sql.SQLException;

public class MySqlDatabase {
    public static void init() throws DataAccessException{
        DatabaseManager.createDatabase();

        try(var conn = DatabaseManager.getConnection()){
            for (String stmt : CREATE_TABLE_STATEMENTS){
                try(var ps = conn.prepareStatement(stmt)){
                    ps.executeUpdate();
                }
            }
        } catch (SQLException e){
            throw new DataAccessException("failed to initialize tables", e);
        }
    }

    private static final String[] CREATE_TABLE_STATEMENTS = {
            """
            CREATE TABLE IF NOT EXISTS user (
                username VARCHAR(256) NOT NULL,
                password VARCHAR(60) NOT NULL,
                email VARCHAR(256) NOT NULL,
                PRIMARY KEY (username)
            )
            """,
            """
            CREATE TABLE IF NOT EXISTS auth (
                authToken VARCHAR(256) NOT NULL,
                username VARCHAR(256) NOT NULL,
                PRIMARY KEY (authToken),
                FOREIGN KEY (username) REFERENCES user(username) ON DELETE CASCADE
            )
            """,
            """
            CREATE TABLE IF NOT EXISTS game (
                gameID INT NOT NULL AUTO_INCREMENT,
                whiteUsername VARCHAR(256) NULL,
                blackUsername VARCHAR(256) NULL,
                gameName VARCHAR(256) NOT NULL,
                gameState LONGTEXT NOT NULL,
                PRIMARY KEY (gameID)
            )
            """
    };
}
