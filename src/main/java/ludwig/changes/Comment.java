package ludwig.changes;

public class Comment extends Change<Comment> {
    private String nodeId;
    private String comment;

    public String nodeId() {
        return nodeId;
    }

    public Comment nodeId(String nodeId) {
        this.nodeId = nodeId;
        return this;
    }

    public String comment() {
        return comment;
    }

    public Comment comment(String comment) {
        this.comment = comment;
        return this;
    }

    @Override
    public <T> T accept(ChangeVisitor<T> visitor) {
        return visitor.visitComment(this);
    }
}
