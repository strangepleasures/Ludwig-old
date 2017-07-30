package ludwig.interpreter;

import ludwig.model.*;
import org.pcollections.HashPMap;
import org.pcollections.HashTreePMap;

import java.util.HashMap;
import java.util.Map;

public class Interpreter {
    public static Object eval(Node node, HashPMap<NamedNode, Object> locals, Map<NamedNode, Object> globals) {
        return node.accept(new InterpretingVisitor(locals, globals));
    }

    public static Object call(FunctionNode functionNode, Object... args) {
        BoundCallNode boundCallNode = new BoundCallNode();
        boundCallNode.add(new RefNode(functionNode));

        for (int i = 0; i < args.length; i++) {
            boundCallNode.arguments().put(functionNode.parameters().get(i), LiteralNode.ofValue(args[i]));
        }

        return eval(boundCallNode, HashTreePMap.empty(), new HashMap<>());
    }

}
