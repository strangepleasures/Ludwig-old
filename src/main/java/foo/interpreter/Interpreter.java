package foo.interpreter;

import foo.model.*;
import org.pcollections.*;

public class Interpreter {
    public static Object eval(Node node, HashPMap<NamedNode, Object> locals) {
        return node.accept(new InterpretingVisitor(locals));
    }

    public static Object call(FunctionNode functionNode, Object... args) {
        BoundCallNode boundCallNode = new BoundCallNode();
        boundCallNode.setFunction(functionNode);

        for (int i = 0; i < args.length; i++) {
            boundCallNode.getArguments().put(functionNode.getParameters().get(i), LiteralNode.ofValue(args[i]));
        }

        return eval(boundCallNode, HashTreePMap.empty());
    }

}
