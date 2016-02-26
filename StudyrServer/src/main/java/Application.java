/**
 * Created by miles on 2/15/16.
 */
import redis.clients.jedis.Jedis;

import static spark.Spark.*;
public class Application {

    public static void main(String[] args) {

        Jedis jedis = new Jedis("localhost");
        jedis.set("foo", "bar");

        get("/hello", (req, res) -> "Hello, World!");

        get("/redis", (req, res) -> jedis.get("foo"));

        post("/redis/:key/post", (req, res) -> {
            String key = req.params("key");
//            String value = req.queryParams("value");

            jedis.set(key, "martin");


            return "martin";
        });

        get("/redis/:key/get", (req, res) ->
            jedis.get(req.params(":key")));

    }

}
