package ludwig.interpreter;

import ludwig.model.*;
import ludwig.runtime.StdLib;
import org.junit.Ignore;
import org.junit.Test;
import org.pcollections.HashTreePMap;

import java.util.HashMap;

import static org.junit.Assert.assertEquals;

public class InterpreterTest {
    private SystemPackage systemPackage = new SystemPackage(StdLib.class);

    @Test
    public void testSimpleFunction() {
        FunctionNode functionNode = new FunctionNode();
        functionNode.setName("foo");
        VariableNode variableNode1 = new VariableNode();
        variableNode1.setName("x");
        functionNode.add(variableNode1);
        VariableNode variableNode2 = new VariableNode();
        variableNode2.setName("y");
        functionNode.add(variableNode2);
        functionNode.add(new SeparatorNode());

        FunctionNode minus = (FunctionNode) systemPackage.item("-");
        ReferenceNode head = new ReferenceNode(minus);
        ReferenceNode referenceNode1 = new ReferenceNode(variableNode1);
        ReferenceNode referenceNode2 = new ReferenceNode(variableNode2);
        head.add(referenceNode1);
        head.add(referenceNode2);
        functionNode.add(head);


        Object result = Interpreter.call(functionNode, 50.0, 8.0);
        assertEquals(42.0, result);
    }

    @Test
    public void testClosure() {
        LambdaNode lambda = new LambdaNode();

        lambda.add(new VariableNode());
        lambda.add(new SeparatorNode());
        FunctionNode plus = (FunctionNode) systemPackage.item("+");
        ReferenceNode head = new ReferenceNode(plus);
        ReferenceNode referenceNode = new ReferenceNode((NamedNode) lambda.children().get(0));
        head.add(referenceNode);
        head.add(LiteralNode.ofValue(3.0));
        lambda.add(head);

        CallNode ucn = new CallNode();
        ucn.add(lambda);
        ucn.add(LiteralNode.ofValue(2.0));

        Object result = Interpreter.eval(ucn, HashTreePMap.empty(), new HashMap<>());
        assertEquals(5.0, result);
    }


    @Test
    @Ignore
    public void testDelayed() {
        FunctionNode or = (FunctionNode) systemPackage.item("or");
        assertEquals(true, Interpreter.call(or, true, true));
        assertEquals(true, Interpreter.call(or, true, false));
        assertEquals(true, Interpreter.call(or, false, true));
        assertEquals(false, Interpreter.call(or, false, false));
        assertEquals(true, Interpreter.call(or, true, null));
    }
}