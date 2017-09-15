package ludwig.utils

import ludwig.model.FunctionNode
import ludwig.model.PackageNode
import ludwig.model.ReferenceNode
import ludwig.model.VariableNode
import org.junit.Test

class PrintUtilTest {

    @Test
    fun testToString() {
        val packageNode = PackageNode().name("mypackage")
        val functionNode = FunctionNode().name("foo")
        val variableNode1 = VariableNode().name("x")
        functionNode.add(variableNode1)
        val variableNode2 = VariableNode()
        variableNode2.name("y")
        functionNode.add(variableNode2)
        val head = ReferenceNode(functionNode)
        val referenceNode1 = ReferenceNode(variableNode2)
        val referenceNode2 = ReferenceNode(variableNode1)
        head.add(referenceNode1)
        head.add(referenceNode2)
        functionNode.add(head)
        packageNode.add(functionNode)
    }

}