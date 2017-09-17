package ludwig.workspace

import com.google.common.collect.HashMultimap
import ludwig.changes.Value
import ludwig.model.NamedNode
import ludwig.model.ReferenceNode

class UsageTracker(workspace: Workspace) {
    private val refs = HashMultimap.create<NamedNode, ReferenceNode>()

    init {
        workspace.changeListeners().add({ change ->
            if (change is Value && workspace.node(change.nodeId) is ReferenceNode) {
                refs.put(workspace.node(change.value) as NamedNode, workspace.node(change.nodeId) as ReferenceNode)
            }
        })
    }

    fun usages(node: NamedNode): Set<ReferenceNode> {
        return refs.get(node)
    }

}
