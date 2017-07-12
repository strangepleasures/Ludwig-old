package foo.interpreter;

import foo.model.*;
import org.pcollections.HashPMap;

import java.util.List;

public class Closure {
    private final HashPMap<NamedNode, Object> locals;
    private final List<ParameterNode> parameters;
    private final List<Node> body;

    public Closure(HashPMap<NamedNode, Object> locals, List<ParameterNode> parameters, List<Node> body) {
        this.locals = locals;
        this.parameters = parameters;
        this.body = body;
    }

    public HashPMap<NamedNode, Object> getLocals() {
        return locals;
    }

    public List<ParameterNode> getParameters() {
        return parameters;
    }

    public List<Node> getBody() {
        return body;
    }
}
