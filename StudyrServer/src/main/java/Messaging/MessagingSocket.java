package Messaging;

import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;

@WebSocket
public class MessagingSocket {
    private final MessagingManager messagingService = MessagingManager.getMessaging();
    @OnWebSocketConnect
    public synchronized void onConnect(Session user) throws Exception {
        System.out.println("New connection");
        messagingService.beginSession(user);
    }

    @OnWebSocketClose
    public void onClose(Session user, int statusCode, String reason) {
        messagingService.closeSession( user );
    }

    @OnWebSocketMessage
    public void onMessage(Session user, String message) throws Exception {
        messagingService.sendMessage(user, message);
    }

}
