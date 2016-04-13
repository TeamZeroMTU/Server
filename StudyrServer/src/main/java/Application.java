/**
 * Created by miles on 2/15/16.
 */

import org.eclipse.jetty.util.log.Log;

import java.util.ArrayList;
import java.util.Collection;

import Data.Course;
import Data.Message;
import Data.User;
import Facebook.TokenInfo;
import Facebook.TokenInterrogator;
import JSON.Json;
import Redis.RedisPostingService;
import redis.clients.jedis.Jedis;
import spark.ResponseTransformer;

import static spark.Spark.exception;
import static spark.Spark.get;
import static spark.Spark.post;
public class Application {

    static ResponseTransformer toJson = obj -> Json.gson.toJson(obj);

    static RedisPostingService rps;

    public static void main(String[] args) {

        Jedis jedis = new Jedis("localhost");

        rps = new RedisPostingService(jedis);

        TokenInterrogator fbInterrogator = new TokenInterrogator();

        // Creates a new user
        post("/u/create",
                (req, res) -> {
                    try {
                        String userToken = req.queryParams("token");
                        final TokenInfo info = fbInterrogator.getUserTokenInfo(userToken);
                        if(info != null && info.data != null && fbInterrogator.isValid(info)) {
                            rps.createUserWithId(info.data.user_id, "", "");
                            res.status(201);
                            return info.data.user_id;
                        }
                    } catch (Exception e) {
                        Log.getLog().warn("create:", e);
                    }
                    res.status(404);
                    return null;
                }, toJson
        );

        // Creates a user with set ID
        post("/u/:id/create",
                (req, res) -> {
                    String id = req.params(":id");
                    String name = req.queryParams("name");
                    String school = req.queryParams("school");
                    System.out.println(name);
                    System.out.println(school);

                    User user  = rps.createUserWithId(id, name, school);
                    if (user == null) {
                        res.status(200); // Not sure what the correct response code is in the case that this user already exists
                        return user;
                    }
                    res.status(201);
                    return user;

                }, toJson
        );

        // Updates the user's school
        post("/u/:id/changeSchool",
                (req, res) -> {
                    try {
                        String id = req.params(":id");
                        String userToken = req.queryParams("token");
                        if(userToken == null) {
                            Log.getLog().warn("ChangeSchool: Null user token");
                        } else {
                            final TokenInfo info = fbInterrogator.getUserTokenInfo(userToken);
                            if (info != null && info.data.user_id.compareTo(id) == 0) {
                                String school = req.queryParams("school");

                                User user = rps.updateSchool(id, school);
                                System.out.println(
                                        "changeSchool: \n" +
                                                "\tUser: " + user.getName() + "\n" +
                                                "\tSchool: " + user.getSchool());
                                res.status(201);
                                return user;
                            } else {
                                Log.getLog().warn("Invalid change school request");
                            }
                        }
                    } catch (Exception e) {
                        Log.getLog().warn("changeSchool:", e);
                    }
                    res.status(404);
                    return null;
                }, toJson
        );

        // Adds a course to an existing user
        post("/u/:id/addcourse",
                (req, res) -> {
                    String id = req.params(":id");
                    String name = req.queryParams("name");

                    User user = rps.addCourseToUser(id, name);
                    res.status(201);
                    return user;
                }, toJson
        );

        // Removes a course from an existing user
        post("/u/:id/removecourse",
                (req, res) -> {
                    String id = req.params(":id");
                    String name = req.queryParams("name");

                    User user = rps.removeCourseFromUser(id, name);
                    res.status(201);
                    return user;
                }, toJson
        );

        // Route for testing code locally when I don't have the FB token
        post("/test/u/:id/info",
                (req, res) -> {
                    return rps.getUserById(req.params(":id"));
                }, toJson);

        // Gets info about a user
        post("/u/:id/info",
                (req, res) -> {
                    try {
                        String userToken = req.queryParams("token");
                        final TokenInfo info = fbInterrogator.getUserTokenInfo(userToken);
                        if(rps.getUserById(info.data.user_id) != null) {
                            String id = req.params(":id");

                            User user = rps.getUserById(id);
                            System.out.println(
                                    "Info: \n" +
                                            "\tUser: " + user.getUserID() + "\n" +
                                    "\tUser: " + user.getName() + "\n" +
                                    "\tSchool: " + user.getSchool());
                            res.status(201);
                            return user;
                        }
                    } catch (Exception e) {
                    }
                    res.status(404);
                    return null;
                }, toJson
        );

        // Gets all courses associated with a school
        post("/s/:school/courses",
                (req, res) -> {
                    ArrayList<Course> courses = rps.getCoursesForSchool(req.params(":school"));
                    res.status(200);
                    return courses;
                }, toJson
        );

        // Rejects a user
        post("/u/:id/reject",
                (req, res) -> {
                    String id = req.params(":id");
                    String rejectId = req.queryParams("rejectId");

                    return rps.rejectUser(id, rejectId);
                }, toJson
        );

        // Matches with a user
        post("/u/:id/match",
                (req, res) -> {
                    String id = req.params(":id");
                    String matchId = req.queryParams("matchId");

                    return rps.matchUser(id, matchId);
                }, toJson
        );

        // Gets all matches for one user
        post("/u/:id/matches",
                (req, res) -> {
                    return rps.getMatches(req.params(":id"));
                }, toJson);


        // Gets users that share classes and go to the same school as another user
        post("/u/:id/similar",
                (req, res) -> {
                    String id = req.params(":id");

                    ArrayList<User> users = rps.getSimilarUsers(id);
                    res.status(201);
                    return users;
                }, toJson
        );

        // Gets a users own ID.
        post("/me/id",
                (req, res) -> {
                    try {
                        String userToken = req.queryParams("token");
                        final TokenInfo info = fbInterrogator.getUserTokenInfo(userToken);
                        User user = rps.getUserById( info.data.user_id );
                        String name = user.getName();
                        if(name == null || name.equals("")) {
                            String newName = fbInterrogator.getName( info.data.user_id, userToken );
                            if(newName != null) {
                                rps.setUserName( info.data.user_id, newName );
                            }
                        }
                        res.status(201);
                        return info.data.user_id;
                    } catch (Exception e) {
                        res.status(404);
                        return null;
                    }
                }, toJson
        );

        // TODO: Fix date so that it is saved in a format that will be accessible
        // Sends a message to a user
        post("/u/:id/sendmessage",
                (req, res) -> {
                    String id = req.params(":id");
                    String recId = req.queryParams("recid");
                    String text = req.queryParams("text");

                    Message msg = rps.createMessage(id, recId, text);
                    return msg;
                }, toJson
        );

        // TODO: Fix date so that it is saved in a format that will be accessible
        // Gets the messges between two users
        post("/u/:id/getmessages",
                (req, res) -> {
                    String id = req.params(":id");
                    String recId = req.queryParams("recid");
                    ArrayList<Message> msgs = rps.getMessages(id, recId);
                    return msgs;
                }, toJson
        );
        
        get("/developer/blaineneedsthings",
                (req, res) -> {
                    Collection<String> userIds = rps.getAllUserIds();
                    ArrayList<User> users = new ArrayList<User>();
                    for (String id : userIds) {
                        users.add(rps.getUserById(id));
                    }
                    return users;
                }, toJson
        );

        exception(Exception.class, (e, request, response) -> {
            Log.getLog().warn("Exception", e);
            response.status(404);
            response.body("Resource not found");
        });
    }
}
