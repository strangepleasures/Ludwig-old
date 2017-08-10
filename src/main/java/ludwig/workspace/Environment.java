package ludwig.workspace;

import lombok.Getter;

@Getter
public class Environment {
    private final Workspace workspace = new Workspace();
    private final UsageTracker usageTracker = new UsageTracker(workspace);
    private final SymbolRegistry symbolRegistry = new SymbolRegistry(workspace);

    public Environment() {
        workspace.init();
    }

}
