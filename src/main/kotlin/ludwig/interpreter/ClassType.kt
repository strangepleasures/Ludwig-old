package ludwig.interpreter

import ludwig.model.ClassNode
import ludwig.model.Node
import ludwig.model.ReferenceNode
import ludwig.model.VariableNode
import org.pcollections.TreePVector
import java.util.*

class ClassType private constructor(node: ClassNode) {

    private val name: String
    private val superClass: ClassType?
    private val fields: TreePVector<VariableNode>
    private val overrides = HashMap<Node, Node>()

    init {
        this.name = node.name
        this.superClass = if (node.children.isEmpty()) null else of((node.children[0] as ReferenceNode).ref as ClassNode)
        var fields = superClass?.fields ?: TreePVector.empty()
        for (i in 1..node.children.size - 1) {
            fields = fields.plus(node.children[i] as VariableNode)
        }
        this.fields = fields
        typesByName.put(node.parent.toString() + ":" + node.name, this)
    }

    fun implementation(signature: Node): Node {
        return (overrides as java.util.Map<Node, Node>).getOrDefault(signature, signature)
    }

    fun fields(): TreePVector<VariableNode> {
        return fields
    }

    fun overrides(): MutableMap<Node, Node> {
        return overrides
    }

    fun superClass(): ClassType? {
        return superClass
    }

    override fun toString(): String {
        return name
    }

    companion object {
        private val types = HashMap<ClassNode, ClassType>()
        private val typesByName = HashMap<String, ClassType>()

        fun of(node: ClassNode): ClassType {
            return (types as java.util.Map<ClassNode, ClassType>).computeIfAbsent(node, { ClassType(it) })
        }

        fun byName(name: String): ClassType? {
            return typesByName[name]
        }
    }
}
