package foo.interpreter;

import foo.model.*;
import foo.utils.NodeUtils;
import org.pcollections.HashPMap;

import java.util.Map;

public class Closure implements Callable {
    private final HashPMap<NamedNode, Object> locals;
    private final Map<NamedNode, Object> globals;
    private final LambdaNode lambda;

    public Closure(HashPMap<NamedNode, Object> locals, Map<NamedNode, Object> globals, LambdaNode lambda) {
        this.locals = locals;
        this.globals = globals;
        this.lambda = lambda;
    }

    @Override
    public Object call(Object[] args) {
        HashPMap<NamedNode, Object> env = locals;
        for (int i = 0; i < args.length; i++) {
            env = env.plus(lambda.parameters().get(i), args[i]);
        }

        InterpretingVisitor visitor = new InterpretingVisitor(env, globals);

        Object result = null;
        for (Node node : lambda.children()) {
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
        return lambda.parameters().size();
    }

    @Override
    public String toString() {
        return NodeUtils.toString(lambda);
    }
}
