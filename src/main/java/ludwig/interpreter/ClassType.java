package ludwig.interpreter;

import ludwig.model.*;
import org.pcollections.TreePVector;

import java.util.HashMap;
import java.util.Map;

public class ClassType {
    private static final Map<ClassNode, ClassType> types = new HashMap<>();
    private static final Map<String, ClassType> typesByName = new HashMap<>();

    private final String name;
    private final ClassType superClass;
    private final TreePVector<VariableNode> fields;
    private final Map<Node, Node> overrides = new HashMap<>();

    public static ClassType of(ClassNode node) {
        return types.computeIfAbsent(node, ClassType::new);
    }

    private ClassType(ClassNode node) {
        this.name = node.name();
        this.superClass = node.children().isEmpty() ? null : of((ClassNode) ((ReferenceNode) node.children().get(0)).ref());
        TreePVector<VariableNode> fields = superClass != null ? superClass.fields : TreePVector.empty();
        for (int i  = 1; i < node.children().size(); i++) {
            fields = fields.plus((VariableNode) node.children().get(i));
        }
        this.fields = fields;
        typesByName.put(node.parent() + ":" + node.name(), this);
    }

    public Node implementation(Node signature) {
        return overrides.getOrDefault(signature, signature);
    }

    public TreePVector<VariableNode> fields() {
        return fields;
    }

    public Map<Node, Node> overrides() {
        return overrides;
    }

    public ClassType superClass() {
        return superClass;
    }

    @Override
    public String toString() {
        return name;
    }

    public static ClassType byName(String name) {
        return typesByName.get(name);
    }
}
