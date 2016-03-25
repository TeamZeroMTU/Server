/**
 * Created by miles on 2/15/16.
 */

import java.util.ArrayList;

import Data.Course;
import Data.User;
import Facebook.TokenInfo;
import Facebook.TokenInterrogator;
import JSON.Json;
import Redis.RedisPostingService;
import redis.clients.jedis.Jedis;
import spark.ResponseTransformer;

import static spark.Spark.delete;
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
                    String name = req.queryParams("name");
                    String school = req.queryParams("school");

                    User user = rps.createUser(name, school);
                    res.status(201);
                    return user;

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

                    User user = rps.createUserWithId(id, name, school);
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
                    String id = req.params(":id");

                    String school = req.queryParams("school");

                    User user = rps.updateSchool(id, school);

                    return user;
                }, toJson
        );

        // Adds a course to an existing user
        post("/u/:id/addcourse",
                (req, res) -> {
                    String id = req.params(":id");
                    String name = req.queryParams("name");

                    Course course = rps.addCourseToUser(id, name);
                    res.status(201);
                    return course;
                }, toJson
        );

        // Removes a course from an existing user
        delete("/u/:id/removecourse",
                (req, res) -> {
                    String id = req.params(":id");
                    String name = req.queryParams("name");

                    Course course = rps.removeCourseFromUser(id, name);
                    res.status(201);
                    return course;
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
                            System.out.println(user.getName());
                            System.out.println(user.getSchool());
                            res.status(201);
                            return user;
                        }
                    } catch (Exception e) {
                    }
                    res.status(404);
                    return null;
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

        // TODO: This is returning all users even if they share no courses in common
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
                        res.status(201);
                        return info.data.user_id;
                    } catch (Exception e) {
                        res.status(404);
                        return null;
                    }
                }, toJson
        );
    }
}
