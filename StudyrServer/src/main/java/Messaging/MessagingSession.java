package Messaging;

import java.io.IOException;

/**
 * Created by jbdaley on 6/16/16.
 */
public interface MessagingSession {
    void giveMessage(String message) throws Exception;
    void close();
    String userid();
}
