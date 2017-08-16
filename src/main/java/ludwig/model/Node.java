package ludwig.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.util.ArrayList;
import java.util.List;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME)
public abstract class Node<T extends Node> {
    private String id;
    private String comment;
    @JsonIgnore
    private Node parent;
    private final List<Node<?>> children = new ArrayList<>();
    @JsonIgnore
    private boolean deleted;

    public abstract <T> T accept(NodeVisitor<T> visitor);

    public String id() {
        return id;
    }

    public T id(String id) {
        this.id = id;
        return (T) this;
    }

    public String comment() {
        return comment;
    }

    public T comment(String comment) {
        this.comment = comment;
        return (T) this;
    }

    public List<Node<?>> children() {
        return children;
    }

    public T add(Node child) {
        children.add(child);
        child.parent = this;
        return (T) this;
    }

    public Node<?> parent() {
        return parent;
    }

    public T parent(Node<?> parent) {
        this.parent = parent;
        return (T) this;
    }

    public <T extends Node> T parentOfType(Class<T> type) {
        Node n = this;
        while (n != null && !type.isInstance(n)) {
            n = n.parent;
        }
        return (T) n;
    }

    public boolean deleted() {
        return deleted;
    }

    public void delete() {
        this.deleted = true;
    }

    @JsonIgnore
    public boolean isOrdered() {
        return true;
    }
}
