package foo.model;

import lombok.*;

@Data
public abstract class Node {
    String id;
    String comment;

    public abstract <T> T accept(NodeVisitor<T> visitor);
}
