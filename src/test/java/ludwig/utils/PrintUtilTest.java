package ludwig.utils;

import ludwig.model.*;
import org.junit.Test;

public class PrintUtilTest {

    @Test
    public void testToString() {
        Node packageNode = new PackageNode().setName("mypackage");
        FunctionNode functionNode = (FunctionNode) new FunctionNode().setName("foo");
        ParameterNode parameterNode1 = (ParameterNode) new ParameterNode().setName("x");
        functionNode.parameters().add(parameterNode1);
        ParameterNode parameterNode2 = new ParameterNode();
        parameterNode2.setName("y");
        functionNode.parameters().add(parameterNode2);
        BoundCallNode boundCallNode = new BoundCallNode();
        boundCallNode.add(functionNode);
        VariableNode variableNode1 = new VariableNode(parameterNode2);
        VariableNode variableNode2 = new VariableNode(parameterNode1);
        boundCallNode.arguments().put(parameterNode1, variableNode1);
        boundCallNode.arguments().put(parameterNode2, variableNode2);
        functionNode.add(boundCallNode);
        packageNode.add(functionNode);
//        assertEquals(
//            "package mypackage\n" +
//                "\tdef foo [x y]\n" +
//                "\t\tfoo\n" +
//                "\t\t\tx: y\n" +
//                "\t\t\ty: x\n",
//            PrintUtil.toString(packageNode));
    }

}