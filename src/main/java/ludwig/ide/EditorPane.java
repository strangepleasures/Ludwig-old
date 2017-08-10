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
import javafx.util.StringConverter;
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

public class EditorPane extends SplitPane {
    private final Environment environment;
    private final Settings settings;
    private final ListView<NamedNode> membersList = new ListView<>();
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
                        showEditor(e.getText());
                    }
            }
            e.consume();
        });

        codeView.setOnKeyReleased(Event::consume);
        codeView.setOnKeyTyped(e -> e.consume());

        membersList.getSelectionModel().selectedItemProperty().addListener(observable -> {
            displayMember();
        });

        membersList.setCellFactory(listView -> new ListCell<NamedNode>() {
            @Override
            protected void updateItem(NamedNode item, boolean empty) {
                super.updateItem(item, empty);

                setText((!empty && item != null) ? NodeUtils.signature(item) : "");
            }
        });

        getItems().addAll(packageTree, membersList, new VBox(signatureView, /*signatureToolbar,*/ lazyCheckbox, codeView));

        membersList.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> {
            if (event.getClickCount() == 2
                && membersList.getSelectionModel().selectedItemProperty().getValue() != null
                && anotherPane != null) {
                anotherPane.insertNode(membersList.getSelectionModel().selectedItemProperty().getValue());
            }
        });


        MenuItem addFunctionMenuItem = new MenuItem("Add...", Icons.icon("add"));
        addFunctionMenuItem.setOnAction(e -> addFunction());

        MenuItem runMenu = new MenuItem("Run...", Icons.icon("run"));
        runMenu.setOnAction(e -> runFunction());

        membersList.setContextMenu(new ContextMenu(
            addFunctionMenuItem,
            runMenu
        ));

        environment.getWorkspace().changeListeners().add(this::processChanges);
    }

    private void deleteNode() {

    }

    private void runFunction() {
        NamedNode selectedItem = selectedMember();
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
                dialog.setContentText(fn.getName());
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
                        .setNode(new FunctionNode().setName(parts.get(0)).id(Change.newId()))
                        .setParent(packageNode.id())
                        .setPrev(packageNode.children().isEmpty() ? null : packageNode.children().get(packageNode.children().size() - 1).id());

                    changes.add(insertFn);

                    String prev = null;
                    for (int i = 1; i < parts.size(); i++) {
                        String id = Change.newId();
                        changes.add(new InsertNode()
                            .setNode(new VariableNode().setName(parts.get(i)).id(id))
                            .setParent(insertFn.getNode().id())
                            .setPrev(prev));
                        prev = id;
                    }
                    changes.add(new InsertNode()
                        .setNode(new SeparatorNode().id(Change.newId()))
                        .setParent(insertFn.getNode().id())
                        .setPrev(prev));

                    environment.getWorkspace().apply(changes);

                    FunctionNode fn = environment.getWorkspace().node(insertFn.getNode().id());
                    navigateTo(fn);
                }

            });
        }
    }

    private void displayMember() {
        NamedNode selectedItem = selectedMember();
        signatureView.getChildren().clear();
        codeView.setText("");

        if (selectedItem == null) {
            return;
        }

        signatureView.add(new Label("Name"), 1, 1);
        signatureView.add(new Label("Description"), 2, 1);
        signatureView.add(nameTextField(selectedItem), 1, 2);
        signatureView.add(commentTextField(selectedItem), 2, 2);

        if (selectedItem instanceof FunctionNode) {
            FunctionNode fn = (FunctionNode) selectedItem;

            int row = 3;
            for (Node n : fn.children()) {
                if (n instanceof SeparatorNode) {
                    break;
                }
                signatureView.add(nameTextField((NamedNode) n), 1, row);
                signatureView.add(commentTextField(n), 2, row);
                row++;
            }
            lazyCheckbox.setSelected(fn.isLazy());

            codeView.setText(PrettyPrinter.print(fn));
        }
    }

    private NamedNode<?> selectedMember() {
        return membersList.getSelectionModel().getSelectedItem();
    }

    private void showEditor(String text) {
        Popup popup = new Popup();
        TextField autoCompleteTextField = new TextField();

        AutoCompletionTextFieldBinding<NamedNode> autoCompletionTextFieldBinding =
            new AutoCompletionTextFieldBinding<>(
                autoCompleteTextField,
                param -> {
                    List<NamedNode> locals = new ArrayList<>();
                    if (selectedMember() instanceof FunctionNode) {
                        for (Node child : selectedMember().children()) {
                            if (child instanceof SeparatorNode) {
                                break;
                            }
                            if (((VariableNode) child).getName().startsWith(param.getUserText())) {
                                locals.add((VariableNode) child);
                            }
                        }
                    }
                    SortedSet<NamedNode> symbols = environment.getSymbolRegistry().symbols(param.getUserText());
                    locals.addAll(symbols);
                    return locals;
                },
                new NamedNodeStringConverter());

        autoCompletionTextFieldBinding.setVisibleRowCount(20);
        NamedNode[] ref = {null};
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
        autoCompleteTextField.deselect();
        Platform.runLater(() -> {
            autoCompleteTextField.setText(text);
            autoCompleteTextField.selectRange(text.length(), text.length());
        });
    }

    private void processChanges(Change change) {
        if (!environment.getWorkspace().isBatchUpdate()) {
            refresh();
        }
    }

    private void gotoDefinition(ReferenceNode node) {
        navigateTo(node.ref());
    }

    private void navigateTo(NamedNode<?> node) {
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
        Insert head = (node instanceof NamedNode) ? new InsertReference().setId(Change.newId()).setRef(node.id()) : new InsertNode().setNode(node);
        List<Change> changes = new ArrayList<>();
        NamedNode<?> selectedItem = selectedMember();
        if (!(selectedItem instanceof FunctionNode) || isReadonly()) {
            return;
        }
        FunctionNode target = (FunctionNode) selectedItem;
        if (sel != null) {
            head.setParent(sel.parent().id());
            int index = sel.parent().children().indexOf(sel);
            head.setPrev(index == 0 ? null : sel.parent().children().get(index - 1).id());
            head.setNext(index == sel.parent().children().size() - 1 ? null : sel.parent().children().get(index + 1).id());
            changes.add(new Delete().setId(sel.id()));
        } else {
            head.setParent(target.id());
            head.setPrev(target.children().get(target.children().size() - 1).id());
        }

        changes.add(head);

        String prev = null;
        if (node instanceof FieldNode) {
            InsertNode insertPlaceholder = new InsertNode()
                .setNode(new PlaceholderNode("it").id(Change.newId()))
                .setParent(((InsertReference) head).getId())
                .setPrev(prev);
            changes.add(insertPlaceholder);
        } else for (Node<?> child : node.children()) {
            if (child instanceof SeparatorNode) {
                break;
            }
            InsertNode insertPlaceholder = new InsertNode()
                .setNode(new PlaceholderNode(((VariableNode) child).getName()).id(Change.newId()))
                .setParent(((InsertReference) head).getId())
                .setPrev(prev);
            changes.add(insertPlaceholder);
            prev = insertPlaceholder.getNode().id();
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
                    .map(item -> (NamedNode) item)
                    .sorted(Comparator.comparing(NodeUtils::signature))
                    .collect(Collectors.toList())));
            }

            if (!membersList.getItems().isEmpty()) {
                membersList.getSelectionModel().select(0);
            }
        }
    }

    private Node selectedNode(int pos) {
        if (membersList.getSelectionModel() != null) {
            NamedNode selectedItem = selectedMember();
            if (!(selectedItem instanceof FunctionNode)) {
                return null;
            }
            FunctionNode node = (FunctionNode) selectedItem;
            List<Node> nodes = NodeUtils.expandNode(node);
            int index = EditorUtils.tokenIndex(codeView.getText(), pos);
            if (index < 0) {
                return null;
            }
            for (int i = 0; i < nodes.size(); i++) {
                if (nodes.get(i) instanceof SeparatorNode) {
                    if (index + i + 1 < nodes.size()) {
                        return nodes.get(index + i + 1);
                    }
                }
            }
        }
        return null;
    }

    private Node selectedNode() {
        return selectedNode(codeView.getSelection().getStart());
    }

    private TextField commentTextField(Node<?> node) {
        TextField textField = new TextField(node.getComment()) {
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
                environment.getWorkspace().apply(Collections.singletonList(new Comment().setNodeId(node.id()).setComment(getText())));
            }
        };
        return textField;
    }

    private TextField nameTextField(NamedNode<?> node) {
        TextField textField = new TextField(node.getName()) {
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
                environment.getWorkspace().apply(Collections.singletonList(new Rename().setNodeId(node.id()).setName(getText())));
            }

        };
        return textField;
    }

    void refresh() {
        NamedNode packageSelection = null;
        NamedNode memberSelection = null;
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

    private static class NamedNodeStringConverter extends StringConverter<NamedNode> {
        @Override
        public String toString(NamedNode object) {
            return NodeUtils.signature(object);
        }

        @Override
        public NamedNode fromString(String string) {
            return null;
        }
    }

    private boolean isReadonly() {
        return isReadonly(membersList.getSelectionModel().getSelectedItem());
    }

    private static boolean isReadonly(Node<?> node) {
        return node == null || node.parentOfType(ProjectNode.class).isReadonly();
    }
}
