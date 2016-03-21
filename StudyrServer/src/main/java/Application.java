/**
 * Created by miles on 2/15/16.
 */
import Data.Course;
import Data.User;
import JSON.Json;
import Redis.RedisPostingService;
import redis.clients.jedis.Jedis;
import spark.ResponseTransformer;

import java.util.ArrayList;
import java.util.UUID;

import static spark.Spark.*;
public class Application {

    static ResponseTransformer toJson = obj -> Json.gson.toJson(obj);

    static RedisPostingService rps;

    public static void main(String[] args) {
	
        Jedis jedis = new Jedis("localhost");

        rps = new RedisPostingService(jedis);

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

                    User user = rps.createUserWithId(id, name, school);
                    if (user == null) {
                        res.status(200); // Not sure what the correct response code is in the case that this user already exists
                        return user;
                    }
                    res.status(201);
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

        // Gets info about a user
        get("/u/:id/info",
                (req, res) -> {
                    String id = req.params(":id");

                    User user = rps.getUserById(id);
                    System.out.println(user.getName());
                    System.out.println(user.getSchool());
                    res.status(201);
                    return user;
                }, toJson
        );


        // TODO: This is returning all users even if they share no courses in common
        // Gets users that share classes and go to the same school as another user
        get("/u/:id/similar",
                (req, res) -> {
                    String id = req.params(":id");

                    ArrayList<User> users = rps.getSimilarUsers(id);
                    res.status(201);
                    return users;
                }, toJson
        );



    }

}
