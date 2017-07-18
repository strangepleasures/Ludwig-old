package foo.model;

import java.util.ArrayList;
import java.util.List;


public class FunctionNode extends NamedNode implements Signature {
    private final List<ParameterNode> parameters = new ArrayList<>();

    @Override
    public List<ParameterNode> parameters() {
        return parameters;
    }

    @Override
    public <T> T accept(NodeVisitor<T> visitor) {
        return visitor.visitFunction(this);
    }
}
