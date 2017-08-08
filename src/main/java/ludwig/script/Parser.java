package ludwig.script;

import ludwig.changes.*;
import ludwig.model.*;
import ludwig.workspace.Workspace;
import org.pcollections.HashPMap;
import org.pcollections.HashTreePMap;

import java.io.IOException;
import java.io.Reader;
import java.util.Collections;
import java.util.List;

public class Parser {
    private final List<String> tokens;
    private final Workspace workspace;

    private int pos;
    private HashPMap<String, NamedNode> locals = HashTreePMap.empty();

    private Parser(List<String> tokens, Workspace workspace) {
        this.tokens = tokens;
        this.workspace = workspace;
    }


    public static void parse(Reader reader, Workspace workspace, ProjectNode projectNode) throws ParserException, IOException, LexerException {
        parse(Lexer.read(reader), workspace, projectNode);
    }

    public static void parse(List<String> tokens, Workspace workspace, ProjectNode projectNode) throws ParserException {
        new Parser(tokens, workspace).parse(projectNode);
    }

    private void parse(ProjectNode projectNode) throws ParserException {
        PackageNode packageNode = parseSignatures(projectNode);
        parseBodies(packageNode);
    }

    private PackageNode parseSignatures(ProjectNode projectNode) throws ParserException {
        consume("(");
        consume("package");

        String packageName = nextToken();
        PackageNode packageNode = projectNode.children().stream().map(n -> (PackageNode) n)
            .filter(n -> n.getName().equals(packageName))
            .findFirst()
            .orElseGet(() -> append(projectNode, new PackageNode().setName(packageName)));

        consume(")");


        while (pos < tokens.size()) {
            parseSignature(packageNode);
        }

        return packageNode;
    }

    private void parseSignature(PackageNode packageNode) throws ParserException {
        consume("(");
        consume("def");
        boolean lazy = false;
        if (currentToken().equals("lazy")) {
            lazy = true;
            consume("lazy");
        }
        FunctionNode fn = append(packageNode, new FunctionNode().setName(nextToken()).setLazy(lazy));
        while (!currentToken().equals(")")) {
            append(fn, new VariableNode().setName(nextToken()));
        }
        consume(")");
        append(fn, new SeparatorNode());
        consume("(");

        int level = 1;
        while (level != 0 && pos < tokens.size()) {
            switch (nextToken()) {
                case "(":
                    level++;
                    break;
                case ")":
                    level--;
                    break;
            }
        }
    }

    private void parseBodies(PackageNode packageNode) throws ParserException {
        rewind();
        consume("(");
        consume("package");

        nextToken();
        consume(")");

        while (pos < tokens.size()) {
            parseBody(packageNode);
        }
    }

    private void parseBody(PackageNode packageNode) throws ParserException {
        consume("(");
        consume("def");
        if (currentToken().equals("lazy")) {
            consume("lazy");
        }
        FunctionNode node = (FunctionNode) packageNode.item(nextToken());
        locals = HashTreePMap.empty();
        for (Node child : node.children()) {
            if (child instanceof SeparatorNode) {
                break;
            }
            VariableNode variableNode = (VariableNode) child;
            locals = locals.plus(variableNode.getName(), variableNode);
        }
        while (!nextToken().equals(")")) ;
        consume("(");
        while (pos < tokens.size() && !currentToken().equals(")")) {
            parseChild(node);
        }

        if (pos < tokens.size()) {
            nextToken();
        }
    }


    private void parseChild(Node parent) throws ParserException {
        int level = 0;
        while (currentToken().equals("(")) {
            level++;
            nextToken();
        }

        try {
            String head = nextToken();

            switch (head) {
                case "call":
                case "if":
                case "else":
                case "return":
                case "throw":
                case "list": {
                    Node node = append(parent, createSpecial(head));
                    while (!currentToken().equals(")")) {
                        parseChild(node);
                    }
                    break;
                }
                case "ref":
                    FunctionReferenceNode ref = append(parent, new FunctionReferenceNode());
                    append(ref, new ReferenceNode(find(nextToken())));
                    break;
                case "for": {
                    ForNode node = append(parent, new ForNode());
                    VariableNode var = new VariableNode();
                    var.setName(nextToken());
                    append(node, var);

                    HashPMap savedLocals = locals;
                    locals = locals.plus(var.getName(), var);

                    while (!currentToken().equals(")")) {
                        parseChild(node);
                    }
                    locals = savedLocals;
                    break;
                }
                case "=": {
                    String name = nextToken();
                    if (locals.containsKey(name)) {
                        AssignmentNode node = append(parent, new AssignmentNode());
                        append(node, new ReferenceNode(locals.get(name)));
                        parseChild(node);
                        break;
                    } else {
                        AssignmentNode node = append(parent, new AssignmentNode());
                        VariableNode lhs = append(node, new VariableNode().setName(name));
                        locals = locals.plus(name, lhs);
                        parseChild(node);
                        break;
                    }
                }
                case "λ":
                case "\\": {
                    LambdaNode node = append(parent, new LambdaNode());
                    HashPMap<String, NamedNode> savedLocals = locals;
                    while (!currentToken().equals(")")) {
                        VariableNode param = append(node, new VariableNode().setName(nextToken()));
                        locals = locals.plus(param.getName(), param);
                    }
                    consume(")");
                    append(node, new SeparatorNode());
                    consume("(");
                    while (!currentToken().equals(")")) {
                        parseChild(node);
                    }
                    locals = savedLocals;
                    break;
                }

                default: {
                    if (locals.containsKey(head)) {
                        append(parent, new ReferenceNode(locals.get(head)));
                    } else {
                        NamedNode headNode = find(head);
                        if (headNode instanceof FunctionNode) {
                            FunctionNode fn = (FunctionNode) headNode;
                            ReferenceNode r = append(parent, new ReferenceNode(fn));
                            for (Node param : fn.children()) {
                                if (param instanceof SeparatorNode) {
                                    break;
                                }
                                parseChild(r);
                            }
                        } else if (Lexer.isLiteral(head)) {
                            append(parent, new LiteralNode(head));
                        } else {
                            throw new ParserException("Unknown symbol: " + head);
                        }
                    }
                }
            }
        } finally {
            for (int i = 0; i < level; i++) {
                consume(")");
            }
        }
    }

    private String nextToken() {
        return tokens.get(pos++);
    }

    private String currentToken() {
        return tokens.get(pos);
    }

    private void consume(String token) throws ParserException {
        if (!nextToken().equals(token)) {
            throw new ParserException("Expected " + token);
        }
    }

    private void rewind() {
        pos = 0;
    }

    // TODO: Optimize
    private NamedNode find(String name) {
        for (ProjectNode project : workspace.getProjects()) {
            for (Node node : project.children()) {
                PackageNode packageNode = (PackageNode) node;
                if (packageNode.item(name) != null) {
                    return packageNode.item(name);
                }
            }
        }
        return null;
    }

    private Node createSpecial(String token) {
        switch (token) {
            case "call":
                return new CallNode();
            case "if":
                return new IfNode();
            case "else":
                return new ElseNode();
            case "return":
                return new ReturnNode();
            case "list":
                return new ListNode();
            case "throw":
                return new ThrowNode();
        }
        return null;
    }

    private <T extends Node> T append(Node<?> parent, T node) {
        if (node instanceof ReferenceNode) {
            InsertReference change = new InsertReference()
                .setId(Change.newId())
                .setParent(parent.id())
                .setPrev(parent.children().isEmpty() ? null : parent.children().get(parent.children().size() - 1).id())
                .setNext(null)
                .setRef(((ReferenceNode) node).ref().id());
            workspace.apply(Collections.singletonList(change));
            return workspace.node(change.getId());
        } else {
            if (node instanceof NamedNode) {
                node.id(parent.id() + ":" + ((NamedNode) node).getName());
            } else {
                node.id(Change.newId());
            }
            InsertNode change = new InsertNode()
                .setNode(node)
                .setParent(parent.id())
                .setPrev(parent.children().isEmpty() ? null : parent.children().get(parent.children().size() - 1).id());
            workspace.apply(Collections.singletonList(change));
            return workspace.node(node.id());
        }

    }
}
