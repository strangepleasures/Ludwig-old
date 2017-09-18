package ludwig.interpreter

import ludwig.model.NamedNode
import ludwig.model.Node
import org.pcollections.HashPMap

class Return<T>(var node: Node, var locals: HashPMap<NamedNode, Any>) : Signal, Delayed<T> {

    override fun get(): T {
        var n: Node = node
        var l: HashPMap<NamedNode, Any> = locals

        while (true) {
            val r = Evaluator(l).eval(n)
            if (r is Return<*>) {
                n = r.node
                l = r.locals
            } else {
                return r as T
            }
        }
    }
}
