package ludwig.workspace;

import lombok.Getter;

@Getter
public class Environment {
    private final Workspace workspace = new Workspace();
    private final UsageTracker usageTracker;
    private final SymbolRegistry symbolRegistry;

    public Environment() {
        symbolRegistry = new SymbolRegistry(workspace);
        usageTracker = new UsageTracker(workspace);
        workspace.init();
    }
}
