package ludwig.workspace

import com.google.common.collect.HashMultimap
import ludwig.changes.Value
import ludwig.model.Node
import ludwig.model.SymbolNode

class UsageTracker(workspace: Workspace) {
    private val refs = HashMultimap.create<Node, SymbolNode>()

    init {
        workspace.changeListeners().add(changeListener { change ->
            if (change is Value && workspace.node(change.nodeId) is SymbolNode) {
                refs.put(workspace.node(change.value), workspace.node(change.nodeId) as SymbolNode)
            }
        })
    }

    fun usages(node: Node): Set<SymbolNode> {
        return refs.get(node)
    }

}
