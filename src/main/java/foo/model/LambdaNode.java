package foo.model;

import java.util.ArrayList;
import java.util.List;

public class LambdaNode extends Node {
    private final List<ParameterNode> parameters = new ArrayList<>();
    private final List<Node> body = new ArrayList<>();

    public List<ParameterNode> getParameters() {
        return parameters;
    }

    public List<Node> getBody() {
        return body;
    }

    @Override
    public <T> T accept(NodeVisitor<T> visitor) {
        return visitor.visitLambda(this);
    }
}
