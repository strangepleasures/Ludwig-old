package ludwig.utils;

import ludwig.model.*;
import org.junit.Test;

public class PrintUtilTest {

    @Test
    public void testToString() {
        Node packageNode = new PackageNode().setName("mypackage");
        FunctionNode functionNode = (FunctionNode) new FunctionNode().setName("foo");
        ParameterNode parameterNode1 = (ParameterNode) new ParameterNode().setName("x");
        functionNode.add(parameterNode1);
        ParameterNode parameterNode2 = new ParameterNode();
        parameterNode2.setName("y");
        functionNode.add(parameterNode2);
        VariableNode head = new VariableNode(functionNode);
        VariableNode variableNode1 = new VariableNode(parameterNode2);
        VariableNode variableNode2 = new VariableNode(parameterNode1);
        head.add(variableNode1);
        head.add(variableNode2);
        functionNode.add(head);
        packageNode.add(functionNode);
    }

}