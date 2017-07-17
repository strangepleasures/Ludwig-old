package foo.interpreter;

import foo.model.*;
import foo.runtime.StdLib;
import org.junit.Test;
import org.pcollections.HashTreePMap;

import static org.junit.Assert.*;

public class InterpreterTest {
    private SystemPackage systemPackage = new SystemPackage(StdLib.class);

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
        BoundCallNode boundCallNode = new BoundCallNode();
        FunctionNode minus = (FunctionNode) systemPackage.item("-");
        boundCallNode.getChildren().add(minus);
        RefNode refNode1 = new RefNode();
        refNode1.getChildren().add(parameterNode1);
        RefNode refNode2 = new RefNode();
        refNode2.getChildren().add(parameterNode2);
        boundCallNode.getArguments().put(minus.getParameters().get(0), refNode1);
        boundCallNode.getArguments().put(minus.getParameters().get(1), refNode2);
        functionNode.getChildren().add(boundCallNode);


        Object result = Interpreter.call(functionNode, 50.0, 8.0);
        assertEquals(42.0, result);
    }

    @Test
    public void testClosure() {
        LambdaNode lambda = new LambdaNode();

        lambda.getParameters().add(new ParameterNode());
        FunctionNode plus = (FunctionNode) systemPackage.item("+");
        BoundCallNode bcn = new BoundCallNode();
        bcn.getChildren().add(plus);
        RefNode refNode = new RefNode();
        refNode.getChildren().add(lambda.getParameters().get(0));
        bcn.getArguments().put(plus.getParameters().get(0), refNode);
        bcn.getArguments().put(plus.getParameters().get(1), LiteralNode.ofValue(3.0));
        lambda.getChildren().add(bcn);

        UnboundCallNode ucn = new UnboundCallNode();
        ucn.getChildren().add(lambda);
        ucn.getChildren().add(LiteralNode.ofValue(2.0));

        Object result = Interpreter.eval(ucn, HashTreePMap.empty());
        assertEquals(5.0, result);
    }

}