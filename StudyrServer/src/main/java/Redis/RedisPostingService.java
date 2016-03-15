package Redis;

import Data.Course;
import Data.Message;
import Data.User;
import redis.clients.jedis.Jedis;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.UUID;

/**
 * Created by miles on 2/27/16.
 */
public class RedisPostingService {

    Jedis jedis;

    public RedisPostingService(Jedis jedis) {
        this.jedis = jedis;
    }

    public User createUser(String name, String school) {

        // Randomly generate an ID (This may be changed later if we are using Facebook IDs)
        String id = UUID.randomUUID().toString();

        // Map the name and school to the ID and add the ID to a list associated with the school
        jedis.hset("user:" + id, "name", name);
        jedis.hset("user:" + id, "school", school);
        jedis.sadd("schoolUsers:" + school, id);

        return new User(id, name, school);
    }

    public Course addCourseToUser(String id, String name) {

        // Add the name of the course to a list of courses for the id
        jedis.sadd("userCourses:" + id, name);
        // Add the user ID to a list of IDs associated with that course
        jedis.sadd("courseUsers:" + name, id);

        return new Course(name);
    }

    public Course removeCourseFromUser(String id, String name) {

        // Remove from the userCourses set
        jedis.srem("userCourses:" + id, name);
        // Remove from the courseUsers set
        jedis.srem("courseUsers:" + name, id);

        return new Course(name);
    }

    public ArrayList<Course> getCoursesById(String id) {

        ArrayList<Course> courses = new ArrayList<Course>();
        for (String courseName : jedis.smembers("userCourses:" + id)) {
            courses.add(new Course(courseName));
        }

        return courses;
    }

    public ArrayList<User> getUsersBySchool(String school) {

        ArrayList<User> users = new ArrayList<User>();
        for (String id : jedis.smembers("schoolUsers:" + school)) {
            users.add(getUserById(id));
        }

        return users;
    }

    public User getUserById(String id) {

        String name = jedis.hget("user:" + id, "name");
        String school = jedis.hget("user:" + id, "school");

        ArrayList<Course> courses = getCoursesById(id);

        return new User(id, name, school, courses);
    }

    public Message createMessage(String senderId, String recId, String text) {

        String id = UUID.randomUUID().toString();

        Message message = new Message(text, id, senderId, recId, new Date());

        Long test = new Date().getTime();

        jedis.hset("message:" + id, "text", text);
        jedis.hset("message:" + id, "time", "" + message.getTime());
        jedis.hset("message:" + id, "sender", senderId);
        jedis.hset("message:" + id, "receiver", recId);


        jedis.lpush("messages:" + senderId, id);
        jedis.lpush("messages:" + recId, id);


        return message;
    }

    public Message getMessageById(String id) {

        String text = jedis.hget("message:" + id, "text");
        String sender = jedis.hget("message:" + id, "sender");
        String receiver = jedis.hget("message:" + id, "receiver");
        String time = jedis.hget("message:" + id, "time");

        Date date = new Date(Long.parseLong(time));

        return new Message(text, id, sender, receiver, date);


    }

    public ArrayList<Message> getMessages(String senderId, String recId) {

        ArrayList<String> idSender = new ArrayList<String>();
        ArrayList<String> idReceiver = new ArrayList<String>();

        for (String id : jedis.lrange("messages:" + senderId, 0, -1)) {
            idSender.add(id);
        }

        for (String id : jedis.lrange("messages:" + recId, 0, -1)) {
            idReceiver.add(id);
        }

        idSender.retainAll(idReceiver);

        ArrayList<Message> messages = new ArrayList<Message>();

        for (String id : idSender) {
            messages.add(0, getMessageById(id));
        }
        
        return messages;


    }
}
