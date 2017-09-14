package ludwig.interpreter;

import ludwig.model.FunctionNode;
import ludwig.model.Node;
import ludwig.utils.NodeUtils;
import org.pcollections.HashTreePMap;


public class CallableRef implements Callable {
    private final Node<?> function;
    private int argCount;

    public CallableRef(Node<?> function) {
        this.function = function;
        argCount = NodeUtils.INSTANCE.arguments(function).size(); // TODO: Optimize
    }

    @Override
    public Object tail(Object... args) {
        return new Evaluator(HashTreePMap.empty()).tail(function, args);
    }

    @Override
    public boolean isLazy() {
        return (function instanceof FunctionNode) && ((FunctionNode)function).lazy();
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
