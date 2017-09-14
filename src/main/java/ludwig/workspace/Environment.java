package ludwig.workspace;

public class Environment {
    private final Workspace workspace = new Workspace();
    private final UsageTracker usageTracker;
    private final SymbolRegistry symbolRegistry;

    public Environment() {
        symbolRegistry = new SymbolRegistry(workspace);
        usageTracker = new UsageTracker(workspace);
        workspace.init();
    }

    public Workspace workspace() {
        return workspace;
    }

    public UsageTracker usageTracker() {
        return usageTracker;
    }

    public SymbolRegistry symbolRegistry() {
        return symbolRegistry;
    }
}
