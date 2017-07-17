package foo.model;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public abstract class Node {
    String id;
    String comment;
    final List<Node> nodes = new ArrayList<>();

    public abstract <T> T accept(NodeVisitor<T> visitor);
}
