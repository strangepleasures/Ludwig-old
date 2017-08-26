package ludwig.ide;

import javafx.scene.control.*;
import ludwig.changes.*;
import ludwig.model.*;
import ludwig.workspace.Workspace;

import static java.util.Collections.singletonList;
import static ludwig.utils.NodeUtils.isReadonly;

class PackageTreeView extends TreeView<NamedNode> {
    private final Workspace workspace;

    PackageTreeView(Workspace workspace) {
        super(createRoot(workspace));
        this.workspace = workspace;
        setShowRoot(false);

        setContextMenu(ContextMenuFactory.menu(new Actions()));

        setPrefWidth(120);
    }

    private static TreeItem<NamedNode> createRoot(Workspace workspace) {
        TreeItem<NamedNode> root = new TreeItem<>();

        for (ProjectNode projectNode : workspace.getProjects()) {
            TreeItem<NamedNode> projectItem = new TreeItem<>(projectNode);

            root.getChildren().add(projectItem);

            for (Node packageNode : projectNode.children()) {
                processPackage(projectItem, (PackageNode) packageNode);
            }
        }

        return root;
    }

    private static void processPackage(TreeItem<NamedNode> parent, PackageNode packageNode) {
        TreeItem<NamedNode> packageItem = new TreeItem<>(packageNode);
        parent.getChildren().add(packageItem);

        for (Node node : packageNode.children()) {
            if (node instanceof PackageNode) {
                processPackage(packageItem, (PackageNode) node);
            }
        }
    }


    void select(NamedNode packageNode) {
        getSelectionModel().select(find(packageNode));
    }

    private TreeItem<NamedNode> find(NamedNode node) {
        if (node == null) {
            return null;
        }
        TreeItem<NamedNode> parentItem = node.parent() == null ? getRoot() : find((NamedNode) node.parent());
        return parentItem.getChildren()
            .stream()
            .filter(i -> i.getValue() == node)
            .findFirst()
            .orElse(null);
    }

    void refresh() {
        setRoot(createRoot(workspace));
    }

    public class Actions {
        public void newPackage() {
            TreeItem<NamedNode> selectedItem = getSelectionModel().getSelectedItem();
            if (selectedItem != null) {
                Node parent = selectedItem.getValue();

                if (isReadonly(parent)) {
                    return;
                }

                TextInputDialog dialog = new TextInputDialog();
                dialog.setTitle("Add a package");
                dialog.setHeaderText("");
                dialog.setContentText("Package name");

                dialog.showAndWait().ifPresent(name -> {
                    PackageNode packageNode = new PackageNode().name(name).id(Change.newId());
                    InsertNode insert = new InsertNode()
                        .node(packageNode)
                        .parent(parent.id());
                    workspace.apply(singletonList(insert));
                    select(packageNode);
                });
            }
        }

        public void deletePackage() {
            TreeItem<NamedNode> selectedItem = getSelectionModel().getSelectedItem();
            if (selectedItem != null) {
                Node packageNode = selectedItem.getValue();
                if (isReadonly(packageNode)) {
                    return;
                }
                NamedNode parent = (NamedNode) packageNode.parent();
                workspace.apply(singletonList(new Delete().id(packageNode.id())));
                select(parent);
            }
        }
    }
}
