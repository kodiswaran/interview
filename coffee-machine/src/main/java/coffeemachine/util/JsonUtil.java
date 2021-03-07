package coffeemachine.util;

import org.json.simple.JSONObject;

import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

public class JsonUtil {

    /**
     * Convert the json object to a hashmap with string as key and integer as the value
     * @param jsonObject the input json object
     * @return json object converted to hashmap with string as key and integer as the value
     */
    @SuppressWarnings("unchecked")
    public static Map<String, Integer> convertObjectToMap(final JSONObject jsonObject) {
        return ((Set<String>) jsonObject.keySet())
                .stream()
                .collect(Collectors.toMap(Function.identity(), str -> JsonUtil.getValue(str, jsonObject, Long.class).intValue()));
    }

    /**
     * For the given json object, it fetches the value using the key and returns the value
     * type casting to the given type
     *
     * @param key the required key to query on the object
     * @param jsonObject the input json object
     * @param type the type of the value
     * @param <T> the type of the value
     * @return the type casted value mapped to the key in the json object
     */
    public static <T> T getValue(final String key, final JSONObject jsonObject, final Class<T> type) {
        return type.cast(jsonObject.get(key));
    }
}
