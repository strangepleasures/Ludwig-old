package ludwig.interpreter

import ludwig.model.*
import org.pcollections.HashPMap
import org.pcollections.HashTreePMap

object Interpreter {
    fun eval(node: Node, locals: HashPMap<NamedNode, Any>): Any? {
        return node.accept(Evaluator(locals))
    }

    fun call(functionNode: FunctionNode, vararg args: Any?): Any? {
        val head = ReferenceNode(functionNode)

        for (arg in args) {
            head.children.add(LiteralNode.ofValue(arg!!))
        }

        return eval(head, HashTreePMap.empty())
    }

}
