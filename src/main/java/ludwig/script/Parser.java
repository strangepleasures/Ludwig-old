package ludwig.script;

import ludwig.changes.Change;
import ludwig.changes.InsertNode;
import ludwig.changes.InsertReference;
import ludwig.interpreter.ClassType;
import ludwig.model.*;
import ludwig.workspace.Workspace;
import org.pcollections.HashPMap;
import org.pcollections.HashTreePMap;

import java.io.IOException;
import java.io.Reader;
import java.util.Collections;
import java.util.List;

import static ludwig.utils.NodeUtils.isField;

public class Parser {
    private final List<String> tokens;
    private final Workspace workspace;

    private int pos;
    private HashPMap<String, NamedNode> locals = HashTreePMap.empty();
    private Signature superFunction;


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

    public static NamedNode item(PackageNode findByName, String name) {
        return findByName.children().stream().filter(n -> n instanceof NamedNode).map(n -> (NamedNode) n).filter(it -> it.name().equals(name)).findFirst().orElse(null);
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
                .filter(n -> n.name().equals(packageName))
                .findFirst()
                .orElseGet(() -> append(projectNode, new PackageNode().name(packageName)));

        consume(")");


        while (pos < tokens.size()) {
            parseSignature(packageNode);
        }

        return packageNode;
    }

    private void parseSignature(PackageNode packageNode) throws ParserException {
        consume("(");

        switch (nextToken()) {
            case "class": {
                ClassNode classNode = append(packageNode, new ClassNode().name(nextToken()));
                if (!currentToken().equals(")")) {
                    ClassNode superClass = (ClassNode) find(nextToken());
                    append(classNode, new ReferenceNode(superClass));
                }
                while (!currentToken().equals(")")) {
                    append(classNode, new VariableNode().name(nextToken()));
                }
                consume(")");
                ClassType.of(classNode);
                break;
            }
            case "def": {
                boolean lazy = false;
                if (currentToken().equals("lazy")) {
                    lazy = true;
                    consume("lazy");
                }
                FunctionNode fn = append(packageNode, new FunctionNode().name(nextToken()).setLazy(lazy));
                while (!currentToken().equals(")")) {
                    append(fn, new VariableNode().name(nextToken()));
                }
                consume(")");

                skipBody();
                break;
            }
            case "method": {
                while (!nextToken().equals(")")) ;
                skipBody();
                break;
            }
        }
    }

    private void skipBody() throws ParserException {
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
        switch (nextToken()) {
            case "class":
                while (!nextToken().equals(")")) ;
                break;
            case "def": {
                if (currentToken().equals("lazy")) {
                    consume("lazy");
                }
                FunctionNode node = (FunctionNode) item(packageNode, nextToken());
                locals = HashTreePMap.empty();
                for (Node child : node.children()) {
                    if (!(child instanceof VariableNode)) {
                        break;
                    }
                    VariableNode variableNode = (VariableNode) child;
                    locals = locals.plus(variableNode.name(), variableNode);
                }
                while (!nextToken().equals(")")) ;
                consume("(");
                while (pos < tokens.size() && !currentToken().equals(")")) {
                    parseChild(node);
                }

                if (pos < tokens.size()) {
                    nextToken();
                }
                break;
            }
            case "method": {
                ClassNode classNode = (ClassNode) find(nextToken());
                FunctionNode fn = (FunctionNode) find(nextToken());
                OverrideNode node = append(packageNode, new OverrideNode());
                append(node, new ReferenceNode(fn));

                superFunction = findSuper(classNode, fn);


                locals = HashTreePMap.empty();

                for (Node child : fn.children()) {
                    if (!(child instanceof VariableNode)) {
                        break;
                    }
                    VariableNode variableNode = (VariableNode) child;
                    locals = locals.plus(variableNode.name(), variableNode);
                }

                while (!nextToken().equals(")")) ;
                consume("(");
                while (pos < tokens.size() && !currentToken().equals(")")) {
                    parseChild(node);
                }

                if (pos < tokens.size()) {
                    nextToken();
                }

                ClassType.of(classNode).overrides().put(fn, node);
                break;
            }
            case "field":
                nextToken();
                consume(")");
                break;

        }
    }

    private Signature findSuper(ClassNode classNode, FunctionNode fn) {
        ClassType t = ClassType.of(classNode);
        Signature s = t.implementation(fn);

        while (t != null) {
            Signature s1 = t.implementation(fn);
            if (s1 != s) {
                return s1;
            }

            t = t.superClass();
        }
        return fn;
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
                case "list":
                case "break":
                case "continue": {
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
                    var.name(nextToken());
                    append(node, var);

                    HashPMap savedLocals = locals;
                    locals = locals.plus(var.name(), var);

                    while (!currentToken().equals(")")) {
                        parseChild(node);
                    }
                    locals = savedLocals;
                    break;
                }
                case "=": {
                    String name = nextToken();
                    AssignmentNode node = append(parent, new AssignmentNode());

                    boolean isField = false;
                    int savedPos = pos;

                    NamedNode f = find(name);
                    if (isField(f)) {
                        ReferenceNode r = append(node, new ReferenceNode(f));
                        parseChild(r);
                        if (!currentToken().equals(")")) {
                            parseChild(node);
                            isField = true;
                        }
                    }

                    if (!isField) {
                        pos = savedPos;
                        node.children().clear();
                        if (locals.containsKey(name)) {
                            append(node, new ReferenceNode(locals.get(name)));
                            parseChild(node);
                        } else {
                            VariableNode lhs = append(node, new VariableNode().name(name));
                            locals = locals.plus(name, lhs);
                            parseChild(node);
                        }
                    }
                    break;
                }
                case "λ":
                case "\\": {
                    LambdaNode node = append(parent, new LambdaNode());
                    HashPMap<String, NamedNode> savedLocals = locals;
                    while (!currentToken().equals(")")) {
                        VariableNode param = append(node, new VariableNode().name(nextToken()));
                        locals = locals.plus(param.name(), param);
                    }
                    consume(")");
                    consume("(");
                    while (!currentToken().equals(")")) {
                        parseChild(node);
                    }
                    locals = savedLocals;
                    break;
                }

                default: {
                    Node headNode = "super".equals(head) ? (Node) superFunction : find(head);

                    if (isField(headNode)) {
                        int savedPos = pos;
                        VariableNode fn = (VariableNode) headNode;
                        ReferenceNode r = append(parent, new ReferenceNode(fn));
                        if (currentToken().equals(")")) {
                            pos = savedPos;
                            parent.children().remove(parent.children().size() - 1);
                        } else {
                            parseChild(r);
                            return;
                        }
                    }

                    if (locals.containsKey(head)) {
                        append(parent, new ReferenceNode(locals.get(head)));
                    } else if (headNode instanceof FunctionNode) {
                        FunctionNode fn = (FunctionNode) headNode;
                        ReferenceNode r = append(parent, new ReferenceNode(fn));
                        for (Node param : fn.children()) {
                            if (!(param instanceof VariableNode)) {
                                break;
                            }
                            parseChild(r);
                        }
                    } else if (headNode instanceof OverrideNode) {
                        FunctionNode fn = (FunctionNode) ((ReferenceNode) headNode.children().get(0)).ref();
                        ReferenceNode r = append(parent, new ReferenceNode(headNode));
                        for (Node param : fn.children()) {
                            if (!(param instanceof VariableNode)) {
                                break;
                            }
                            parseChild(r);
                        }
                    } else if (headNode instanceof ClassNode) {
                        ClassNode cn = (ClassNode) headNode;
                        ReferenceNode r = append(parent, new ReferenceNode(cn));
                        while (!currentToken().equals(")")) {
                            parseChild(r);
                        }
                    } else if (Lexer.isLiteral(head)) {
                        append(parent, new LiteralNode(head));
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
                if (item(packageNode, name) != null) {
                    return item(packageNode, name);
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
            case "break":
                return new BreakNode();
            case "continue":
                return new ContinueNode();
        }
        return null;
    }

    private <T extends Node> T append(Node<?> parent, T node) {
        if (node instanceof ReferenceNode) {
            InsertReference change = new InsertReference()
                    .id(Change.newId())
                    .parent(parent.id())
                    .prev(parent.children().isEmpty() ? null : parent.children().get(parent.children().size() - 1).id())
                    .next(null)
                    .ref(((ReferenceNode) node).ref().id());
            workspace.apply(Collections.singletonList(change));
            return workspace.node(change.id());
        } else {
            if (node instanceof NamedNode) {
                node.id(parent.id() + ":" + ((NamedNode) node).name());
            } else {
                node.id(Change.newId());
            }
            InsertNode change = new InsertNode()
                    .node(node)
                    .parent(parent.id())
                    .prev(parent.children().isEmpty() ? null : parent.children().get(parent.children().size() - 1).id());
            workspace.apply(Collections.singletonList(change));
            return workspace.node(node.id());
        }

    }
}
