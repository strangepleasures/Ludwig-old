package foo.workspace;

import com.sun.xml.internal.bind.v2.runtime.reflect.Lister;
import foo.changes.*;
import foo.model.*;

import java.util.*;

public class Workspace {
    public static final int MAX_PROBLEMS = 10;

    private final Map<String, Node> nodes = new HashMap<>();
    private final List<Change> appliedChanges = new ArrayList<>();
    private final List<ProjectNode> projects = new ArrayList<>();

    private final ChangeVisitor<Problem> changeVisitor = new ChangeVisitor<Problem>() {
        @Override
        public Problem visitCreateProject(CreateProjectChange createProjectChange) {
            ProjectNode projectNode = new ProjectNode();
            projectNode.setId(createProjectChange.getId());
            projectNode.setName(createProjectChange.getName());
            addNode(projectNode);
            return null;
        }

        @Override
        public Problem visitCreatePackage(CreatePackageChange createPackageChange) {
            Node parent = node(createPackageChange.getParentId());
            PackageNode packageNode = new PackageNode();
            packageNode.setName(createPackageChange.getName());
            packageNode.setId(createPackageChange.getId());
            addNode(packageNode);
            if (parent instanceof ProjectNode) {
                ((ProjectNode) parent).getPackages().add(packageNode);
            } else if(parent instanceof PackageNode) {
                ((PackageNode) parent).getItems().add(packageNode);
            }
            return null;
        }
    };

    public List<ProjectNode> getProjects() {
        return projects;
    }

    public List<Problem> apply(List<Change> changes) {
        List<Problem> problems = new ArrayList<>();

        for (Change change: changes) {
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
        } else {
           restore();
        }
        return problems;
    }

    private void restore() {
        nodes.clear();
        projects.clear();

        List<Change> changes =new ArrayList<>(appliedChanges);
        appliedChanges.clear();
        apply(changes);
    }

    public <T extends Node> T node(String id) {
        return (T) nodes.get(id);
    }

    public void addNode(Node node) {
        nodes.put(node.getId(), node);
        if (node instanceof ProjectNode) {
            projects.add((ProjectNode) node);
        }
    }
}
