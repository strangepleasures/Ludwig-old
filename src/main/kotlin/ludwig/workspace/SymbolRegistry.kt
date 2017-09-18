package ludwig.workspace


import ludwig.changes.Delete
import ludwig.changes.Rename
import ludwig.model.FunctionNode
import ludwig.model.NamedNode
import ludwig.model.Node
import ludwig.model.ProjectNode
import ludwig.utils.isField
import ludwig.utils.signature
import java.util.*
import java.util.function.Consumer


class SymbolRegistry(workspace: Workspace) {
    private val symbols = TreeSet(Comparator.comparing<Any, String>({ signature(it) }))
    private val nodesById = HashMap<String, Node>()

    init {
        workspace.changeListeners().add(changeListener { change ->
            if (change is Rename) {
                val node = workspace.node(change.nodeId)
                if (node is FunctionNode || isField(node)) {
                    symbols.add(node)
                    nodesById.put(change.name, workspace.node(change.nodeId)!!)
                }
            } else if (change is Delete) {
                val node = nodesById.remove(change.nodeId)
                if (node != null) {
                    deleteNode(node)
                }
            }
        })

        workspace.projects.forEach(Consumer<ProjectNode> { this.grab(it) })
    }

    private fun deleteNode(node: Node) {
        symbols.remove(node)
        node.forEach(Consumer<Node> { this.deleteNode(it) })
    }

    fun symbols(s: String): SortedSet<NamedNode> =
            symbols.subSet(s, s + Character.MAX_VALUE) as SortedSet<NamedNode>


    private fun grab(node: Node) {
        if (node is FunctionNode || isField(node)) {
            symbols.add(node)
        } else {
            node.forEach(Consumer<Node> { this.grab(it) })
        }

    }
}
