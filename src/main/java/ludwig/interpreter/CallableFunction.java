package ludwig.interpreter;

import ludwig.model.*;
import org.pcollections.HashPMap;
import org.pcollections.HashTreePMap;

import java.util.Map;

public class CallableFunction implements Callable {
    private final FunctionNode function;
    private int argCount;

    public CallableFunction(FunctionNode function) {
        this.function = function;

        for (int i = 0; i < function.children().size(); i++) {
            if (function.children().get(i) instanceof SeparatorNode) {
                argCount = i;
                break;
            }
        }
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
            env = env.plus((NamedNode) function.children().get(i), args[i]);
        }

        InterpretingVisitor visitor = new InterpretingVisitor(env);

        Object result = null;

        for (int i = argCount + 1; i < function.children().size(); i++) {
            result = function.children().get(i).accept(visitor);
            if (result instanceof Signal) {
                break;
            }
        }

        return result;
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
