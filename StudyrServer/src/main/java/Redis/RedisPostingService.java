package Redis;

import Data.Course;
import Data.Message;
import Data.User;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Transaction;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.Set;
import java.util.UUID;

/**
 * Created by miles on 2/27/16.
 */
public class RedisPostingService {

    Jedis jedis;
    Transaction txn;

    public RedisPostingService(Jedis jedis) {
        this.jedis = jedis;
    }

    public User createUser(String name, String school) {

        // Set txn
        txn  = jedis.multi();

        // Randomly generate an ID (This may be changed later if we are using Facebook IDs)
        String id = UUID.randomUUID().toString();

        // Map the name and school to the ID and add the ID to a list associated with the school
        txn.hset("user:" + id, "name", name);
        txn.hset("user:" + id, "school", school);
        txn.sadd("schoolUsers:" + school, id);

        txn.exec();

        return new User(id, name, school);
    }

    public User createUserWithId(String id, String name, String school) {

        // Set txn
        txn  = jedis.multi();

        txn.hset("user:" + id, "name", name);
        txn.hset("user:" + id, "school", school);
        txn.sadd("schoolUsers:" + school, id);

        txn.exec();

        return new User(id, name, school);
    }

    public User matchUser(String id, String matchId) {

        // Set txn
        txn  = jedis.multi();

        txn.sadd("match:" + id, matchId);

        txn.exec();

        return getUserById(id);
    }

    public Course addCourseToUser(String id, String name) {

        // Set txn
        txn  = jedis.multi();

        // Add the name of the course to a list of courses for the id
        txn.sadd("userCourses:" + id, name);
        // Add the user ID to a list of IDs associated with that course
        txn.sadd("courseUsers:" + name, id);

        txn.exec();

        return new Course(name);
    }

    public Course removeCourseFromUser(String id, String name) {

        // Set txn
        txn  = jedis.multi();

        // Remove from the userCourses set
        txn.srem("userCourses:" + id, name);
        // Remove from the courseUsers set
        txn.srem("courseUsers:" + name, id);

        txn.exec();

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
        System.out.println("NAME: " + name);
        String school = jedis.hget("user:" + id, "school");
        System.out.println("SCHOOL: " + school);

        ArrayList<Course> courses = getCoursesById(id);

        return new User(id, name, school, courses);
    }

    public ArrayList<String> getPotentialMatchIds(String id) {
        ArrayList<String> matches = new ArrayList<String>();
        for (String matchId : jedis.smembers("match:" + id)) {
            matches.add(matchId);
        }
        return matches;
    }

    public ArrayList<User> getMatches(String id) {
        ArrayList<String> potentialMatches = getPotentialMatchIds(id);

        for (String matchId : potentialMatches) {
            ArrayList<String> matches = getPotentialMatchIds(matchId);
            for (String mId : matches) {
                if (mId.equals(id)) {
                    continue;
                }
            }
            potentialMatches.remove(matchId);
        }

        ArrayList<User> foundMatches = new ArrayList<User>();

        for (String mId : potentialMatches) {
            foundMatches.add(getUserById(mId));
        }

        return foundMatches;

    }

    public Message createMessage(String senderId, String recId, String text) {

        // Set txn
        txn  = jedis.multi();

        String id = UUID.randomUUID().toString();

        Message message = new Message(text, id, senderId, recId, new Date());

        Long test = new Date().getTime();

        txn.hset("message:" + id, "text", text);
        txn.hset("message:" + id, "time", "" + message.getTime());
        txn.hset("message:" + id, "sender", senderId);
        txn.hset("message:" + id, "receiver", recId);


        txn.lpush("messages:" + senderId, id);
        txn.lpush("messages:" + recId, id);

        txn.exec();


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

    public ArrayList<User> getSimilarUsers(String id) {


        ArrayList<Course> courses = getCoursesById(id);
        ArrayList<String> courseStrings = new ArrayList<String>();

        for (Course course : courses) {
            courseStrings.add(course.getName());
        }

        ArrayList<User> users = getUsersBySchool(jedis.hget("user:" + id, "school"));

        for (User user : users) {
            ArrayList<Course> userCourses = getCoursesById(id);
            ArrayList<String> userCourseStrings = new ArrayList<String>();
            for (Course course : userCourses) {
                userCourseStrings.add(course.getName());
            }

            userCourseStrings.retainAll(courseStrings);
            if (userCourseStrings.size() == 0) {
                users.remove(user);
            }
        }

        return users;
    }
}
