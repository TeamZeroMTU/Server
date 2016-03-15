package Data;

import java.util.Date;

/**
 * Created by miles on 3/15/16.
 */
public class Message {

    private String text;
    private String id;
    private String sender;
    private String receiver;
    private Date time;

    public Message(String text, String id, String sender, String receiver, Date time) {
        this.text = text;
        this.id = id;
        this.sender = sender;
        this.receiver = receiver;
        this.time = time;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getReceiver() {
        return receiver;
    }

    public void setReceiver(String receiver) {
        this.receiver = receiver;
    }

    public Date getTime() {
        return time;
    }

    public void setTime(Date time) {
        this.time = time;
    }
}
