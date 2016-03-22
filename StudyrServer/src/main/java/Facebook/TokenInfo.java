package Facebook;

/**
 * Created by jbdaley on 3/9/16.
 */
public class TokenInfo {

    TokenInfo() {}

    public class Data {
        public String app_id;
        public String application;
        public String expires_at;
        public String is_valid;
        public String issued_at;
        public String[] scopes;
        public String user_id;
    }

    public Data data;
}
