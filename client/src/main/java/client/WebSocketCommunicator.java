package client;

import jakarta.websocket.*;
import java.net.URI;
import java.nio.ByteBuffer;
import java.util.Timer;
import java.util.TimerTask;
import java.util.function.Consumer;

@ClientEndpoint
public class WebSocketCommunicator {
    private Session session;
    private final Consumer<String> messageHandler;
    private Timer heartbeatTimer;

    public WebSocketCommunicator(String url, Consumer<String> messageHandler) throws Exception {
        this.messageHandler = messageHandler;
        WebSocketContainer container = ContainerProvider.getWebSocketContainer();
        container.setDefaultMaxSessionIdleTimeout(10 * 60 * 1000);
        URI uri = new URI(url.replace("http", "ws") + "/ws");
        container.connectToServer(this, uri);
    }

    public void send(String message) throws Exception {
        session.getBasicRemote().sendText(message);
    }

    public void close() throws Exception {
        stopHeartbeat();
        if (session != null && session.isOpen()) {
            session.close();
        }
    }

    @OnOpen
    public void onOpen(Session session) {
        this.session = session;
        session.setMaxIdleTimeout(10 * 60 * 1000);
        startHeartbeat();
        System.out.println("Connected to WebSocket.");
    }

    @OnMessage
    public void onMessage(String message) {
        messageHandler.accept(message);
    }

    //@OnClose
    //public void onClose(Session session, CloseReason reason) {
    //    stopHeartbeat();
    //    System.out.println("Disconnected. Reason: " + reason);
    //}

    @OnError
    public void onError(Session session, Throwable throwable) {
        System.out.println("WebSocket error: " + throwable.getMessage());
    }

    //this will keep a connection to the server.
    private void startHeartbeat() {
        heartbeatTimer = new Timer(true);
        heartbeatTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                try {
                    if (session != null && session.isOpen()) {
                        session.getAsyncRemote().sendPong(ByteBuffer.wrap(new byte[]{1}));
                    }
                } catch (Exception e) {
                    System.out.println("Heartbeat failed: " + e.getMessage());
                }
            }
        }, 10000, 10000);
    }

    private void stopHeartbeat() {
        if (heartbeatTimer != null) {
            heartbeatTimer.cancel();
            heartbeatTimer = null;
        }
    }
}