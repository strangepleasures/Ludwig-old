package ludwig.interpreter;

import ludwig.model.*;
import ludwig.utils.NodeUtils;
import ludwig.utils.PrettyPrinter;
import org.pcollections.HashPMap;

import java.util.Map;

public class Closure implements Callable {
    private final HashPMap<NamedNode, Object> locals;
    private final Map<NamedNode, Object> globals;
    private final LambdaNode lambda;
    private int argCount;

    public Closure(HashPMap<NamedNode, Object> locals, Map<NamedNode, Object> globals, LambdaNode lambda) {
        this.locals = locals;
        this.globals = globals;
        this.lambda = lambda;

        for (int i = 0; i < lambda.children().size(); i++) {
            Node node = lambda.children().get(i);
            if (node instanceof SeparatorNode) {
                argCount = i;
                break;
            }
        }
    }

    @Override
    public Object tail(Object[] args) {
        HashPMap<NamedNode, Object> env = locals;
        InterpretingVisitor visitor = null;
        Object result = null;

        for (int i = 0; i < lambda.children().size(); i++) {
            Node node = lambda.children().get(i);
            if (node instanceof VariableNode) {
                env = env.plus((NamedNode) node, args[i]);
            } else if (node instanceof SeparatorNode) {
                visitor = new InterpretingVisitor(env, globals);
            } else {
                result = node.accept(visitor);
                if (result instanceof Signal) {
                    break;
                }
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
        return PrettyPrinter.print(lambda);
    }
}
