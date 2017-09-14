package ludwig.ide;

import javafx.scene.layout.VBox;
import ludwig.model.NamedNode;
import ludwig.model.Node;
import ludwig.model.OverrideNode;
import ludwig.model.VariableNode;
import ludwig.utils.NodeUtils;
import ludwig.workspace.Environment;


public class SignatureEditor extends VBox {
    private final Environment environment;
    private Node<?> node;

    public SignatureEditor(Environment environment) {
        this.environment = environment;
    }

    public void setNode(Node<?> node) {
        this.node = node;

        getChildren().clear();

        NamedNode<?> decl = (node instanceof OverrideNode) ? NodeUtils.INSTANCE.declaration((OverrideNode) node) : (NamedNode) node;
        if (node == null) {
            return;
        }

        getChildren().add(new SignatureItemEditor(environment, decl));

        for (Node n : decl.children()) {
            if (!(n instanceof VariableNode)) {
                break;
            }
            getChildren().add(new SignatureItemEditor(environment, (NamedNode<?>) n));
        }

    }
}
