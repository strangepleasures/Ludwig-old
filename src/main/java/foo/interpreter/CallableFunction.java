package foo.interpreter;

import foo.model.*;
import foo.utils.PrintUtil;
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
            env = env.plus(function.getParameters().get(i), args[i]);
        }

        InterpretingVisitor visitor = new InterpretingVisitor(env);

        Object result = null;
        for (Node node : function.getChildren()) {
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
    public String toString() {
        return PrintUtil.toString(function);
    }
}
