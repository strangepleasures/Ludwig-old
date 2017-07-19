package foo.ide.swing;

import foo.workspace.Workspace;

import javax.swing.tree.DefaultTreeModel;

class PackageTreeModel extends DefaultTreeModel {
    PackageTreeModel(Workspace workspace) {
        super(new WorkspaceTreeNode(workspace));
    }
}
