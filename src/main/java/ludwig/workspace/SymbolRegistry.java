package ludwig.workspace;


import ludwig.changes.Delete;
import ludwig.changes.InsertNode;
import ludwig.model.*;

import java.util.*;

public class SymbolRegistry {
    private final TreeSet symbols = new TreeSet<>(Comparator.comparing(SymbolRegistry::stringify));
    private final Map<String, Node> nodesById = new HashMap<>();

    public SymbolRegistry(Workspace workspace) {
        workspace.changeListeners().add(change -> {
            if (change instanceof InsertNode) {
                Node node = ((InsertNode) change).getNode();
                if (node instanceof FunctionNode || node instanceof FieldNode) {
                    symbols.add(node);
                    nodesById.put(node.id(), node);
                }
            } else if (change instanceof Delete) {
                symbols.remove(nodesById.remove(((Delete) change).getId()));
            }
        });

        workspace.getProjects().forEach(this::grab);
    }

    public SortedSet<NamedNode> symbols(String s) {
        if (s.isEmpty() || s.length() < 2 && Character.isAlphabetic(s.charAt(0))) {
            return new TreeSet<>();
        }

        return symbols.subSet(s, s + Character.MAX_VALUE);
    }

    private static String stringify(Object o) {
        if (o instanceof FunctionNode) {
            return ((FunctionNode) o).signature();
        }
        return o.toString();
    }

    private void grab(Node<?> node) {
        if (node instanceof FunctionNode || node instanceof FieldNode) {
            symbols.add(node);
        } else {
            node.children().forEach(this::grab);
        }

    }
}
