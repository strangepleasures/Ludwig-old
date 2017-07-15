package foo.model;

import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class FunctionNode extends NamedNode implements ListLike {
    private final List<ParameterNode> parameters = new ArrayList<>();
    private final List<Node> items = new ArrayList<>();

    @Override
    public <T> T accept(NodeVisitor<T> visitor) {
        return visitor.visitFunction(this);
    }
}
