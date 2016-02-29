/**
 * Created by miles on 2/15/16.
 */
import Data.Course;
import Data.User;
import JSON.Json;
import Redis.RedisPostingService;
import redis.clients.jedis.Jedis;
import spark.ResponseTransformer;
import java.util.UUID;

import static spark.Spark.*;
public class Application {

    static ResponseTransformer toJson = obj -> Json.gson.toJson(obj);

    static RedisPostingService rps;

    public static void main(String[] args) {
	port(4200);
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
                    res.status(201);
                    return user;
                }, toJson
        );

    }

}
