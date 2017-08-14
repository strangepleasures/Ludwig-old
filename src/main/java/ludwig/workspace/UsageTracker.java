package ludwig.workspace;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.SetMultimap;
import ludwig.changes.InsertReference;
import ludwig.model.NamedNode;
import ludwig.model.ReferenceNode;

import java.util.Set;

public class UsageTracker {
    private final SetMultimap<NamedNode, ReferenceNode> refs = HashMultimap.create();

    public UsageTracker(Workspace workspace) {
        workspace.changeListeners().add(change -> {
            if (change instanceof InsertReference) {
                InsertReference ref = (InsertReference) change;
                refs.put(workspace.node(ref.ref()), workspace.node(ref.id()));
            }
        });
    }

    public Set<ReferenceNode> usages(NamedNode<?> node) {
        return refs.get(node);
    }

}
