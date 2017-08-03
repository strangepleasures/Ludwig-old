package ludwig.model;

public class LambdaNode extends Node {

    @Override
    public <T> T accept(NodeVisitor<T> visitor) {
        return visitor.visitLambda(this);
    }

    @Override
    public String toString() {
        return "λ";
    }
}
