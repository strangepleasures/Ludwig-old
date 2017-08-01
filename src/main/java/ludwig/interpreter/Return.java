package ludwig.interpreter;

import lombok.Value;
import ludwig.model.NamedNode;
import ludwig.model.Node;
import org.pcollections.HashPMap;

import java.util.Map;
import java.util.function.Supplier;

@Value
public class Return<T> implements Signal, Supplier<T> {
    Node node;
    HashPMap<NamedNode, Object> locals;
    Map<NamedNode, Object> globals;

    public T get() {
        Node n = node;
        HashPMap<NamedNode, Object> l = locals;
        Map<NamedNode, Object> g = globals;

        while (true) {
            Object r = n.accept(new InterpretingVisitor(l, g));
            if (r instanceof Return) {
                Return inner = (Return) r;
                n = inner.node;
                l = inner.locals;
            } else {
                return (T) r;
            }
        }
    }
}
