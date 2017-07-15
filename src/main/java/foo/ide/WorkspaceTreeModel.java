package foo.ide;

import foo.model.*;
import foo.workspace.Workspace;
import javafx.scene.control.TreeItem;

public class WorkspaceTreeModel {
    private final Workspace workspace;
    private final TreeItem<NamedNode> root = new TreeItem<>();


    public WorkspaceTreeModel(Workspace workspace) {
        this.workspace = workspace;
        init();
    }

    public TreeItem<NamedNode> getWorkspaceRoot() {
        return root;
    }

    public void init() {
        root.getChildren().clear();

        for (ProjectNode projectNode: workspace.getProjects()) {
            TreeItem<NamedNode> projectItem = new TreeItem<NamedNode>(projectNode) {
                @Override
                public String toString() {
                    return projectNode.getName();
                }
            };

            root.getChildren().add(projectItem);

            for (PackageNode packageNode: projectNode.getPackages()) {
                processPackage(projectItem, packageNode);
            }
        }
    }

    private void processPackage(TreeItem<NamedNode> parent, PackageNode packageNode) {
        TreeItem<NamedNode> packageItem = new TreeItem<NamedNode>(packageNode) {
            @Override
            public String toString() {
                return packageNode.getName();
            }
        };
        parent.getChildren().add(packageItem);

        for (Node node: packageNode.getItems()) {
            if (node instanceof PackageNode) {
                processPackage(packageItem, (PackageNode) node);
            }
        }
    }
}
