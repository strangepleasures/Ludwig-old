package ludwig.interpreter;

import ludwig.model.FieldNode;

public class CallableField implements Callable {
    private final FieldNode field;

    public CallableField(FieldNode field) {
        this.field = field;
    }

    @Override
    public Object tail(Object... args) {
        return ((Instance)args[0]).get(field);
    }

    @Override
    public int argCount() {
        return 1;
    }
}
