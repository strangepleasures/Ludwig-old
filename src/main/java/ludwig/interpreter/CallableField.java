package ludwig.interpreter;

import ludwig.model.FieldNode;
import org.pcollections.HashTreePMap;

public class CallableField implements Callable {
    private final FieldNode field;

    public CallableField(FieldNode field) {
        this.field = field;
    }

    @Override
    public Object tail(Object... args) {
        return new Evaluator(HashTreePMap.empty()).tail(field, args);
    }

    @Override
    public int argCount() {
        return 1;
    }
}
