package ludwig.interpreter;

import lombok.Value;
import ludwig.model.NamedNode;
import ludwig.model.Node;
import org.pcollections.HashPMap;

@Value
public class Return<T> implements Signal, Delayed<T> {
    Node node;
    HashPMap<NamedNode, Object> locals;

    public T get() {
        Node n = node;
        HashPMap<NamedNode, Object> l = locals;

        while (true) {
            Object r = n.accept(new Evaluator(l));
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
