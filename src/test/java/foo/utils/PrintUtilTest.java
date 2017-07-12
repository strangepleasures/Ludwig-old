package foo.utils;

import foo.model.*;
import org.junit.Test;

import static org.junit.Assert.*;

public class PrintUtilTest {

    @Test
    public void testToString() {
        PackageNode packageNode = new PackageNode();
        packageNode.setName("mypackage");
        FunctionNode functionNode = new FunctionNode();
        functionNode.setName("foo");
        ParameterNode parameterNode1 = new ParameterNode();
        parameterNode1.setName("x");
        functionNode.getParameters().add(parameterNode1);
        ParameterNode parameterNode2 = new ParameterNode();
        parameterNode2.setName("y");
        functionNode.getParameters().add(parameterNode2);
        CallNode callNode = new CallNode();
        callNode.setFunction(functionNode);
        RefNode refNode1 = new RefNode();
        refNode1.setNode(parameterNode2);
        RefNode refNode2 = new RefNode();
        refNode2.setNode(parameterNode1);
        callNode.getArguments().put(parameterNode1, refNode1);
        callNode.getArguments().put(parameterNode2, refNode2);
        functionNode.getBody().add(callNode);
        packageNode.getItems().add(functionNode);
        assertEquals(
            "package mypackage\n" +
            "  def foo [x y]\n" +
            "    foo\n" +
            "      x: y\n" +
            "      y: x\n",
            PrintUtil.toString(packageNode));
    }

}