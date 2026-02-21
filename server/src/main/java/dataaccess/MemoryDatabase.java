package dataaccess;

import model.AuthData;
import model.GameData;
import model.UserData;
import java.util.HashMap;
import java.util.Map;

public class MemoryDatabase{
    final Map<String, UserData> users = new HashMap<>();
    final Map<String, AuthData> auths = new HashMap<>();
    final Map<Integer, GameData> games = new HashMap<>();
    int nextGameID = 1;

}

