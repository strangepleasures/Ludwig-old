package ludwig.workspace;

import ludwig.changes.*;
import ludwig.interpreter.Runtime;
import ludwig.model.*;
import ludwig.script.Parser;

import java.io.InputStreamReader;
import java.io.Reader;
import java.util.*;
import java.util.function.Consumer;

public class Workspace {
    public static final int MAX_PROBLEMS = 10;

    private final Map<String, Node> nodes = new HashMap<>();
    private final List<Change> appliedChanges = new ArrayList<>();
    private final List<ProjectNode> projects = new ArrayList<>();
    private final List<Consumer<List<Change>>> changeListeners = new ArrayList<>();

    public Workspace() {
        Runtime runtime = new Runtime();
        addNode(runtime);

        try {
            try (Reader reader = new InputStreamReader(Workspace.class.getResourceAsStream("/system.lw"))) {
                Parser.parse(reader, this, runtime);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public final List<Consumer<List<Change>>> changeListeners() {
        return changeListeners;
    }

    private final ChangeVisitor<Problem> changeVisitor = new ChangeVisitor<Problem>() {

        @Override
        public Problem visitInsertNode(InsertNode insert) {
            return place(insert.getNode(), insert);
        }

        @Override
        public Problem visitInsertReference(InsertReference insert) {
            Node ref = new ReferenceNode(node(insert.getRef())).id(insert.getId());
            return place(ref, insert);
        }

        @Override
        public Problem visitDelete(Delete delete) {
            Node node = node(delete.getId());
            node.parent().children().remove(node);
            node.delete();
            return null;
        }
    };

    private Problem place(Node node, Insert insert) {
        addNode(node);
        Node parent = node(insert.getParent());
        node.parent(parent);

        if (parent != null) {
            Node prev = node(insert.getPrev());
            Node next = node(insert.getNext());

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
        return null;
    }

    public List<ProjectNode> getProjects() {
        return projects;
    }

    public List<Problem> apply(List<Change> changes) {
        List<Problem> problems = new ArrayList<>();

        for (Change change : changes) {
            Problem problem = change.accept(changeVisitor);
            if (problem != null) {
                problems.add(problem);
                if (problems.size() == MAX_PROBLEMS) {
                    break;
                }
            }
        }

        if (problems.isEmpty()) { // TODO: Make a distinction between warnings and errors
            appliedChanges.addAll(changes);

            changeListeners.forEach(listener -> listener.accept(changes));
        } else {
            restore();
        }
        return problems;
    }

    private void restore() {
        nodes.clear();
        projects.clear();

        List<Change> changes = new ArrayList<>(appliedChanges);
        appliedChanges.clear();
        apply(changes);
    }

    public <T extends Node> T node(String id) {
        return id == null ? null : (T) nodes.get(id);
    }

    public void addNode(Node node) {
        nodes.put(node.id(), node);
        if (node instanceof ProjectNode) {
            projects.add((ProjectNode) node);
        }
        node.children().forEach(this::addNode);
    }
}
