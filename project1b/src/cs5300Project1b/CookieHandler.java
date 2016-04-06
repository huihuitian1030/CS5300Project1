package cs5300Project1b;

public class CookieHandler {
    public static final String localtion = "0_0";
    public static String creatCookieData(int sessionId, SessionInfo data) {
        String cookieValue =  sessionId + "__" + data.getVersion() + "__" + localtion;
        return cookieValue;
    }
    
    public static int getSessionId (String cookieValue) {
        return Integer.parseInt(cookieValue.substring(0, cookieValue.indexOf("_")));
    }
}

