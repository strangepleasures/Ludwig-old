package ludwig.interpreter;

import ludwig.model.*;
import ludwig.utils.NodeUtils;
import org.pcollections.HashPMap;
import org.pcollections.HashTreePMap;

import java.util.Map;

public class CallableFunction implements Callable {
    private final FunctionNode function;
    private final Map<NamedNode, Object> globals;

    public CallableFunction(FunctionNode function, Map<NamedNode, Object> globals) {
        this.function = function;
        this.globals = globals;
    }

    @Override
    public Object call(Object[] args) {
        Object result = tail(args);

        if (result instanceof Return) {
            return ((Return) result).get();
        }

        return result;
    }

    @Override
    public Object tail(Object... args) {
        HashPMap<NamedNode, Object> env = HashTreePMap.empty();

        for (int i = 0; i < args.length; i++) {
            env = env.plus(function.parameters().get(i), args[i]);
        }

        InterpretingVisitor visitor = new InterpretingVisitor(env, globals);

        Object result = null;

        for (Node node : function.children()) {
            result = node.accept(visitor);
            if (result instanceof Signal) {
                break;
            }
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