package ludwig.model;

import java.util.ArrayList;
import java.util.List;


public class FunctionNode extends NamedNode<FunctionNode> {
    @Override
    public <T> T accept(NodeVisitor<T> visitor) {
        return visitor.visitFunction(this);
    }
}
