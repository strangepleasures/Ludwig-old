package ludwig.interpreter;

import ludwig.model.*;
import org.pcollections.HashTreePMap;

import static ludwig.utils.NodeUtils.arguments;

public class CallableRef implements Callable {
    private final Node<?> function;
    private int argCount;

    public CallableRef(Node<?> function) {
        this.function = function;
        argCount = arguments(function).size(); // TODO: Optimize
    }

    @Override
    public Object tail(Object... args) {
        return new Evaluator(HashTreePMap.empty()).tail(function, args);
    }

    @Override
    public boolean isLazy() {
        return (function instanceof FunctionNode) && ((FunctionNode)function).isLazy();
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
