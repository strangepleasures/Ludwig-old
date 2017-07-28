package foo.script;

import foo.model.*;
import foo.workspace.Workspace;

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
        PackageNode packageNode = new PackageNode();

        packageNode.setName(packageName);
        consume(")");

        projectNode.children().add(packageNode);

        while (pos < tokens.size()) {
            packageNode.children().add(parseSignature());
        }

        return packageNode;
    }

    private Node parseSignature() throws ParserException {
        consume("(");
        String token  = nextToken();
        switch (token) {
            case "def": {
                FunctionNode node = new FunctionNode();
                node.setName(nextToken());
                while (!currentToken().equals(")")) {
                    node.parameters().add((ParameterNode) new ParameterNode().setName(nextToken()));
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
                while (!currentToken().equals(")")) {
                    nextToken();
                }
                consume(")");
                consume("(");
                while (pos < tokens.size() && !currentToken().equals(")")) {
                    node.children().add(parseNode());
                }

                nextToken();
            }
        }
    }


    private Node parseNode() throws ParserException {
        boolean isForm = currentToken().equals("(");
        if (isForm) {
            nextToken();
        }

        try {
            String head = nextToken();

            switch (head) {
                case "!":
                case "if":
                case "else":
                case "return": {
                    Node node = createSpecial(head);
                    while (!currentToken().equals(")")) {
                        node.children().add(parseNode());
                    }
                    return node;
                }
                case "for": {
                    ForNode node = new ForNode();
                    node.setName(nextToken());
                    while (!currentToken().equals(")")) {
                        node.children().add(parseNode());
                    }
                    return node;
                }
                case "=": {
                    String name = nextToken();
                    if (locals.containsKey(name)) {
                        AssignmentNode node = new AssignmentNode();
                        node.children().add(new RefNode(locals.get(name)));
                        node.children().add(parseNode());
                        return node;
                    } else {
                        LetNode node = (LetNode) new LetNode().setName(name);
                        locals.put(name, node);
                        node.children().add(parseNode());
                        return node;
                    }
                }
                case "@": {
                    return new RefNode(find(nextToken()));
                }
                case "λ":
                case "\\": {
                    LambdaNode node = new LambdaNode();
                    while (!currentToken().equals(")")) {
                        ParameterNode param = (ParameterNode) new ParameterNode().setName(nextToken());
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
                        return new RefNode(locals.get(head));
                    }

                    NamedNode headNode = find(head);
                    if (headNode != null) {
                        if (headNode instanceof FunctionNode) {
                            FunctionNode fn = (FunctionNode) headNode;
                            BoundCallNode node = new BoundCallNode();
                            node.children().add(new RefNode(fn));
                            for (ParameterNode param : fn.parameters()) {
                                node.arguments().put(param, parseNode());
                            }
                            return node;
                        } else {
                            return new RefNode(headNode);
                        }
                    } else if (Lexer.isLiteral(head)) {
                        return new LiteralNode(head);
                    }
                }
            }
            return null;
        } finally {
            if (isForm) {
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
            case "!":
                return new UnboundCallNode();
            case "if":
                return new IfNode();
            case "else":
                return new ElseNode();
            case "return":
                return new ReturnNode();
        }
        return null;
    }
}
