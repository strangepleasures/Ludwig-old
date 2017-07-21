package foo.utils;

import foo.model.*;
import org.junit.Test;

import static org.junit.Assert.*;

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
        boundCallNode.children().add(functionNode);
        RefNode refNode1 = new RefNode(parameterNode2);
        RefNode refNode2 = new RefNode(parameterNode1);
        boundCallNode.arguments().put(parameterNode1, refNode1);
        boundCallNode.arguments().put(parameterNode2, refNode2);
        functionNode.children().add(boundCallNode);
        packageNode.children().add(functionNode);
//        assertEquals(
//            "package mypackage\n" +
//                "\tdef foo [x y]\n" +
//                "\t\tfoo\n" +
//                "\t\t\tx: y\n" +
//                "\t\t\ty: x\n",
//            PrintUtil.toString(packageNode));
    }

}