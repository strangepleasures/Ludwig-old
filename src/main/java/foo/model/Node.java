package foo.model;

import lombok.*;

@Getter
@Setter
public abstract class Node {
    String id;
    String comment;

    public abstract <T> T accept(NodeVisitor<T> visitor);
}
