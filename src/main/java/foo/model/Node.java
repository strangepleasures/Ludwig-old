package foo.model;

public abstract class Node {
    public abstract <T> T accept(NodeVisitor<T> visitor);
}
