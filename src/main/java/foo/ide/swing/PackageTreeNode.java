package foo.ide.swing;

import foo.model.NamedNode;
import foo.model.PackageNode;

import javax.swing.tree.DefaultMutableTreeNode;
import java.util.Comparator;

class PackageTreeNode extends DefaultMutableTreeNode {
    PackageTreeNode(PackageNode p) {
        setUserObject(p);
        p.children()
            .stream()
            .filter(PackageNode.class::isInstance)
            .map(n -> (NamedNode) n)
            .sorted(Comparator.comparing(NamedNode::getName))
            .forEach(child -> add(new PackageTreeNode((PackageNode) child)));
    }
}
