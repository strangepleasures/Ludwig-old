package ludwig.ide;

import com.sun.javafx.scene.control.skin.TextAreaSkin;
import impl.org.controlsfx.autocompletion.AutoCompletionTextFieldBinding;
import javafx.application.Platform;
import javafx.event.Event;
import javafx.geometry.Bounds;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.stage.Popup;
import ludwig.changes.*;
import ludwig.model.*;
import ludwig.script.Lexer;
import ludwig.utils.NodeUtils;
import ludwig.utils.PrettyPrinter;
import ludwig.workspace.Environment;

import java.util.ArrayList;
import java.util.List;

import static ludwig.utils.NodeUtils.arguments;
import static ludwig.utils.NodeUtils.isReadonly;

public class CodeEditor extends TextArea {
    private final Environment environment;
    private Node<?> node;

    public CodeEditor(Environment environment) {

        this.environment = environment;

        setOnKeyPressed(e -> {
            switch (e.getCode()) {
                case LEFT:
                    selectPrevNode();
                    break;
                case RIGHT:
                    selectNextNode();
                    break;
                case UP:
                    selectPrevLine();
                    break;
                case DOWN:
                    selectNextLine();
                    break;
                case BACK_SPACE:
                    selectPrevNode();
                case DELETE:
                    deleteNode();
                    break;
                default:
                    if (!isEditable() && e.getText() != null && !e.getText().isEmpty()) {
                        showEditor(e.getText(), false);
                    }
            }
            e.consume();
        });

        setOnKeyReleased(Event::consume);
        setOnKeyTyped(Event::consume);

        setOnMouseClicked(e -> {
            if (e.getClickCount() == 2) {
                if (!isEditable()) {
                    return;
                }

                Node n = selectedNode();

                if (n == null) {
                    return;
                }
                if (n instanceof PlaceholderNode) {
                    showEditor("", true);
                } else {
                    showEditor(n.toString(), true);
                }
            }
        });
    }

    private void deleteNode() {

    }

    public void setContent(Node<?> node) {
        this.node = node;
        if (node == null) {
            setText("");
            setEditable(false);
        } else {
            setText(PrettyPrinter.print(node));
            setEditable(!isReadonly(node));
        }
    }

    private Node selectedNode(int pos) {
        if (node == null) {
            return null;
        }
        List<Node> nodes = NodeUtils.expandNode(node);
        int index = EditorUtils.tokenIndex(getText(), pos);
        if (index < 0) {
            return null;
        }

        return nodes.get(index + arguments(node).size());
    }

    public Node selectedNode() {
        return selectedNode(getSelection().getStart());
    }

    public void locate(Node node) {
        for (int i = 0; i < getText().length(); i++) {
            if (selectedNode(i) == node) {
                selectRange(i, i);
                break;
            }
        }
    }

    private void selectNextNode() {
        int start = getSelection().getStart();
        Node current = selectedNode(start);
        int length = getText().length();
        for (int i = start + 1; i < length; i++) {
            if (selectedNode(i) != current) {
                selectRange(i, i);
                break;
            }
        }
    }

    private void selectPrevNode() {
        int start = getSelection().getStart();
        Node current = selectedNode(start);

        for (int i = start - 1; i >= 0; i--) {
            Node sel = selectedNode(i);
            if (sel != current) {
                selectRange(i, i);
                for (int j = i - 1; j >= 0; j--) {
                    if (selectedNode(j) != sel) {
                        selectRange(j + 1, j + 1);
                        return;
                    }
                }
            }
        }

        selectRange(0, 0);
    }

    private void selectNextLine() {
        String text = getText();
        int start = getSelection().getStart();
        int length = text.length();
        for (int i = start + 1; i < length; i++) {
            if (text.charAt(i) == '\n') {
                for (int j = i + 1; j < length; j++) {
                    if (text.charAt(j) != ' ') {
                        selectRange(j, j);
                        return;
                    }
                }

                break;
            }
        }
        selectRange(length + 1, length + 1);
    }

    private void selectPrevLine() {
        String text = getText();
        int start = getSelection().getStart();
        boolean first = true;
        for (int i = start - 1; i >= 0; i--) {
            if (text.charAt(i) == '\n') {
                if (first) {
                    first = false;
                } else {
                    selectRange(i, i);
                    selectNextNode();
                    return;
                }
            }
        }
        selectRange(0, 0);
    }

    public void insertNode(Node<?> node) {
        Node<?> sel = selectedNode();
        Insert head = (node instanceof NamedNode) ? new InsertReference().id(Change.newId()).ref(node.id()) : new InsertNode().node(node);
        List<Change> changes = new ArrayList<>();
        Node selectedItem = this.node;
        if (!(selectedItem instanceof FunctionNode) || !isEditable()) {
            return;
        }
        FunctionNode target = (FunctionNode) selectedItem;
        if (sel != null) {
            head.parent(sel.parent().id());
            int index = sel.parent().children().indexOf(sel);
            head.prev(index == 0 ? null : sel.parent().children().get(index - 1).id());
            head.next(index == sel.parent().children().size() - 1 ? null : sel.parent().children().get(index + 1).id());
            changes.add(new Delete().id(sel.id()));
        } else {
            head.parent(target.id());
            head.prev(target.children().isEmpty() ? null : target.children().get(target.children().size() - 1).id());
        }

        changes.add(head);

        String prev = null;

        for (String arg : arguments(node)) {
            InsertNode insertPlaceholder = new InsertNode()
                .node(new PlaceholderNode().setParameter(arg).id(Change.newId()))
                .parent(((InsertReference) head).id())
                .prev(prev);
            changes.add(insertPlaceholder);
            prev = insertPlaceholder.node().id();
        }


        environment.getWorkspace().apply(changes);

        selectNextNode();
    }


    private void showEditor(String text, boolean selectAll) {
        Popup popup = new Popup();
        TextField autoCompleteTextField = new TextField();
        autoCompleteTextField.setText(text);

        AutoCompletionTextFieldBinding<Node> autoCompletionTextFieldBinding =
            new AutoCompletionTextFieldBinding<>(
                autoCompleteTextField,
                param -> {
                    List<Node> suggestions = new ArrayList<>();
                    if (param.getUserText().isEmpty() || "= variable value".startsWith(param.getUserText())) {
                        suggestions.add(new AssignmentNode()
                            .add(new PlaceholderNode().setParameter("variable").id(Change.newId()))
                            .add(new PlaceholderNode().setParameter("value").id(Change.newId())));
                    }
                    if (param.getUserText().isEmpty() || param.getUserText().startsWith("λ") || param.getUserText().startsWith("\\")) {
                        suggestions.add(new LambdaNode()
                            .add(new PlaceholderNode().setParameter("args...").id(Change.newId())));
                    }
                    if (param.getUserText().isEmpty() || "ref fn".startsWith(param.getUserText())) {
                        suggestions.add(new FunctionReferenceNode()
                            .add(new PlaceholderNode().setParameter("fn").id(Change.newId())));
                    }
                    if (param.getUserText().isEmpty() || "call fn args...".startsWith(param.getUserText())) {
                        suggestions.add(new CallNode()
                            .add(new PlaceholderNode().setParameter("fn").id(Change.newId()))
                            .add(new PlaceholderNode().setParameter("args...").id(Change.newId())));
                    }
                    if (param.getUserText().isEmpty() || "if condition statements...>".startsWith(param.getUserText())) {
                        suggestions.add(new IfNode()
                            .add(new PlaceholderNode().setParameter("condition").id(Change.newId()))
                            .add(new PlaceholderNode().setParameter("statements...").id(Change.newId())));
                    }
                    if (param.getUserText().isEmpty() || "else statements...".startsWith(param.getUserText())) {
                        suggestions.add(new ElseNode()
                            .add(new PlaceholderNode().setParameter("statements...").id(Change.newId())));
                    }
                    if (param.getUserText().isEmpty() || "for var seq statements...".startsWith(param.getUserText())) {
                        suggestions.add(new ForNode()
                            .add(new PlaceholderNode().setParameter("var").id(Change.newId()))
                            .add(new PlaceholderNode().setParameter("seq").id(Change.newId()))
                            .add(new PlaceholderNode().setParameter("statements...").id(Change.newId())));
                    }
                    if (param.getUserText().isEmpty() || "break loop-var".startsWith(param.getUserText())) {
                        suggestions.add(new BreakNode()
                            .add(new PlaceholderNode().setParameter("loop-var").id(Change.newId())));
                    }
                    if (param.getUserText().isEmpty() || "continue loop-var".startsWith(param.getUserText())) {
                        suggestions.add(new ContinueNode()
                            .add(new PlaceholderNode().setParameter("loop-var").id(Change.newId())));
                    }
                    if (param.getUserText().isEmpty() || "return result".startsWith(param.getUserText())) {
                        suggestions.add(new ReturnNode()
                            .add(new PlaceholderNode().setParameter("result").id(Change.newId())));
                    }

                    suggestions.addAll(NodeUtils.collectLocals(this.node, selectedNode(), param.getUserText()));
                    suggestions.addAll(environment.getSymbolRegistry().symbols(param.getUserText()));

                    return suggestions;
                },
                new NodeStringConverter());

        autoCompletionTextFieldBinding.setVisibleRowCount(20);
        Node[] ref = {null};
        autoCompletionTextFieldBinding.setOnAutoCompleted(e -> ref[0] = e.getCompletion());

        popup.getContent().add(autoCompleteTextField);
        TextAreaSkin skin = (TextAreaSkin) getSkin();
        Bounds caretBounds = localToScreen(skin.getCaretBounds());
        autoCompleteTextField.setOnAction(ev -> {
            if (Lexer.isLiteral(autoCompleteTextField.getText())) {
                insertNode(new LiteralNode(autoCompleteTextField.getText()).id(Change.newId()));
            } else if (ref[0] != null) {
                insertNode(ref[0]);
            } else if (!autoCompleteTextField.getText().isEmpty()) {
                insertNode(new VariableNode().name(autoCompleteTextField.getText()).id(Change.newId()));
            }
            popup.hide();
            autoCompletionTextFieldBinding.dispose();
        });
        autoCompleteTextField.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ESCAPE) {
                popup.hide();
                autoCompletionTextFieldBinding.dispose();
            }
        });

        popup.show(this, caretBounds.getMinX(), caretBounds.getMinY());

        if (!selectAll) {
            Platform.runLater(() -> autoCompleteTextField.selectRange(text.length(), text.length()));
        }
    }
}
