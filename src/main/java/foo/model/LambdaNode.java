package foo.model;

import lombok.*;

import java.security.*;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class LambdaNode extends Node implements Signature {
    private final List<ParameterNode> parameters = new ArrayList<>();
    private final List<Node> items = new ArrayList<>();

    @Override
    public <T> T accept(NodeVisitor<T> visitor) {
        return visitor.visitLambda(this);
    }
}
