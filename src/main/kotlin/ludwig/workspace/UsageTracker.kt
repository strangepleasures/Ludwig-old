package ludwig.workspace

import com.google.common.collect.HashMultimap
import ludwig.changes.InsertReference
import ludwig.model.NamedNode
import ludwig.model.ReferenceNode
import java.util.function.Consumer

class UsageTracker(workspace: Workspace) {
    private val refs = HashMultimap.create<NamedNode, ReferenceNode>()

    init {
        workspace.changeListeners().add(Consumer { change ->
            if (change is InsertReference) {
                refs.put(workspace.node(change.ref), workspace.node(change.id))
            }
        })
    }

    fun usages(node: NamedNode): Set<ReferenceNode> {
        return refs.get(node)
    }

}
