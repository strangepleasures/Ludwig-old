package foo.model;

public abstract class Node {
    private String id;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public abstract <T> T accept(NodeVisitor<T> visitor);
}
