package ludwig.ide;

import com.sun.javafx.scene.control.skin.TextAreaSkin;
import javafx.beans.binding.Bindings;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import lombok.Getter;
import lombok.Setter;
import ludwig.changes.Change;
import ludwig.changes.InsertNode;
import ludwig.interpreter.*;
import ludwig.model.*;
import ludwig.script.Lexer;
import ludwig.script.LexerException;
import ludwig.utils.NodeUtils;
import ludwig.utils.PrettyPrinter;

import java.io.IOException;
import java.io.StringReader;
import java.util.*;

public class EditorPane extends SplitPane {
    private final App app;
    private final ListView<FunctionNode> membersList = new ListView<>();
    private final PackageTreeView packageTree;
    private final TextArea codeView = new TextArea();

    @Getter
    @Setter
    private EditorPane anotherPane;

    public EditorPane(App app) {
        this.app = app;

        packageTree = new PackageTreeView(app.getWorkspace());
        packageTree.setMinWidth(120);


        membersList.setMinWidth(120);

        packageTree.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            fillMembers();
        });


        membersList.setPrefHeight(1E6);

        VBox methodPane = new VBox();
        TableView<NamedNode> signatureView = new TableView<>();
        methodPane.getChildren().add(signatureView);

        TableColumn<NamedNode, String> nameColumn = new TableColumn<>("Name");
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        TableColumn<NamedNode, String> descriptionColumn = new TableColumn<>("Description");
        descriptionColumn.setCellValueFactory(new PropertyValueFactory<>("comment"));
        signatureView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        signatureView.getColumns().addAll(nameColumn, descriptionColumn);
        signatureView.setFixedCellSize(25);

        signatureView.prefHeightProperty().bind(Bindings.size(signatureView.getItems()).multiply(signatureView.getFixedCellSize()).add(30));
        signatureView.minHeightProperty().bind(signatureView.prefHeightProperty());
        signatureView.maxHeightProperty().bind(signatureView.prefHeightProperty());

        codeView.setEditable(false);
        codeView.setPrefHeight(1E6);
        methodPane.getChildren().add(codeView);

        codeView.setOnMouseClicked(e -> {
            Node sel = selectedNode(getPosition(e));
            if (e.isControlDown()) {
                if (sel instanceof ReferenceNode) {
                    gotoDefinition((ReferenceNode) sel);
                }
            }
        });


        membersList.getSelectionModel().selectedItemProperty().addListener(observable -> {
            signatureView.getItems().clear();
            codeView.setText("");

            FunctionNode node = membersList.getSelectionModel().getSelectedItem();
            if (node != null) {
                FunctionNode fn = (FunctionNode) node;
                signatureView.getItems().add(fn);
                for (Node n : fn.children()) {
                    if (n instanceof SeparatorNode) {
                        break;
                    }
                    signatureView.getItems().add((NamedNode) n);
                }

                codeView.setText(PrettyPrinter.print(fn));
            }
        });

        membersList.setCellFactory(listView -> new ListCell<FunctionNode>() {
            @Override
            protected void updateItem(FunctionNode item, boolean empty) {
                super.updateItem(item, empty);

                if (!empty && item != null) {
                    setText(item.getName());
                }
            }
        });

        getItems().addAll(packageTree, membersList, methodPane);

        membersList.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> {
            if (event.getClickCount() == 2
                && membersList.getSelectionModel().selectedItemProperty().getValue() != null
                && anotherPane != null) {
                anotherPane.insertNode(membersList.getSelectionModel().selectedItemProperty().getValue());
            }
        });


        MenuItem addFunction = new MenuItem("Add...", Icons.icon("add"));
        addFunction.setOnAction(e -> {
            TreeItem<NamedNode> selectedItem = packageTree.getSelectionModel().getSelectedItem();
            if (selectedItem != null && selectedItem.getValue() instanceof PackageNode) {
                PackageNode packageNode = (PackageNode) selectedItem.getValue();

                TextInputDialog dialog = new TextInputDialog();
                dialog.setTitle("Add a function");
                dialog.setHeaderText("Enter function signature");

                dialog.showAndWait().ifPresent(signature -> {
                    List<String> parts = Collections.emptyList();
                    try {
                        parts = Lexer.read(new StringReader(signature));
                    } catch (IOException | LexerException t) {
                    }
                    if (!parts.isEmpty()) {
                        List<Change> changes = new ArrayList<>();

                        InsertNode insertFn = (InsertNode) new InsertNode()
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

                        app.getWorkspace().apply(changes);

                        FunctionNode fn = app.getWorkspace().node(insertFn.getNode().id());
                        navigateTo(fn);
                    }

                });
            }
        });
        MenuItem runMenu = new MenuItem("Run...", Icons.icon("run"));
        runMenu.setOnAction(e -> {
            FunctionNode fn = membersList.getSelectionModel().getSelectedItem();
            if (fn != null) {

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
                    new Alert(Alert.AlertType.ERROR, "Error: " + err.toString()).show();
                }
            }
        });

        membersList.setContextMenu(new ContextMenu(
            addFunction,
            runMenu
        ));

        app.getWorkspace().changeListeners().add(this::processChanges);
    }

    private void processChanges(List<Change> changes) {
        for (Change change : changes) {
            if (change instanceof InsertNode) {
                InsertNode insert = (InsertNode) change;
                Node node = app.getWorkspace().node(insert.getNode().id());

                if (node instanceof FunctionNode
                    && packageTree.getSelectionModel().getSelectedItem() != null
                    && node.parentOfType(PackageNode.class) == packageTree.getSelectionModel().getSelectedItem().getValue()) {
                    fillMembers();
                }
            }
        }
    }


    private int getPosition(MouseEvent e) {
        TextAreaSkin skin = (TextAreaSkin) codeView.getSkin();
        return skin.getInsertionPoint(e.getX(), e.getY());
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

    private void insertNode(NamedNode value) {

    }


    private void fillMembers() {
        membersList.getItems().clear();
        NamedNode node = packageTree.getSelectionModel().getSelectedItem().getValue();

        if (node instanceof PackageNode) {
            PackageNode packageNode = (PackageNode) node;

            packageNode.children()
                .stream()
                .filter(item -> !(item instanceof PackageNode))
                .map(item -> (FunctionNode) item)
                .sorted(Comparator.comparing(NamedNode::getName))
                .forEach(membersList.getItems()::add);
        }
    }


    private Node selectedNode(int pos) {
        FunctionNode node = membersList.getSelectionModel().getSelectedItem();
        if (node instanceof FunctionNode) {
            List<Node> nodes = NodeUtils.expandNode((Node) node);
            int index = EditorUtils.tokenIndex(codeView.getText(), pos);
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

}
