package ludwig.interpreter;

import ludwig.model.*;
import org.pcollections.HashPMap;
import org.pcollections.HashTreePMap;

public class Interpreter {
    public static Object eval(Node node, HashPMap<NamedNode, Object> locals) {
        return node.accept(new Evaluator(locals));
    }

    public static Object call(FunctionNode functionNode, Object... args) {
        ReferenceNode head = new ReferenceNode(functionNode);

        for (Object arg : args) {
            head.add(LiteralNode.Companion.ofValue(arg));
        }

        return eval(head, HashTreePMap.empty());
    }

}
