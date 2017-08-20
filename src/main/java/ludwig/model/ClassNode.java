package ludwig.model;

public class ClassNode extends NamedNode<ClassNode>  {
    @Override
    public <T> T accept(NodeVisitor<T> visitor) {
        return visitor.visitClass(this);
    }
}
