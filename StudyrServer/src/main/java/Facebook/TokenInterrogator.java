package Facebook;

import com.google.gson.Gson;

import org.eclipse.jetty.util.log.Log;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

/**
 * Created by jbdaley on 3/16/16.
 */
public class TokenInterrogator {
    private final String fbPrivateAppKey;
    private final String fbPublicAppKey = "576652959159971";

    public TokenInterrogator() {
        InputStream in = ClassLoader.getSystemClassLoader().getResourceAsStream("private.txt");
        BufferedReader reader = new BufferedReader(new InputStreamReader(in));
        String fileKey = null;
        try {
            fileKey = reader.readLine();
        } catch (java.io.IOException e) {
            Log.getLog().warn("Unable to aquire facebook private key!\n" + e.toString());
        }
        fbPrivateAppKey = fileKey;
    }

    public TokenInfo getUserTokenInfo(final String tokenString) {
        try {
            if(tokenString == null) {
                Log.getLog().info("token is null");
            } else {
                Log.getLog().info("Starting token info request");
                StringBuilder urlBuilder =
                        new StringBuilder()
                                .append("https://graph.facebook.com/v2.5/debug_token?")
                                .append(URLEncoder.encode("input_token", "UTF-8"))
                                .append("=")
                                .append(URLEncoder.encode(tokenString, "UTF-8"))
                                .append("&")
                                .append(URLEncoder.encode("access_token", "UTF-8"))
                                .append("=")
                                .append(fbPublicAppKey + "|" + fbPrivateAppKey);

                final String urlString = urlBuilder.toString();
                URL url = new URL(urlString);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setReadTimeout(10000);
                connection.setConnectTimeout(15000);
                connection.setRequestMethod("GET");
                connection.setDoOutput(false);
                connection.setDoInput(true);
                connection.connect();
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String input;
                StringBuilder responseBuilder = new StringBuilder();
                while ((input = reader.readLine()) != null) {
                    responseBuilder.append(input);
                }
                reader.close();
                final String jsonString = responseBuilder.toString();
                return new Gson().fromJson(jsonString, TokenInfo.class);
            }
        } catch (Exception e) {
            Log.getLog().warn("TokenInterrogator: Invalid token info!", e);
        }
        Log.getLog().warn("TokenInterrogator: Null token!");
        return null;
    }

    public boolean isValid(TokenInfo info) {
        return info.data.app_id == fbPublicAppKey && info.data.is_valid.compareTo("true") == 0;
    }
}