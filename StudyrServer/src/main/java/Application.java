/**
 * Created by miles on 2/15/16.
 */
import static spark.Spark.*;
public class Application {

    public static void main(String[] args) {
        get("/", (req, res) -> "Hello, World!");
    }

}
