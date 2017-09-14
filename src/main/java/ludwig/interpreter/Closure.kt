package ludwig.interpreter

import ludwig.model.*
import ludwig.utils.PrettyPrinter
import org.pcollections.HashPMap

class Closure(private val locals: HashPMap<NamedNode<*>, Any>, private val lambda: LambdaNode) : Callable {
    private var argCount: Int = 0

    init {

        for (i in 0..lambda.children().size - 1) {
            val node = lambda.children()[i]
            if (node !is VariableNode) {
                argCount = i
                break
            }
        }
    }

    override fun tail(args: Array<Any?>): Any? {
        var env: HashPMap<NamedNode<*>, Any> = locals
        var visitor: Evaluator? = null
        var result: Any? = null

        for (i in 0..lambda.children().size - 1) {
            val node = lambda.children()[i]
            if (node is VariableNode) {
                env = env.plus(node as NamedNode<*>, args[i])
            } else {
                if (visitor == null) {
                    visitor = Evaluator(env)
                }
                result = node.accept(visitor)
                if (result is Signal) {
                    break
                }
            }
        }

        return result
    }

    override fun argCount(): Int {
        return argCount
    }

    override fun toString(): String {
        return PrettyPrinter.print(lambda)
    }
}
