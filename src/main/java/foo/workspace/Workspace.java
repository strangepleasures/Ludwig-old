package foo.workspace;

import foo.changes.*;
import foo.changes.Package;
import foo.interpreter.SystemPackage;
import foo.model.*;
import foo.runtime.StdLib;

import java.util.*;

public class Workspace {
    public static final int MAX_PROBLEMS = 10;

    private final Map<String, Node> nodes = new HashMap<>();
    private final List<Change> appliedChanges = new ArrayList<>();
    private final List<ProjectNode> projects = new ArrayList<>();

    public Workspace() {
        registerProject(new foo.interpreter.Runtime());
    }

    private final ChangeVisitor<Problem> changeVisitor = new ChangeVisitor<Problem>() {
        @Override
        public Problem visitProject(Project project) {
            ProjectNode projectNode = new ProjectNode();
            projectNode.setId(project.getId());
            projectNode.setName(project.getName());
            addNode(projectNode);
            return null;
        }

        @Override
        public Problem visitPackage(Package aPackage) {
            Node parent = node(aPackage.getParent());
            PackageNode packageNode = new PackageNode();
            packageNode.setName(aPackage.getName());
            packageNode.setId(aPackage.getId());
            addNode(packageNode);
            if (parent instanceof ProjectNode) {
                ((ProjectNode) parent).getPackages().add(packageNode);
            } else if(parent instanceof PackageNode) {
                ((PackageNode) parent).getItems().add(packageNode);
            }
            return null;
        }

        @Override
        public Problem visitFunction(Function function) {
            PackageNode parent = node(function.getParent());
            FunctionNode functionNode = new FunctionNode();
            functionNode.setName(function.getName());
            functionNode.setId(function.getId());
            addNode(functionNode);
            parent.getItems().add(functionNode);
            return null;
        }

        @Override
        public Problem visitParameter(Parameter parameter) {
            ParameterNode parameterNode = new ParameterNode();
            parameterNode.setName(parameter.getName());
            parameterNode.setId(parameter.getId());
            place(parameterNode, parameter.getPosition());

            return null;
        }

        @Override
        public Problem visitBoundCall(BoundCall boundCall) {
            FunctionNode function = node(boundCall.getFunction());
            BoundCallNode node = new BoundCallNode();
            node.setFunction(function);
            node.setId(boundCall.getId());
            place(node, boundCall.getDestination());
            return null;
        }

        @Override
        public Problem visitReference(Reference reference) {
            RefNode ref = new RefNode();
            ref.setId(reference.getId());
            ref.setNode(node(reference.getNode()));
            place(ref, reference.getDestination());
            return null;
        }

        @Override
        public Problem visitLiteral(Literal literal) {
            LiteralNode literalNode = new LiteralNode(literal.getValue());
            literalNode.setId(literal.getId());
            place(literalNode, literal.getDestination());
            return null;
        }

        @Override
        public Problem visitUnboundCall(UnboundCall unboundCall) {
            UnboundCallNode node = new UnboundCallNode();
            node.setId(unboundCall.getId());
            place(node, unboundCall.getDestination());
            return null;
        }

        @Override
        public Problem visitReturn(Return aReturn) {
            ReturnNode node = new ReturnNode();
            node.setId(aReturn.getId());
            place(node, aReturn.getPosition());
            return null;
        }

        @Override
        public Problem visitLambda(Lambda lambda) {
            LambdaNode lambdaNode = new LambdaNode();
            lambdaNode.setId(lambda.getId());
            place(lambdaNode, lambda.getDestination());
            return null;
        }

        @Override
        public Problem visitAnd(And and) {
            AndNode andNode = new AndNode();
            andNode.setId(and.getId());
            place(andNode, and.getDestination());
            return null;
        }

        @Override
        public Problem visitOr(Or or) {
            OrNode orNode = new OrNode();
            orNode.setId(or.getId());
            place(orNode, or.getDestination());
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

        List<Change> changes = new ArrayList<>(appliedChanges);
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

    public void registerProject(ProjectNode projectNode) {
        addNode(projectNode);
        for (PackageNode packageNode: projectNode.getPackages()) {
            registerPackage(packageNode);
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
        addNode(node);

        if (destination instanceof Binding) {
            Binding binding = (Binding) destination;
            BoundCallNode parent = node(binding.getParent());
            ParameterNode parameter = node(binding.getParameter());
            parent.getArguments().put(parameter, node);
        } else if (destination instanceof Position) {
            Position position = (Position) destination;
            ListLike parent = node(position.getParent());

            Node prev = node(position.getPrev());
            Node next = node(position.getNext());

            List items = (node instanceof ParameterNode) ? ((Signature) parent).getParameters() : parent.getNodes();

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
        } else if (destination instanceof Slot) {
            ValueHolder<Node> dest = node(((Slot) destination).getParent());
            dest.setValue(node);
        }
    }
}
