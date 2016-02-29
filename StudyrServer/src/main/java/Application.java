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

        Jedis jedis = new Jedis("localhost");
        jedis.set("foo", "bar");

        rps = new RedisPostingService(jedis);


        get("/", (req, res) -> "Welcome to Studyr");

        get("/hello", (req, res) -> {

            System.out.println("hello world");
            return "Hello, world!";
        });

//        get("/redis", (req, res) -> jedis.get("foo"));

        post("/u/create",
                (req, res) -> {
                    String name = req.queryParams("name");
                    String school = req.queryParams("school");

                    User user = rps.createUser(name, school);
                    res.status(201);
                    return user;

                }, toJson
        );

        post("/u/:id/addcourse",
                (req, res) -> {
                    String id = req.params(":id");
                    String name = req.queryParams("name");

                    Course course = rps.addCourseToUser(id, name);
                    res.status(201);
                    return course;
                }, toJson
        );

        delete("/u/:id/removecourse",
                (req, res) -> {
                    String id = req.params(":id");
                    String name = req.queryParams("name");

                    Course course = rps.removeCourseFromUser(id, name);
                    res.status(201);
                    return course;
                }, toJson
        );

        get("/u/:id/info",
                (req, res) -> {
                    String id = req.params(":id");

                    User user = rps.getUserById(id);
                    res.status(201);
                    return user;
                }, toJson
        );



        get("/u/:id/get",
                (req, res) -> {
                    return null;
                });

//        post("/post",
//                (req, res) -> {
//                    System.out.println("test");
//
//                    User user = new User("id", "name", "school" );
//
//                    res.status(201);
//                    return user;
//
//                }, toJson
//        );

        get("/redis/:key/get", (req, res) ->
                jedis.get(req.params(":key")));

    }

}
