package ludwig.changes;

public class Comment extends Change<Comment> {
    private String nodeId;
    private String comment;

    public String getNodeId() {
        return nodeId;
    }

    public Comment setNodeId(String nodeId) {
        this.nodeId = nodeId;
        return this;
    }

    public String getComment() {
        return comment;
    }

    public Comment setComment(String comment) {
        this.comment = comment;
        return this;
    }

    @Override
    public <T> T accept(ChangeVisitor<T> visitor) {
        return visitor.visitComment(this);
    }
}
