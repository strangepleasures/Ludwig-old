package ludwig.interpreter;

import ludwig.model.*;
import ludwig.utils.NodeUtils;
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
    public Object tail(Object[] args) {
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
