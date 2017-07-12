package foo.model;

import java.util.*;

public class PackageNode extends NamedNode {
    private final SortedSet<NamedNode> items = new TreeSet<>(Comparator.comparing(NamedNode::getName));

    public SortedSet<NamedNode> getItems() {
        return items;
    }

    @Override
    public <T> T accept(NodeVisitor<T> visitor) {
        return visitor.visitPackage(this);
    }
}
