package server;

import io.javalin.websocket.WsContext;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ConnectionManager {

    private final Map<Integer, Map<String, WsContext>> connections = new ConcurrentHashMap<>();

    public void add(Integer gameID, String username, WsContext session) {
        connections.computeIfAbsent(gameID, id -> new ConcurrentHashMap<>()).put(username, session);
    }

    public void remove(Integer gameID, String username) {
        Map<String, WsContext> gameConnections = connections.get(gameID);
        if (gameConnections != null) {
            gameConnections.remove(username);
            if (gameConnections.isEmpty()) {
                connections.remove(gameID);
            }
        }
    }

    public void broadcast(Integer gameID, String message) {
        Map<String, WsContext> gameConnections = connections.get(gameID);
        if (gameConnections == null) {
            return;
        }

        for (WsContext session : gameConnections.values()) {
            session.send(message);
        }
    }

    public void broadcastExcept(Integer gameID, String excludedUsername, String message) {
        Map<String, WsContext> gameConnections = connections.get(gameID);
        if (gameConnections == null) {
            return;
        }

        for (var entry : gameConnections.entrySet()) {
            if (!entry.getKey().equals(excludedUsername)) {
                entry.getValue().send(message);
            }
        }
    }

//public void sendToOne(Integer gameID, String username, String message) {
//    Map<String, WsContext> gameConnections = connections.get(gameID);
//    if (gameConnections == null) {
//        return;
//    }

//    WsContext session = gameConnections.get(username);
//    if (session != null) {
//        session.send(message);
//    }
//}
}