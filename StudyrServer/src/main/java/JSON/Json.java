package JSON;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.time.Instant;
import java.util.List;

/**
 * Holds the gson instance
 */
public class Json {
    public static Gson gson = new GsonBuilder()
            .registerTypeAdapter(Instant.class, new InstantSerializer())
            .registerTypeAdapter(Instant.class, new InstantDeserializer())
            .create();

    private static final Type listOfStringType = new TypeToken<List<String[]>>() {}.getType();

    /**
     * Reads a string list from a json string. Allows null parameter.
     * @param json a string containing a json array, or null
     * @return A list of strings, or null if input was null
     */
    public static List<String> listOfStringFromJson(String json) {
        if (json == null) {
            return null;
        }
        return Json.gson.fromJson(json, listOfStringType);
    }
}
