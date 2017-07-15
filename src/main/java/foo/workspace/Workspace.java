package foo.workspace;

import foo.changes.*;
import foo.interpreter.SystemPackage;
import foo.model.*;

import java.util.*;

public class Workspace {
    public static final int MAX_PROBLEMS = 10;

    private final Map<String, Node> nodes = new HashMap<>();
    private final List<Change> appliedChanges = new ArrayList<>();
    private final List<ProjectNode> projects = new ArrayList<>();

    public Workspace() {
        registerPackage(new SystemPackage());
    }

    private final ChangeVisitor<Node> changeVisitor = new ChangeVisitor<Node>() {
        @Override
        public Node visitCreateProject(CreateProject createProject) {
            ProjectNode projectNode = new ProjectNode();
            projectNode.setId(createProject.getId());
            projectNode.setName(createProject.getName());
            addNode(projectNode);
            return null;
        }

        @Override
        public Node visitCreatePackage(CreatePackage createPackage) {
            Node parent = node(createPackage.getParent());
            PackageNode packageNode = new PackageNode();
            packageNode.setName(createPackage.getName());
            packageNode.setId(createPackage.getId());
            addNode(packageNode);
            if (parent instanceof ProjectNode) {
                ((ProjectNode) parent).getPackages().add(packageNode);
            } else if(parent instanceof PackageNode) {
                ((PackageNode) parent).getItems().add(packageNode);
            }
            return null;
        }

        @Override
        public Node visitCreateFunction(CreateFunction createFunction) {
            PackageNode parent = node(createFunction.getParent());
            FunctionNode functionNode = new FunctionNode();
            functionNode.setName(createFunction.getName());
            functionNode.setId(createFunction.getId());
            addNode(functionNode);
            parent.getItems().add(functionNode);
            return null;
        }

        @Override
        public Node visitCreateParameter(CreateParameter createParameter) {
            ParameterNode parameterNode = new ParameterNode();
            parameterNode.setName(createParameter.getName());
            parameterNode.setId(createParameter.getId());
            addNode(parameterNode);
            place(parameterNode, createParameter.getPosition());

            return null;

        }

        @Override
        public Node visitCreateBoundCall(CreateBoundCall createBoundCall) {
            FunctionNode function = node(createBoundCall.getFunction());
            BoundCallNode node = new BoundCallNode();
            node.setFunction(function);
            node.setId(createBoundCall.getId());
            addNode(node);

            place(node, createBoundCall.getDestination());

            return node;
        }

        @Override
        public Node visitReference(Reference reference) {
            RefNode ref = new RefNode();
            ref.setId(reference.getId());
            ref.setNode(node(reference.getNodeId()));
            addNode(ref);

            place(ref, reference.getDestination());

            return ref;
        }
    };

    public List<ProjectNode> getProjects() {
        return projects;
    }

    public List<Problem> apply(List<Change> changes) {
        List<Problem> problems = new ArrayList<>();

        for (Change change: changes) {

            try {
                change.accept(changeVisitor);
            } catch (Exception e) {
//            Problem problem =
//            if (problem != null) {
//                problems.add(problem);
//                if (problems.size() == MAX_PROBLEMS) {
//                    break;
//                }
//            }
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

    public <T> T node(String id) {
        return (T) nodes.get(id);
    }

    public void addNode(Node node) {
        nodes.put(node.getId(), node);
        if (node instanceof ProjectNode) {
            projects.add((ProjectNode) node);
        }
    }

    public void registerPackage(PackageNode p) {
        addNode(p);
        for (Node n: p.getItems()) {
            if (n instanceof PackageNode) {
                registerPackage(p);
            } else {
                addNode(n);

                if (n instanceof FunctionNode) {
                    for (ParameterNode parameterNode: ((FunctionNode) n).getParameters()) {
                        addNode(parameterNode);
                    }
                }
            }
        }
    }

    private void place(Node node, Destination destination) {
        if (destination instanceof Binding) {
            Binding binding = (Binding) destination;
            BoundCallNode parent = node(binding.getParent());
            ParameterNode parameter = node(binding.getParameter());
            parent.getArguments().put(parameter, node);
        } else {
            Position position = (Position) destination;
            ListLike parent = node(position.getParent());

            Node prev = node(position.getPrev());
            Node next = node(position.getNext());

            List items = (node instanceof ParameterNode) ? ((FunctionNode) parent).getParameters() : parent.getItems();


            if (next == null) {
                if (!items.isEmpty() && items.get(items.size() - 1) == prev || items.isEmpty() && prev == null) {
                    items.add(node);
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
}
