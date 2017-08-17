package ludwig.interpreter;

import ludwig.model.FieldNode;

import java.util.HashMap;
import java.util.Map;

public class Instance {
    private final ClassType type;
    private final Map<FieldNode, Object> data = new HashMap<>();

    public Instance(ClassType type) {
        this.type = type;
    }

    public ClassType type() {
        return type;
    }

    public <R> R get(FieldNode field) {
        return (R) data.get(field);
    }

    public <R> void set(FieldNode field, R value) {
        data.put(field, value);
    }
}
