package dataaccess;

import model.GameData;
import java.util.ArrayList;
import java.util.Collection;

public class MemoryGameDAO implements GameDAO{
    private final MemoryDatabase db;

    public MemoryGameDAO(MemoryDatabase db){
        this.db = db;
    }

    @Override
    public int createGame(GameData game){
        int id = db.nextGameID++;
        GameData stored = new GameData(id, null, null, game.gameName(), game.game());
        db.games.put(id, stored);
        return id;
    }

    @Override
    public GameData getGame(int gameID){
        return db.games.get(gameID);
    }

    @Override
    public Collection<GameData> listGames(){
        return new ArrayList<>(db.games.values());
    }

    @Override
    public void updateGame(GameData game) throws DataAccessException{
        if (!db.games.containsKey(game.gameID())){
            throw new DataAccessException("Error: bad request");
        }
        db.games.put(game.gameID(), game);
    }

    @Override
    public void clear(){
        db.games.clear();
        db.nextGameID = 1;
    }
}