package foo.ide.swing;

import foo.model.*;

import javax.swing.*;
import java.util.Comparator;

class FunctionListModel extends DefaultListModel<NamedNode> {
    FunctionListModel(PackageNode sel) {
        sel.children()
            .stream()
            .filter(FunctionNode.class::isInstance)
            .map(n -> (NamedNode) n)
            .sorted(Comparator.comparing(NamedNode::getName))
            .forEach(child -> addElement(child));
    }
}
