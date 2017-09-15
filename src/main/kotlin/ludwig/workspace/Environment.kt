package ludwig.workspace

class Environment {
    private val workspace = Workspace()
    private val usageTracker: UsageTracker
    private val symbolRegistry: SymbolRegistry

    init {
        symbolRegistry = SymbolRegistry(workspace)
        usageTracker = UsageTracker(workspace)
        workspace.init()
    }

    fun workspace(): Workspace {
        return workspace
    }

    fun usageTracker(): UsageTracker {
        return usageTracker
    }

    fun symbolRegistry(): SymbolRegistry {
        return symbolRegistry
    }
}
