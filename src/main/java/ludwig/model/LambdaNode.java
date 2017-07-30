package ludwig.model;

import java.util.ArrayList;
import java.util.List;

public class LambdaNode extends Node implements Signature {
    private final List<ParameterNode> parameters = new ArrayList<>();

    @Override
    public List<ParameterNode> parameters() {
        return parameters;
    }

    @Override
    public <T> T accept(NodeVisitor<T> visitor) {
        return visitor.visitLambda(this);
    }
}
