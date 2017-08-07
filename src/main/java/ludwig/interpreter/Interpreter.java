package ludwig.interpreter;

import ludwig.model.*;
import org.pcollections.HashPMap;
import org.pcollections.HashTreePMap;

import java.util.HashMap;
import java.util.Map;

public class Interpreter {
    public static Object eval(Node node, HashPMap<NamedNode, Object> locals, Map<NamedNode, Object> globals) {
        return node.accept(new InterpretingVisitor(locals));
    }

    public static Object call(FunctionNode functionNode, Object... args) {
        ReferenceNode head = new ReferenceNode(functionNode);

        for (Object arg : args) {
            head.add(LiteralNode.ofValue(arg));
        }

        return eval(head, HashTreePMap.empty(), new HashMap<>());
    }

}
