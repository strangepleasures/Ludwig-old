package ludwig.interpreter;

import ludwig.model.NamedNode;
import ludwig.model.Node;
import org.pcollections.HashPMap;

public class Return<T> implements Signal, Delayed<T> {
    Node node;
    HashPMap<NamedNode, Object> locals;

    public Return(Node node, HashPMap<NamedNode, Object> locals) {
        this.node = node;
        this.locals = locals;
    }

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
