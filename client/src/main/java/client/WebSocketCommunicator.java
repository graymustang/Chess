package client;

import jakarta.websocket.*;
import java.net.URI;
import java.util.function.Consumer;

@ClientEndpoint
public class WebSocketCommunicator {

    private Session session;
    private final Consumer<String> messageHandler;

    public WebSocketCommunicator(String url, Consumer<String> messageHandler) throws Exception {
        this.messageHandler = messageHandler;
        WebSocketContainer container = ContainerProvider.getWebSocketContainer();
        URI uri = new URI(url.replace("http", "ws") + "/ws");
        container.connectToServer(this, uri);
    }

    public void send(String message) throws Exception {
        session.getBasicRemote().sendText(message);
    }

    @OnOpen
    public void onOpen(Session session) {
        this.session = session;
        System.out.println("Connected to WebSocket.");
    }

    @OnMessage
    public void onMessage(String message) {
        messageHandler.accept(message);
    }

    @OnClose
    public void onClose(Session session, CloseReason reason) {
        System.out.println("Disconnected.");
    }

    @OnError
    public void onError(Session session, Throwable throwable) {
        throwable.printStackTrace();
    }
}