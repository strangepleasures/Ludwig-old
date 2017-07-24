package foo.interpreter;

import foo.model.*;
import foo.utils.NodeUtils;
import org.pcollections.HashPMap;
import org.pcollections.HashTreePMap;

public class CallableFunction implements Callable {
    private final FunctionNode function;

    public CallableFunction(FunctionNode function) {
        this.function = function;
    }

    @Override
    public Object call(Object[] args) {
        HashPMap<NamedNode, Object> env = HashTreePMap.empty();

        for (int i = 0; i < args.length; i++) {
            env = env.plus(function.parameters().get(i), args[i]);
        }

        InterpretingVisitor visitor = new InterpretingVisitor(env);

        Object result = null;
        for (Node node : function.children()) {
            result = node.accept(visitor);
            if (result instanceof Signal) {
                break;
            }
        }

        if (result instanceof Return) {
            return ((Return) result).getValue();
        }

        return result;
    }

    @Override
    public int argCount() {
        return function.parameters().size();
    }

    @Override
    public String toString() {
        return NodeUtils.toString(function);
    }
}
