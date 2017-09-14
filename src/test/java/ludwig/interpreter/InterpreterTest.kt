package ludwig.interpreter

import ludwig.model.*
import ludwig.runtime.StdLib
import ludwig.script.Parser
import org.junit.Ignore
import org.junit.Test
import org.pcollections.HashTreePMap

import org.junit.Assert.assertEquals

class InterpreterTest {
    private val systemPackage = Builtins.of(StdLib::class.java)

    @Test
    fun testSimpleFunction() {
        val functionNode = FunctionNode()
        functionNode.name("foo")
        val variableNode1 = VariableNode()
        variableNode1.name("x")
        functionNode.add(variableNode1)
        val variableNode2 = VariableNode()
        variableNode2.name("y")
        functionNode.add(variableNode2)

        val minus = Parser.item(systemPackage, "-") as FunctionNode?
        val head = ReferenceNode(minus!!)
        val referenceNode1 = ReferenceNode(variableNode1)
        val referenceNode2 = ReferenceNode(variableNode2)
        head.add(referenceNode1)
        head.add(referenceNode2)
        functionNode.add(head)


        val result = Interpreter.call(functionNode, 50.0, 8.0)
        assertEquals(42.0, result)
    }

    @Test
    fun testClosure() {
        val lambda = LambdaNode()

        lambda.add(VariableNode())
        val plus = Parser.item(systemPackage, "+") as FunctionNode?
        val head = ReferenceNode(plus!!)
        val referenceNode = ReferenceNode(lambda.children()[0])
        head.add(referenceNode)
        head.add(LiteralNode.ofValue(3.0))
        lambda.add(head)

        val ucn = CallNode()
        ucn.add(lambda)
        ucn.add(LiteralNode.ofValue(2.0))

        val result = Interpreter.eval(ucn, HashTreePMap.empty())
        assertEquals(5.0, result)
    }


    @Test
    @Ignore
    fun testDelayed() {
        val or = Parser.item(systemPackage, "or") as FunctionNode?
        assertEquals(true, Interpreter.call(or, true, true))
        assertEquals(true, Interpreter.call(or, true, false))
        assertEquals(true, Interpreter.call(or, false, true))
        assertEquals(false, Interpreter.call(or, false, false))
        assertEquals(true, Interpreter.call(or, true, null))
    }
}