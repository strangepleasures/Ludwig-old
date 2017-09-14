package ludwig.workspace;

import ludwig.changes.*;
import ludwig.interpreter.Builtins;
import ludwig.model.*;
import ludwig.runtime.StdLib;
import ludwig.script.Parser;

import java.io.InputStreamReader;
import java.io.Reader;
import java.util.*;
import java.util.function.Consumer;

public class Workspace {
    public static final int MAX_PROBLEMS = 10;

    private final Map<String, Node> nodes = new HashMap<>();
    private final List<Change<?>> appliedChanges = new ArrayList<>();
    private final List<ProjectNode> projects = new ArrayList<>();
    private final List<Consumer<Change<?>>> changeListeners = new ArrayList<>();
    private ProjectNode builtins;
    private boolean batchUpdate;
    private boolean loading;

    public void init() {
        builtins = new ProjectNode().name("Runtime").id("Runtime").readonly(true);
        builtins.add(Builtins.of(StdLib.class));

        addNode(builtins);

        try {
            try (Reader reader = new InputStreamReader(Workspace.class.getResourceAsStream("/system.lw"))) {
                Parser.Companion.parse(reader, this, builtins);
            }
            try (Reader reader = new InputStreamReader(Workspace.class.getResourceAsStream("/system-tests.lw"))) {
                Parser.Companion.parse(reader, this, builtins);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public final List<Consumer<Change<?>>> changeListeners() {
        return changeListeners;
    }

    private final ChangeVisitor<Problem> changeVisitor = new ChangeVisitor<Problem>() {

        @Override
        public Problem visitInsertNode(InsertNode insert) {
            return place(insert.node(), insert);
        }

        @Override
        public Problem visitInsertReference(InsertReference insert) {
            Node ref = new ReferenceNode(node(insert.ref())).id(insert.id());
            return place(ref, insert);
        }

        @Override
        public Problem visitDelete(Delete delete) {
            Node node = node(delete.id());
            node.parent().children().remove(node);
            node.delete();
            return null;
        }

        @Override
        public Problem visitComment(Comment comment) {
            Node node = node(comment.nodeId());
            node.comment(comment.comment());
            return null;
        }

        @Override
        public Problem visitRename(Rename rename) {
            NamedNode node = node(rename.getNodeId());
            node.name(rename.name());
            return null;
        }
    };

    private Problem place(Node node, Insert insert) {
        addNode(node);
        Node parent = node(insert.parent());
        node.parent(parent);

        if (parent != null) {
            Node prev = node(insert.prev());
            Node next = node(insert.next());

            if (!parent.isOrdered()) {
                parent.add(node);
            } else {
                List items = parent.children();

                if (next == null) {
                    if (!items.isEmpty() && items.get(items.size() - 1) == prev || items.isEmpty() && prev == null) {
                        parent.add(node);
                    }
                } else if (prev == null) {
                    if (!items.isEmpty() && items.get(0) == next) {
                        items.add(0, node);
                    }
                } else {
                    int prevIndex = items.indexOf(prev);
                    int nextIndex = items.indexOf(next);

                    if (nextIndex == prevIndex + 1) {
                        items.add(nextIndex, next);
                    }
                }
            }
        }
        return null;
    }

    public List<ProjectNode> getProjects() {
        return projects;
    }

    public List<Problem> apply(List<Change<?>> changes) {
        List<Problem> problems = new ArrayList<>();
        for (int i = 0; i < changes.size(); i++) {
            Change<?> change = changes.get(i);
            batchUpdate = i < changes.size() - 1;
            Problem problem = change.accept(changeVisitor);
            if (problem != null) {
                problems.add(problem);
                if (problems.size() == MAX_PROBLEMS) {
                    break;
                }
            } else {
                changeListeners.forEach(listener -> listener.accept(change));
            }
        }

        if (problems.isEmpty()) { // TODO: Make a distinction between warnings and errors
            appliedChanges.addAll(changes);


        } else {
            restore();
        }
        return problems;
    }

    public List<Problem> load(List<Change<?>> changes) {
        loading = true;
        try {
            return apply(changes);
        } finally {
            loading = false;
        }
    }

    public boolean isLoading() {
        return loading;
    }

    private void restore() {
        nodes.clear();
        projects.clear();

        List<Change<?>> changes = new ArrayList<>(appliedChanges);
        appliedChanges.clear();
        apply(changes);
    }

    public <T extends Node> T node(String id) {
        return id == null ? null : (T) nodes.get(id);
    }

    public void addNode(Node<?> node) {
        nodes.put(node.id(), node);
        if (node instanceof ProjectNode) {
            projects.add((ProjectNode) node);
        }
        node.children().forEach(this::addNode);
    }

    public boolean isBatchUpdate() {
        return batchUpdate;
    }
}
