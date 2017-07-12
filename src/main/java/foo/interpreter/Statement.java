package foo.interpreter;

import foo.model.NamedNode;
import foo.model.Node;
import org.pcollections.HashPMap;

@FunctionalInterface
public interface Statement {
    Object eval(Interpreter interpreter, HashPMap<NamedNode, Object> locals, Node[] arguments);
}
