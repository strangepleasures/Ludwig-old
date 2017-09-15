package ludwig.interpreter

import ludwig.model.FunctionNode
import ludwig.model.Node
import ludwig.utils.NodeUtils
import org.pcollections.HashTreePMap


class CallableRef(private val function: Node<*>) : Callable {
    private val argCount: Int

    init {
        argCount = NodeUtils.arguments(function).size // TODO: Optimize
    }

    override fun tail(args: Array<Any?>): Any? {
        return Evaluator(HashTreePMap.empty()).tail(function, args)
    }

    override val isLazy: Boolean
        get() = function is FunctionNode && function.lazy

    override fun argCount(): Int {
        return argCount
    }

    override fun toString(): String {
        return "ref " + function
    }
}
