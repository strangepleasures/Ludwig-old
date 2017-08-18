package ludwig.interpreter;

import ludwig.model.VariableNode;

import java.util.HashMap;
import java.util.Map;

public class Instance {
    private final ClassType type;
    private final Map<VariableNode, Object> data = new HashMap<>();

    public Instance(ClassType type) {
        this.type = type;
    }

    public ClassType type() {
        return type;
    }

    public <R> R get(VariableNode field) {
        return (R) data.get(field);
    }

    public <R> void set(VariableNode field, R value) {
        data.put(field, value);
    }
}
