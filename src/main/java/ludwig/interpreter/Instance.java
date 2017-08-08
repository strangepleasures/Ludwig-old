package ludwig.interpreter;

import java.util.HashMap;
import java.util.Map;

public class Instance {
    private final Map<Field, Object> data = new HashMap<>();

    public <R> R get(Field field) {
        return (R) data.get(field);
    }

    public <R> void set(Field field, R value) {
        data.put(field, value);
    }
}
