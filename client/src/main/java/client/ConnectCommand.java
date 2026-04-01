package client;

public class ConnectCommand {
    public String commandType = "CONNECT";
    public String authToken;
    public Integer gameID;

    public ConnectCommand(String authToken, Integer gameID) {
        this.authToken = authToken;
        this.gameID = gameID;
    }
}