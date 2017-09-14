package ludwig.ide;

import com.sun.javafx.scene.control.skin.TreeViewSkin;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import ludwig.model.*;
import ludwig.workspace.Environment;

import java.util.List;


public class CodeTreeView extends TreeView<Node> {
    private final Environment environment;

    public CodeTreeView(Environment environment) {
        super(new TreeItem<>(new ListNode()));
        this.environment = environment;
        setShowRoot(false);
    }

    public void setNode(Node<?> node) {
        getRoot().getChildren().clear();

        if (node instanceof FunctionNode) {
            for (int i = 0; i < node.children().size(); i++) {
                if (!(node.children().get(i) instanceof  VariableNode)) {
                    setContent(getRoot(), node.children().subList(i, node.children().size()));
                    break;
                }
            }
        } else if (node instanceof OverrideNode) {
            for (int i = 1; i < node.children().size(); i++) {
                if (!(node.children().get(i) instanceof  VariableNode)) {
                    setContent(getRoot(), node.children().subList(i, node.children().size()));
                    break;
                }
            }
        }
    }

    private void setContent(TreeItem<Node> parent, List<Node<?>> nodes) {
        for (Node<?> node: nodes) {
            TreeItem<Node> item = new TreeItem<>(node);
            parent.getChildren().add(item);
            item.setExpanded(true);
            setContent(item, node.children());
        }
    }

    public Node<?> selectedNode() {
        TreeItem<Node> selectedItem = getSelectionModel().getSelectedItem();
        return selectedItem != null ? selectedItem.getValue() : null;
    }

    public void locate(Node<?> node) {

    }
}
