package ludwig.workspace;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.SetMultimap;
import ludwig.changes.Change;
import ludwig.changes.InsertReference;
import ludwig.model.NamedNode;
import ludwig.model.ReferenceNode;

import java.util.Set;
import java.util.function.Consumer;

public class UsageTracker implements Consumer<Change> {
    private final Workspace workspace;
    private final SetMultimap<NamedNode, ReferenceNode> refs = HashMultimap.create();

    public UsageTracker(Workspace workspace) {
        this.workspace = workspace;
        workspace.changeListeners().add(this);
    }

    public Set<ReferenceNode> usages(NamedNode<?> node) {
        return refs.get(node);
    }

    @Override
    public void accept(Change change) {
        if (change instanceof InsertReference) {
            InsertReference ref = (InsertReference) change;
            refs.put(workspace.node(ref.getRef()), workspace.node(ref.getId()));
        }
        // TODO: handle deletions
    }
}
