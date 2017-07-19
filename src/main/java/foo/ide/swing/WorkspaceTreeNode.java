package foo.ide.swing;

import foo.model.NamedNode;
import foo.model.ProjectNode;
import foo.workspace.Workspace;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;
import java.util.Comparator;

class WorkspaceTreeNode extends DefaultMutableTreeNode {
    WorkspaceTreeNode(Workspace workspace) {
        setUserObject(workspace);
        workspace.getProjects()
        .stream()
        .sorted(Comparator.comparing(NamedNode::getName))
        .forEach(project -> add(new ProjectTreeNode(project)));
    }
}
