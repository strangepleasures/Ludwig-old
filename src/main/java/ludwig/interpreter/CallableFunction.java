package ludwig.interpreter;

import ludwig.model.*;
import org.pcollections.HashPMap;
import org.pcollections.HashTreePMap;

public class CallableFunction implements Callable {
    private final FunctionNode function;
    private int argCount;

    public CallableFunction(FunctionNode function) {
        this.function = function;

        for (int i = 0; i < function.children().size(); i++) {
            if (!(function.children().get(i) instanceof VariableNode)) {
                argCount = i;
                break;
            }
        }
    }

    @Override
    public Object tail(Object... args) {
        return new Evaluator(HashTreePMap.empty()).tail(function, args);
    }

    @Override
    public boolean isLazy() {
        return function.isLazy();
    }

    @Override
    public int argCount() {
        return argCount;
    }

    @Override
    public String toString() {
        return "ref " + function;
    }
}
