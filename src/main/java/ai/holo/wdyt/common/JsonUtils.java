package ai.holo.wdyt.common;

public class JsonUtils {

    public static String preprocessGptJson(String json) {
        return json.replace("```json", "")
                .replace("```", "");
    }
}
