package ludwig.utils;

import ludwig.model.*;
import org.junit.Test;

public class PrintUtilTest {

    @Test
    public void testToString() {
        Node packageNode = new PackageNode().name("mypackage");
        FunctionNode functionNode = new FunctionNode().name("foo");
        VariableNode variableNode1 = new VariableNode().name("x");
        functionNode.add(variableNode1);
        VariableNode variableNode2 = new VariableNode();
        variableNode2.name("y");
        functionNode.add(variableNode2);
        ReferenceNode head = new ReferenceNode(functionNode);
        ReferenceNode referenceNode1 = new ReferenceNode(variableNode2);
        ReferenceNode referenceNode2 = new ReferenceNode(variableNode1);
        head.add(referenceNode1);
        head.add(referenceNode2);
        functionNode.add(head);
        packageNode.add(functionNode);
    }

}