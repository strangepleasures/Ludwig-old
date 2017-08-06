package ludwig.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.util.ArrayList;
import java.util.List;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME)
public abstract class Node {

    private String id;
    private String comment;
    @JsonIgnore
    private Node parent;
    final List<Node> children = new ArrayList<>();

    public abstract <T> T accept(NodeVisitor<T> visitor);

    public String id() {
        return id;
    }

    public Node id(String id) {
        this.id = id;
        return this;
    }

    public String getComment() {
        return comment;
    }

    public Node setComment(String comment) {
        this.comment = comment;
        return this;
    }

    public List<Node> children() {
        return children;
    }

    public Node add(Node child) {
        children.add(child);
        child.parent = this;
        return this;
    }

    public Node parent() {
        return parent;
    }

    public Node parent(Node parent) {
        this.parent = parent;
        return this;
    }

    public <T extends Node> T parentOfType(Class<T> type) {
        Node n = this;
        while (n != null && !type.isInstance(n)) {
            n = n.parent;
        }
        return (T) n;
    }
}
