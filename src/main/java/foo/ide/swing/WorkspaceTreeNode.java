package foo.ide.swing;

import foo.model.NamedNode;
import foo.workspace.Workspace;

import javax.swing.tree.DefaultMutableTreeNode;
import java.util.Comparator;

class WorkspaceTreeNode extends DefaultMutableTreeNode {
    WorkspaceTreeNode(Workspace workspace) {
        setUserObject(workspace);
        workspace.getProjects()
        .stream()
        .sorted(Comparator.comparing(NamedNode::name))
        .forEach(project -> add(new ProjectTreeNode(project)));
    }
}
