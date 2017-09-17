package ludwig.workspace

import com.google.common.collect.HashMultimap
import ludwig.changes.Value
import ludwig.model.Node
import ludwig.model.ReferenceNode

class UsageTracker(workspace: Workspace) {
    private val refs = HashMultimap.create<Node, ReferenceNode>()

    init {
        workspace.changeListeners().add(changeListener { change ->
            if (change is Value && workspace.node(change.nodeId) is ReferenceNode) {
                refs.put(workspace.node(change.value), workspace.node(change.nodeId) as ReferenceNode)
            }
        })
    }

    fun usages(node: Node): Set<ReferenceNode> {
        return refs.get(node)
    }

}
