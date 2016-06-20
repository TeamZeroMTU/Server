package Messaging;

import Data.Message;
import Facebook.TokenInfo;
import Facebook.TokenInterrogator;
import JSON.Json;
import Redis.RedisPostingService;
import org.eclipse.jetty.websocket.api.Session;
import spark.ResponseTransformer;

import java.util.ArrayList;

/**
 * Created by jbdaley on 6/16/16.
 */
public class StandardMessagingSession implements MessagingSession {
    private Session session;
    private RedisPostingService rps;
    private MessagingManager mgr = MessagingManager.getMessaging();

    private String SenderId = null;
    private String ReceiverId = null;
    private boolean authenicated = false;

    private static ResponseTransformer toJson = obj -> Json.gson.toJson(obj);
    public StandardMessagingSession(Session user, RedisPostingService rps){
        this.session = user;
        this.rps = rps;
    }
    @Override
    public void giveMessage(String message) throws Exception {
        if(SenderId == null) {
            SenderId = message;
        } else if(!authenicated) {
            TokenInterrogator interrogator = TokenInterrogator.getInstance();
            TokenInfo info = interrogator.getUserTokenInfo(message);
            if (interrogator.isValid(info)) {
                authenicated = true;
            } else {
                mgr.closeSession(session);
            }
        } else if(ReceiverId == null) {
            ReceiverId = message;
            ArrayList<Message> msgList = rps.getMessages(SenderId, ReceiverId);
            String jsonMsg = toJson.render(msgList);
            session.getRemote().sendString(jsonMsg);
        } else {
            Message msg = rps.createMessage(SenderId, ReceiverId, message);
            ArrayList<Message> msgList = new ArrayList<Message>();
            msgList.add(msg);
            String jsonMsg = toJson.render(msgList);
            session.getRemote().sendString(jsonMsg);

            Session receiverSession = mgr.getSession(SenderId);
            if(receiverSession != null && receiverSession != session) {
                receiverSession.getRemote().sendString(jsonMsg);
            }
        }
    }

    @Override
    public void close() {

    }

    @Override
    public String userid() {
        return SenderId;
    }
}
