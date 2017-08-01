package ludwig.script;

import ludwig.changes.Change;
import ludwig.model.*;
import ludwig.workspace.Workspace;

import java.io.IOException;
import java.io.Reader;
import java.util.*;

public class Parser {
    private final List<String> tokens;
    private final Workspace workspace;

    private int pos;
    private Map<String, NamedNode> locals = new HashMap<>();

    private Parser(List<String> tokens, Workspace workspace) {
        this.tokens = tokens;
        this.workspace = workspace;
    }


    public static List<Node> parse(Reader reader, Workspace workspace, ProjectNode projectNode) throws ParserException, IOException, LexerException {
        return parse(Lexer.read(reader), workspace, projectNode);
    }

    public static List<Node> parse(List<String> tokens, Workspace workspace, ProjectNode projectNode) throws ParserException {
        return new Parser(tokens, workspace).parse(projectNode);
    }

    private List<Node> parse(ProjectNode projectNode) throws ParserException {
        PackageNode packageNode = parseSignatures(projectNode);
        parseBodies(packageNode);
        return null;
    }



    private PackageNode parseSignatures(ProjectNode projectNode) throws ParserException {
        consume("(");
        consume("package");

        String packageName = nextToken();
        PackageNode packageNode = projectNode.children().stream().map(n -> (PackageNode) n)
            .filter(n -> n.getName().equals(packageName)).findFirst().orElseGet(() -> {
                PackageNode pn = new PackageNode();
                pn.setName(packageName).id(projectNode.id() + ":" + packageName);
                projectNode.children().add(pn);
                return pn;
            });

        consume(")");



        while (pos < tokens.size()) {
            packageNode.children().add(parseSignature(packageNode.id()));
        }

        return packageNode;
    }

    private Node parseSignature(String packageId) throws ParserException {
        consume("(");
        String token  = nextToken();
        switch (token) {
            case "def": {
                FunctionNode node = new FunctionNode();
                node.setName(nextToken());
                node.id(packageId + ":" + node.getName());
                while (!currentToken().equals(")")) {
                    ParameterNode param = new ParameterNode();
                    param.setName(nextToken());
                    param.id(node.id() + ":" + param.getName());
                    node.parameters().add(param);
                }
                consume(")");
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
                return node;
            }
            case "=": {
                LetNode node = new LetNode();
                node.setName(nextToken());
                node.id(packageId + ":" + node.id());

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
                return node;
            }
        }

        return null;
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
        String token  = nextToken();
        switch (token) {
            case "def": {
                FunctionNode node = (FunctionNode) packageNode.item(nextToken());
                locals.clear();
                node.parameters().forEach(p -> locals.put(p.getName(), p));
                while (!nextToken().equals(")"));
                consume("(");
                while (pos < tokens.size() && !currentToken().equals(")")) {
                    node.children().add(parseNode());
                }

                if (pos < tokens.size()) {
                    nextToken();
                }
                break;
            }
            case "=": {
                LetNode node = (LetNode) packageNode.item(nextToken());
                node.children().add(parseNode());
                consume(")");
                break;
            }
        }
    }


    private Node parseNode() throws ParserException {
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
                case "list":   {
                    Node node = createSpecial(head);
                    node.id(Change.newId());
                    while (!currentToken().equals(")")) {
                        node.children().add(parseNode());
                    }
                    return node;
                }
                case "for": {
                    ForNode node = new ForNode();
                    node.setName(nextToken());
                    node.id(Change.newId());
                    locals.put(node.getName(), node);
                    while (!currentToken().equals(")")) {
                        node.children().add(parseNode());
                    }
                    locals.remove(node.getName());
                    return node;
                }
                case "=": {
                    String name = nextToken();
                    if (locals.containsKey(name)) {
                        AssignmentNode node = new AssignmentNode();
                        node.id(Change.newId());
                        RefNode var = new RefNode(locals.get(name));
                        var.id(Change.newId());
                        node.children().add(var);
                        node.children().add(parseNode());
                        return node;
                    } else {
                        LetNode node = (LetNode) new LetNode().setName(name).id(Change.newId());
                        locals.put(name, node);
                        node.children().add(parseNode());
                        return node;
                    }
                }
                case "ref": {
                    RefNode node = new RefNode(find(nextToken()));
                    node.id(Change.newId());
                    return node;
                }
                case "λ":
                case "\\": {
                    LambdaNode node = new LambdaNode();
                    node.id(Change.newId());
                    while (!currentToken().equals(")")) {
                        ParameterNode param = (ParameterNode) new ParameterNode().setName(nextToken()).id(Change.newId());
                        locals.put(param.getName(), param);
                        node.parameters().add(param);
                    }
                    consume(")");
                    consume("(");
                    while (!currentToken().equals(")")) {
                        node.children().add(parseNode());
                    }
                    return node;
                }

                default: {
                    if (locals.containsKey(head)) {
                        return new RefNode(locals.get(head)).id(Change.newId());
                    }

                    NamedNode headNode = find(head);
                    if (headNode != null) {
                        if (headNode instanceof FunctionNode) {
                            FunctionNode fn = (FunctionNode) headNode;
                            BoundCallNode node = new BoundCallNode();
                            node.id(Change.newId());
                            RefNode r = new RefNode(fn);
                            r.id(Change.newId());
                            node.children().add(r);
                            for (ParameterNode param : fn.parameters()) {
                                node.arguments().put(param, parseNode());
                            }
                            return node;
                        } else {
                            return new RefNode(headNode);
                        }
                    } else if (Lexer.isLiteral(head)) {
                        return new LiteralNode(head).id(Change.newId());
                    } else {
                        throw new ParserException("Unknown symbol: " + head);
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
                return new UnboundCallNode();
            case "if":
                return new IfNode();
            case "else":
                return new ElseNode();
            case "return":
                return new ReturnNode();
            case "list":
                return new ListNode();
        }
        return null;
    }
}
