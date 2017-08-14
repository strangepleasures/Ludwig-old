package ludwig.interpreter;

import ludwig.model.FieldNode;

import java.util.HashMap;
import java.util.Map;

public class Instance {
    private final Type type;
    private final Map<FieldNode, Object> data = new HashMap<>();

    public Instance(Type type) {
        this.type = type;
    }

    public Type type() {
        return type;
    }

    public <R> R get(FieldNode field) {
        return (R) data.get(field);
    }

    public <R> void set(FieldNode field, R value) {
        data.put(field, value);
    }
}
