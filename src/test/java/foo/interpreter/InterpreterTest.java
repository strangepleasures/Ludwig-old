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
        functionNode.parameters().add(parameterNode1);
        ParameterNode parameterNode2 = new ParameterNode();
        parameterNode2.setName("y");
        functionNode.parameters().add(parameterNode2);
        BoundCallNode boundCallNode = new BoundCallNode();
        FunctionNode minus = (FunctionNode) systemPackage.item("-");
        boundCallNode.add(minus);
        RefNode refNode1 = new RefNode(parameterNode1);
        RefNode refNode2 = new RefNode(parameterNode2);
        boundCallNode.arguments().put(minus.parameters().get(0), refNode1);
        boundCallNode.arguments().put(minus.parameters().get(1), refNode2);
        functionNode.add(boundCallNode);


        Object result = Interpreter.call(functionNode, 50.0, 8.0);
        assertEquals(42.0, result);
    }

    @Test
    public void testClosure() {
        LambdaNode lambda = new LambdaNode();

        lambda.parameters().add(new ParameterNode());
        FunctionNode plus = (FunctionNode) systemPackage.item("+");
        BoundCallNode bcn = new BoundCallNode();
        bcn.add(plus);
        RefNode refNode = new RefNode(lambda.parameters().get(0));
        bcn.arguments().put(plus.parameters().get(0), refNode);
        bcn.arguments().put(plus.parameters().get(1), LiteralNode.ofValue(3.0));
        lambda.add(bcn);

        UnboundCallNode ucn = new UnboundCallNode();
        ucn.add(lambda);
        ucn.add(LiteralNode.ofValue(2.0));

        Object result = Interpreter.eval(ucn, HashTreePMap.empty());
        assertEquals(5.0, result);
    }

}