package server;

import io.javalin.websocket.WsContext;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class ConnectionManager {

    private final Map<Integer, Map<String, WsContext>> gameConnections = new ConcurrentHashMap<>();

    public void add(int gameID, String username, WsContext session) {
        gameConnections
                .computeIfAbsent(gameID, id -> new ConcurrentHashMap<>())
                .put(username, session);
    }

    public void remove(int gameID, String username) {
        Map<String, WsContext> gameMap = gameConnections.get(gameID);
        if (gameMap != null) {
            gameMap.remove(username);
            if (gameMap.isEmpty()) {
                gameConnections.remove(gameID);
            }
        }
    }

    public void removeUserEverywhere(String username) {
        for (Map.Entry<Integer, Map<String, WsContext>> entry : gameConnections.entrySet()) {
            entry.getValue().remove(username);
            if (entry.getValue().isEmpty()) {
                gameConnections.remove(entry.getKey());
            }
        }
    }

    public void sendToUser(int gameID, String username, String message) {
        Map<String, WsContext> gameMap = gameConnections.get(gameID);
        if (gameMap == null) return;

        WsContext ws = gameMap.get(username);
        if (ws != null) {
            ws.send(message);
        }
    }

    public void broadcast(int gameID, String message) {
        Map<String, WsContext> gameMap = gameConnections.get(gameID);
        if (gameMap == null) return;

        for (WsContext ws : gameMap.values()) {
            ws.send(message);
        }
    }

    public void broadcastExcept(int gameID, String excludedUsername, String message) {
        Map<String, WsContext> gameMap = gameConnections.get(gameID);
        if (gameMap == null) return;

        for (Map.Entry<String, WsContext> entry : gameMap.entrySet()) {
            if (!entry.getKey().equals(excludedUsername)) {
                entry.getValue().send(message);
            }
        }
    }

    public Set<String> getConnectedUsers(int gameID) {
        Map<String, WsContext> gameMap = gameConnections.get(gameID);
        return gameMap == null ? Set.of() : gameMap.keySet();
    }
}