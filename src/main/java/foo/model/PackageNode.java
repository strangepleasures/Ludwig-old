package foo.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PackageNode extends NamedNode {
    @Override
    public <T> T accept(NodeVisitor<T> visitor) {
        return visitor.visitPackage(this);
    }

    public NamedNode item(String name) {
        return getChildren().stream().map(n -> (NamedNode) n).filter(it -> it.getName().equals(name)).findFirst().orElse(null);
    }
}
