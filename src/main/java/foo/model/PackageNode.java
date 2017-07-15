package foo.model;

import lombok.Data;

import java.util.*;

@Data
public class PackageNode extends NamedNode {
    private final SortedSet<NamedNode> items = new TreeSet<>(Comparator.comparing(NamedNode::getName));

    @Override
    public <T> T accept(NodeVisitor<T> visitor) {
        return visitor.visitPackage(this);
    }

    public NamedNode item(String name) {
        return items.stream().filter(it -> it.getName().equals(name)).findFirst().orElse(null);
    }
}
