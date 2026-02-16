package service;

import dataaccess.AuthDAO;
import dataaccess.GameDAO;
import dataaccess.UserDAO;

public class ClearService{
    private final UserDAO users;
    private final AuthDAO auths;
    private final GameDAO games;

    public ClearService(UserDAO users, AuthDAO auths, GameDAO games){
        this.users = users;
        this.auths = auths;
        this.games = games;
    }

    public void clear() throws Exception{
        users.clear();
        auths.clear();
        games.clear();
    }
}