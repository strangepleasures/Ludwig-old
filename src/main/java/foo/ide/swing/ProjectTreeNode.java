package foo.ide.swing;

import foo.model.*;

import javax.swing.tree.DefaultMutableTreeNode;
import java.util.Comparator;

class ProjectTreeNode extends DefaultMutableTreeNode {
    ProjectTreeNode(ProjectNode project) {
        setUserObject(project);
        project.children()
            .stream()
            .map(n -> (NamedNode) n)
            .sorted(Comparator.comparing(NamedNode::name))
            .forEach(p -> add(new PackageTreeNode((PackageNode) p)));
    }
}
