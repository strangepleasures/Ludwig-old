package foo.interpreter;

import foo.model.*;
import org.junit.Test;

import static org.junit.Assert.*;

public class InterpreterTest {
    private Interpreter interpreter = new Interpreter();

    @Test
    public void testSimpleFunction() {
        FunctionNode functionNode = new FunctionNode();
        functionNode.setName("foo");
        ParameterNode parameterNode1 = new ParameterNode();
        parameterNode1.setName("x");
        functionNode.getParameters().add(parameterNode1);
        ParameterNode parameterNode2 = new ParameterNode();
        parameterNode2.setName("y");
        functionNode.getParameters().add(parameterNode2);
        CallNode callNode = new CallNode();
        callNode.setFunction(SystemPackage.MINUS);
        RefNode refNode1 = new RefNode();
        refNode1.setNode(parameterNode1);
        RefNode refNode2 = new RefNode();
        refNode2.setNode(parameterNode2);
        callNode.getArguments().put(SystemPackage.MINUS.getParameters().get(0), refNode1);
        callNode.getArguments().put(SystemPackage.MINUS.getParameters().get(1), refNode2);
        functionNode.getBody().add(callNode);


        Object result = interpreter.call(functionNode, 50.0, 8.0);
        assertEquals(42.0, result);
    }

}