package foo.ide;

import foo.model.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.TreeItem;

import java.util.stream.Collectors;

public class TreeNode extends TreeItem<Node> {

    private boolean childrenAdded;

    public TreeNode(Node value) {
        super(value);
    }

    @Override
    public boolean isLeaf() {
        return false;/// getValue() == null || getValue().accept(IsLeafVisitor.INSTANCE);
    }

    @Override
    public ObservableList<TreeItem<Node>> getChildren() {

        ObservableList<TreeItem<Node>> children = super.getChildren();

        if (!childrenAdded) {
            children.clear();
            getValue().children().forEach(child -> children.add(new TreeNode(child)));
            childrenAdded = true;
        }

        return children;
    }
}
