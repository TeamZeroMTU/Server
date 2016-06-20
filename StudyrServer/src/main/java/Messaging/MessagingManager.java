package Messaging;

import org.eclipse.jetty.websocket.api.Session;

import java.util.HashMap;
import java.util.Map;

import JSON.Json;
import Redis.RedisPostingService;
import spark.ResponseTransformer;

/**
 * Created by jbdaley on 5/3/16.
 */
public class MessagingManager {
    private static ResponseTransformer toJson = obj -> Json.gson.toJson(obj);
    private final Map<Session, MessagingSession> sessionSessionImplMap = new HashMap<>();
    private final Map<String, Session> idSessionMap = new HashMap<>();
    private final RedisPostingService rps;
    private static MessagingManager service;

    private MessagingManager(RedisPostingService rps) {
        this.rps = rps;
    }

    public static void init(RedisPostingService rps) {
        service = new MessagingManager( rps );
    }

    protected static MessagingManager getMessaging() { return service; }

    protected void sendMessage(Session session, String message) throws Exception {
        MessagingSession msgSession = sessionSessionImplMap.get(session);
        msgSession.giveMessage(message);
        idSessionMap.put(msgSession.userid(), session);
    }

    protected Session getSession(String id) {
        return idSessionMap.get(id);
    }

    protected void closeSession(Session session) {
        MessagingSession msgSession = sessionSessionImplMap.remove(session);
        idSessionMap.remove(msgSession.userid());
        msgSession.close();
    }

    protected void beginSession(Session user) throws Exception {
        sessionSessionImplMap.put(user, new StandardMessagingSession(user, rps));
    }
}
