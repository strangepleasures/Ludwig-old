package ludwig.ide;

import com.sun.javafx.collections.ObservableListWrapper;
import com.sun.javafx.scene.control.skin.TextAreaSkin;
import impl.org.controlsfx.autocompletion.AutoCompletionTextFieldBinding;
import javafx.application.Platform;
import javafx.event.Event;
import javafx.geometry.Bounds;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Popup;
import lombok.Getter;
import lombok.Setter;
import ludwig.changes.*;
import ludwig.interpreter.*;
import ludwig.model.*;
import ludwig.script.Lexer;
import ludwig.script.LexerException;
import ludwig.utils.NodeUtils;
import ludwig.utils.PrettyPrinter;
import ludwig.workspace.Environment;

import java.io.IOException;
import java.io.StringReader;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.Collections.singletonList;
import static ludwig.utils.NodeUtils.arguments;

public class EditorPane extends SplitPane {
    private final Environment environment;
    private final Settings settings;
    private final ListView<Node> membersList = new ListView<>();
    private final PackageTreeView packageTree;
    private final GridPane signatureView = new GridPane();
    private final ToolBar signatureToolbar;
    private final CheckBox lazyCheckbox = new CheckBox("Lazy");
    private final TextArea codeView = new TextArea();

    @Getter
    @Setter
    private EditorPane anotherPane;

    public EditorPane(Environment environment, Settings settings) {
        this.environment = environment;
        this.settings = settings;

        packageTree = new PackageTreeView(environment.getWorkspace());
        packageTree.setMinWidth(120);

        membersList.setMinWidth(120);

        packageTree.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            fillMembers();
        });

        membersList.setPrefHeight(1E6);


        TableColumn<NamedNode, String> nameColumn = new TableColumn<>("Name");
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        TableColumn<NamedNode, String> descriptionColumn = new TableColumn<>("Description");
        descriptionColumn.setCellValueFactory(new PropertyValueFactory<>("comment"));
        descriptionColumn.setCellFactory(TextFieldTableCell.forTableColumn());

        Button addParameterButton = new Button("", Icons.icon("add"));
        addParameterButton.setOnAction(e -> {


        });

        Button removeParameterButton = new Button("", Icons.icon("delete"));
        removeParameterButton.setOnAction(e -> {

        });
        Button moveParameterUpButton = new Button("", Icons.icon("up"));
        moveParameterUpButton.setOnAction(e -> {

        });
        Button moveParameterDownButton = new Button("", Icons.icon("down"));
        moveParameterDownButton.setOnAction(e -> {

        });
        signatureToolbar = new ToolBar(addParameterButton, removeParameterButton, moveParameterUpButton, moveParameterDownButton);

        codeView.setPrefHeight(1E6);


        MenuItem gotoDefinitionMenuItem = new MenuItem("Go to definition");

        gotoDefinitionMenuItem.setOnAction(e -> {
            Node sel = selectedNode();
            if (sel instanceof ReferenceNode) {
                gotoDefinition((ReferenceNode) sel);
            }
        });
        codeView.setContextMenu(new ContextMenu(gotoDefinitionMenuItem));

        codeView.setOnKeyPressed(e -> {
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
                    if (!isReadonly() && e.getText() != null && !e.getText().isEmpty()) {
                        showEditor(e.getText(), false);
                    }
            }
            e.consume();
        });

        codeView.setOnKeyReleased(Event::consume);
        codeView.setOnKeyTyped(Event::consume);

        codeView.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2) {
                if (isReadonly()) {
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

        membersList.getSelectionModel().selectedItemProperty().addListener(observable -> displayMember());

        membersList.setCellFactory(listView -> new SignatureListCell());

        getItems().addAll(packageTree, membersList, new VBox(signatureView, /*signatureToolbar,*/ lazyCheckbox, codeView));

        membersList.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> {
            if (event.getClickCount() == 2
                && membersList.getSelectionModel().selectedItemProperty().getValue() != null
                && anotherPane != null) {
                anotherPane.insertNode((Node<?>) membersList.getSelectionModel().selectedItemProperty().getValue());
            }
        });

        MenuItem addPackageMenuItem = new MenuItem("Add...", Icons.icon("add"));
        addPackageMenuItem.setOnAction(e -> addPackage());
        packageTree.setContextMenu(new ContextMenu(addPackageMenuItem));


        MenuItem addFunctionMenuItem = new MenuItem("Add Function...", Icons.icon("add"));
        addFunctionMenuItem.setOnAction(e -> addFunction());

        MenuItem overrideFunctionMenuItem = new MenuItem("Override...", Icons.icon("add"));
        overrideFunctionMenuItem.setOnAction(e -> overrideFunction());

        MenuItem runMenuItem = new MenuItem("Run...", Icons.icon("run"));
        runMenuItem.setOnAction(e -> runFunction());

        MenuItem deleteFunctionMenuItem = new MenuItem("Delete", null);
        deleteFunctionMenuItem.setOnAction(e -> deleteFunction());

        membersList.setContextMenu(new ContextMenu(
            addFunctionMenuItem,
            overrideFunctionMenuItem,
            runMenuItem,
            deleteFunctionMenuItem
        ));

        environment.getWorkspace().changeListeners().add(this::processChanges);
    }

    private void deleteFunction() {
        if (isReadonly()) {
            return;
        }
        Node selectedItem = selectedMember();
        if (selectedItem != null) {
            environment.getWorkspace().apply(singletonList(new Delete().id(selectedItem.id())));
        }
    }

    private void overrideFunction() {
        TreeItem<NamedNode> selectedItem = packageTree.getSelectionModel().getSelectedItem();
        if (selectedItem != null && selectedItem.getValue() instanceof PackageNode) {
            PackageNode packageNode = (PackageNode) selectedItem.getValue();

            TextInputDialog dialog = new TextInputDialog();
            dialog.setTitle("Override");
            dialog.setHeaderText("");

            AutoCompletionTextFieldBinding<Node> autoCompletionTextFieldBinding =
                new AutoCompletionTextFieldBinding<>(
                    dialog.getEditor(),
                    param -> {
                        List<Node> suggestions = new ArrayList<>();
                        suggestions.addAll(environment.getSymbolRegistry().symbols(param.getUserText()));

                        return suggestions;
                    },
                    new NodeStringConverter());

            autoCompletionTextFieldBinding.setVisibleRowCount(20);
            Node[] ref = {null};
            autoCompletionTextFieldBinding.setOnAutoCompleted(e -> {
                ref[0] = e.getCompletion();
            });


            dialog.showAndWait().ifPresent(signature -> {
                if (ref[0] != null) {
                    List<Change> changes = new ArrayList<>();

                    InsertNode insertOverride = new InsertNode()
                        .node(new OverrideNode().id(Change.newId()))
                        .parent(packageNode.id());

                    changes.add(insertOverride);

                    changes.add(new InsertReference()
                        .ref(ref[0].id())
                        .id(Change.newId())
                        .parent(insertOverride.node().id()));

                    environment.getWorkspace().apply(changes);

                    OverrideNode o = environment.getWorkspace().node(insertOverride.node().id());
                    navigateTo(o);
                }
            });
        }
    }

    private void addPackage() {
        TreeItem<NamedNode> selectedItem = packageTree.getSelectionModel().getSelectedItem();
        if (selectedItem != null) {
            Node parent = selectedItem.getValue();
            if (NodeUtils.isReadonly(parent)) {
                return;
            }

            TextInputDialog dialog = new TextInputDialog();
            dialog.setTitle("Add a package");
            dialog.setHeaderText("");
            dialog.setContentText("Package name");

            dialog.showAndWait().ifPresent(name -> {
                InsertNode insert = new InsertNode()
                    .node(new PackageNode().name(name).id(Change.newId()))
                    .parent(parent.id());
                environment.getWorkspace().apply(singletonList(insert));
                navigateTo(environment.getWorkspace().node(insert.node().id()));
            });
        }
    }

    private void deleteNode() {
        if (isReadonly()) {
            return;
        }
        Node<?> node = selectedNode();
        if (node == null) {
            return;
        }
        Node<?> parent = node.parent();
        // TODO: implement
    }

    private void runFunction() {
        Node selectedItem = selectedMember();
        if (!(selectedItem instanceof FunctionNode)) {
            return;
        }
        FunctionNode fn = (FunctionNode) selectedItem;
        try {
            Callable callable = (fn instanceof Callable) ? (Callable) fn : new CallableFunction(fn);
            Object result;
            if (callable.argCount() > 0) {
                TextInputDialog dialog = new TextInputDialog();
                dialog.setTitle("Execute function");
                dialog.setHeaderText("Enter function arguments");
                dialog.setContentText(fn.name());
                Optional<String> params = dialog.showAndWait();
                if (params.isPresent()) {
                    Object[] args = Lexer.read(new StringReader(params.get()))
                        .stream()
                        .filter(s -> !s.equals("(") && !s.equals(")"))
                        .map(NodeUtils::parseLiteral)
                        .map(x -> callable.isLazy() ? (Delayed<?>) () -> x : x)
                        .toArray();
                    result = callable.call(args);
                } else {
                    return;
                }

            } else {
                result = callable.call();
            }
            new Alert(Alert.AlertType.INFORMATION, "Result: " + NodeUtils.formatLiteral(result)).show();
        } catch (Exception err) {
            err.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Error: " + err.toString()).show();
        }
    }

    private void addFunction() {
        TreeItem<NamedNode> selectedItem = packageTree.getSelectionModel().getSelectedItem();
        if (selectedItem != null && selectedItem.getValue() instanceof PackageNode) {
            PackageNode packageNode = (PackageNode) selectedItem.getValue();

            TextInputDialog dialog = new TextInputDialog();
            dialog.setTitle("Add a function");
            dialog.setHeaderText("Enter function signature");

            dialog.showAndWait().ifPresent(signature -> {
                List<String> parts = Collections.emptyList();
                try {
                    parts = Lexer.read(new StringReader(signature))
                        .stream()
                        .filter(s -> !s.equals("(") && !s.equals(")"))
                        .collect(Collectors.toList());
                } catch (IOException | LexerException t) {
                }
                if (!parts.isEmpty()) {
                    List<Change> changes = new ArrayList<>();

                    InsertNode insertFn = new InsertNode()
                        .node(new FunctionNode().name(parts.get(0)).id(Change.newId()))
                        .parent(packageNode.id());

                    changes.add(insertFn);

                    String prev = null;
                    for (int i = 1; i < parts.size(); i++) {
                        String id = Change.newId();
                        changes.add(new InsertNode()
                            .node(new VariableNode().name(parts.get(i)).id(id))
                            .parent(insertFn.node().id())
                            .prev(prev));
                        prev = id;
                    }

                    environment.getWorkspace().apply(changes);

                    FunctionNode fn = environment.getWorkspace().node(insertFn.node().id());
                    navigateTo(fn);
                }

            });
        }
    }

    private void displayMember() {
        Node selectedItem = selectedMember();
        signatureView.getChildren().clear();
        codeView.setText("");

        if (selectedItem == null) {
            return;
        }

        NamedNode head = (selectedItem instanceof OverrideNode) ? (NamedNode) ((ReferenceNode) ((OverrideNode) selectedItem).children().get(0)).ref() : (NamedNode) selectedItem;

        signatureView.add(new Label("Name"), 1, 1);
        signatureView.add(new Label("Description"), 2, 1);
        signatureView.add(nameTextField(head), 1, 2);
        signatureView.add(commentTextField(selectedItem), 2, 2);

        if (head instanceof FunctionNode) {
            FunctionNode fn = (FunctionNode) head;

            int row = 3;
            for (Node n : fn.children()) {
                if (!(n instanceof VariableNode)) {
                    break;
                }
                signatureView.add(nameTextField((VariableNode) n), 1, row);
                signatureView.add(commentTextField(n), 2, row);
                row++;
            }
            lazyCheckbox.setSelected(fn.isLazy());

            codeView.setText(PrettyPrinter.print(fn));
        }
    }

    private Node selectedMember() {
        if (membersList.getSelectionModel() == null) {
            return null;
        }
        return membersList.getSelectionModel().getSelectedItem();
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

                    suggestions.addAll(NodeUtils.collectLocals(selectedMember(), selectedNode(), param.getUserText()));
                    suggestions.addAll(environment.getSymbolRegistry().symbols(param.getUserText()));

                    return suggestions;
                },
                new NodeStringConverter());

        autoCompletionTextFieldBinding.setVisibleRowCount(20);
        Node[] ref = {null};
        autoCompletionTextFieldBinding.setOnAutoCompleted(e -> {
            ref[0] = e.getCompletion();
        });

        popup.getContent().add(autoCompleteTextField);
        TextAreaSkin skin = (TextAreaSkin) codeView.getSkin();
        Bounds caretBounds = codeView.localToScreen(skin.getCaretBounds());
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

        popup.show(codeView, caretBounds.getMinX(), caretBounds.getMinY());

        if (!selectAll) {
            Platform.runLater(() -> autoCompleteTextField.selectRange(text.length(), text.length()));
        }
    }

    private void processChanges(Change change) {
        if (!environment.getWorkspace().isBatchUpdate()) {
            refresh();
        }
    }

    private void gotoDefinition(ReferenceNode node) {
        navigateTo(node.ref());
    }

    private void navigateTo(Node<?> node) {
        packageTree.select(node.parentOfType(PackageNode.class));
        FunctionNode fn = node.parentOfType(FunctionNode.class);
        if (fn != null) {
            membersList.getSelectionModel().select(fn);
            Node decl = node.parentOfType(AssignmentNode.class);
            if (decl == null) {
                decl = node.parentOfType(ForNode.class);
            }
            if (decl == null) {
                decl = node.parentOfType(LambdaNode.class);
            }
            if (decl != null) {
                locate(decl);
            }
        }
    }

    private void locate(Node node) {
        for (int i = 0; i < codeView.getText().length(); i++) {
            if (selectedNode(i) == node) {
                codeView.selectRange(i, i);
                break;
            }
        }
    }

    private void insertNode(Node<?> node) {
        Node<?> sel = selectedNode();
        Insert head = (node instanceof NamedNode) ? new InsertReference().id(Change.newId()).ref(node.id()) : new InsertNode().node(node);
        List<Change> changes = new ArrayList<>();
        Node selectedItem = selectedMember();
        if (!(selectedItem instanceof FunctionNode) || isReadonly()) {
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


    private void fillMembers() {
        membersList.getItems().clear();
        TreeItem<NamedNode> selectedItem = packageTree.getSelectionModel().getSelectedItem();
        if (selectedItem != null) {
            NamedNode node = selectedItem.getValue();

            if (node instanceof PackageNode) {
                PackageNode packageNode = (PackageNode) node;
                membersList.setItems(new ObservableListWrapper<>(packageNode.children()
                    .stream()
                    .filter(item -> !(item instanceof PackageNode))
                    .map(item -> (Node<?>) item)
                    .sorted(Comparator.comparing(NodeUtils::signature))
                    .collect(Collectors.toList())));
            }

            if (!membersList.getItems().isEmpty()) {
                membersList.getSelectionModel().select(0);
            }
        }
    }

    private Node selectedNode(int pos) {
        Node selectedItem = selectedMember();
        if (!(selectedItem instanceof FunctionNode)) {
            return null;
        }
        FunctionNode node = (FunctionNode) selectedItem;
        List<Node> nodes = NodeUtils.expandNode(node);
        int index = EditorUtils.tokenIndex(codeView.getText(), pos);
        if (index < 0) {
            return null;
        }

        return nodes.get(index + arguments(node).size());
    }

    private Node selectedNode() {
        return selectedNode(codeView.getSelection().getStart());
    }

    private TextField commentTextField(Node<?> node) {
        return new TextField(node.comment()) {
            private String saved;

            {
                setOnAction(e -> applyChanges());

                this.focusedProperty().addListener(e -> {
                    if (focusedProperty().get()) {
                        saved = getText();
                    } else {
                        if (!Objects.equals(getText(), saved)) {
                            applyChanges();
                        }
                    }
                });

                setEditable(!isReadonly());
            }

            private void applyChanges() {
                environment.getWorkspace().apply(singletonList(new Comment().nodeId(node.id()).comment(getText())));
            }
        };
    }

    private TextField nameTextField(NamedNode node) {
        return new TextField(node.name()) {
            private String saved;

            {
                setOnAction(e -> applyChanges());

                this.focusedProperty().addListener(e -> {
                    if (focusedProperty().get()) {
                        saved = getText();
                    } else {
                        if (!getText().equals(saved)) {
                            applyChanges();
                        }
                    }
                });

                setEditable(!isReadonly());
            }

            private void applyChanges() {
                environment.getWorkspace().apply(singletonList(new Rename().setNodeId(node.id()).name(getText())));
            }
        };
    }

    private void refresh() {
        Node packageSelection = null;
        Node memberSelection = null;
        NamedNode signatureSelection = null;
        Node codeSelection = null;

        TreeItem<NamedNode> selectedItem = packageTree.getSelectionModel().getSelectedItem();
        if (selectedItem != null) {
            packageSelection = selectedItem.getValue();
        }

        memberSelection = selectedMember();

        codeSelection = selectedNode();

        packageTree.refresh();
        packageTree.getSelectionModel().clearSelection();
        membersList.getSelectionModel().clearSelection();

        if (memberSelection != null) {
            navigateTo(memberSelection);
        } else if (packageSelection != null) {
            navigateTo(packageSelection);
        }

        if (codeSelection != null) {
            locate(codeSelection);
        }
    }

    private void selectNextNode() {
        int start = codeView.getSelection().getStart();
        Node current = selectedNode(start);
        int length = codeView.getText().length();
        for (int i = start + 1; i < length; i++) {
            if (selectedNode(i) != current) {
                codeView.selectRange(i, i);
                break;
            }
        }
    }

    private void selectPrevNode() {
        int start = codeView.getSelection().getStart();
        Node current = selectedNode(start);

        for (int i = start - 1; i >= 0; i--) {
            Node sel = selectedNode(i);
            if (sel != current) {
                codeView.selectRange(i, i);
                for (int j = i - 1; j >= 0; j--) {
                    if (selectedNode(j) != sel) {
                        codeView.selectRange(j + 1, j + 1);
                        return;
                    }
                }
            }
        }

        codeView.selectRange(0, 0);
    }

    private void selectNextLine() {
        String text = codeView.getText();
        int start = codeView.getSelection().getStart();
        int length = text.length();
        for (int i = start + 1; i < length; i++) {
            if (text.charAt(i) == '\n') {
                for (int j = i + 1; j < length; j++) {
                    if (text.charAt(j) != ' ') {
                        codeView.selectRange(j, j);
                        return;
                    }
                }

                break;
            }
        }
        codeView.selectRange(length + 1, length + 1);
    }

    private void selectPrevLine() {
        String text = codeView.getText();
        int start = codeView.getSelection().getStart();
        boolean first = true;
        for (int i = start - 1; i >= 0; i--) {
            if (text.charAt(i) == '\n') {
                if (first) {
                    first = false;
                } else {
                    codeView.selectRange(i, i);
                    selectNextNode();
                    return;
                }
            }
        }
        codeView.selectRange(0, 0);
    }

    private boolean isReadonly() {
        return NodeUtils.isReadonly(membersList.getSelectionModel().getSelectedItem());
    }
}
