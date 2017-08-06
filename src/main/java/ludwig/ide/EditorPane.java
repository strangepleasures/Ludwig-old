package ludwig.ide;

import com.sun.javafx.scene.control.skin.TextAreaSkin;
import javafx.beans.binding.Bindings;
import javafx.event.ActionEvent;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.util.Callback;
import lombok.Getter;
import lombok.Setter;
import ludwig.changes.Change;
import ludwig.changes.InsertNode;
import ludwig.model.*;
import ludwig.utils.NodeUtils;
import ludwig.utils.PrettyPrinter;
import ludwig.workspace.Workspace;

import java.util.*;

public class EditorPane extends SplitPane {
    private final App app;
    private final ListView<Named> membersList = new ListView<>();
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

            Named node = membersList.getSelectionModel().getSelectedItem();
            if (node != null) {
                if (node instanceof FunctionNode) {
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

                if (node instanceof AssignmentNode) {
                    AssignmentNode an = (AssignmentNode) node;
                    signatureView.getItems().add((NamedNode) an.children().get(0));
                    codeView.setText(PrettyPrinter.print(an.children().get(1)));
                }
            }
        });

        membersList.setCellFactory(new Callback<ListView<Named>, ListCell<Named>>() {
            @Override
            public ListCell<Named> call(ListView<Named> listView) {
                return new ListCell<Named>() {
                    @Override
                    protected void updateItem(Named item, boolean empty) {
                        super.updateItem(item, empty);

                        if (!empty && item != null) {
                            setText(item.getName());
                        }
                    }
                };
            }
        });

        membersList.focusedProperty().addListener((observable, oldValue, newValue) ->     {
            if (newValue) {
                app.setActionTarget(new ActionTarget() {
                    @Override
                    public void add() {
                        TreeItem<NamedNode> selectedItem = packageTree.getSelectionModel().getSelectedItem();
                        if (selectedItem != null && selectedItem.getValue() instanceof PackageNode) {
                            FunctionNode fn = new FunctionNode();
                            fn.setName("<New>");
                            fn.id(Change.newId());
                            InsertNode change = new InsertNode();
                            change.setNode(fn);
                            change.setParent(selectedItem.getValue().id());
                            change.setPrev(selectedItem.getValue().children().stream().findFirst().map(Node::id).orElse(null));
                            app.getWorkspace().apply(Collections.singletonList(change));
                        }
                    }

                    @Override
                    public void delete() {
                    }
                });
            }
        });

        getItems().addAll(packageTree, membersList, methodPane);

        membersList.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> {
            if (event.getClickCount() == 2
                && membersList.getSelectionModel().selectedItemProperty().getValue() != null
                && anotherPane != null) {
                anotherPane.insertNode((NamedNode) membersList.getSelectionModel().selectedItemProperty().getValue());
            }
        });


        ContextMenu codeContextMenu = new ContextMenu();

        app.getWorkspace().changeListeners().add(this::processChanges);
    }

    private void processChanges(List<Change> changes) {
        for (Change change: changes) {
            if (change instanceof InsertNode) {
                InsertNode insert = (InsertNode) change;
                Node node = app.getWorkspace().node(insert.getNode().id());
                if (node instanceof NamedNode) {
                    NamedNode named = (NamedNode) node;

                    if ("<New>".equals(named.getName())) {
                        fillMembers();
                        navigateTo(named);
                    }

                }
            }
        }
    }


    private int getPosition(MouseEvent e) {
        TextAreaSkin skin = (TextAreaSkin) codeView.getSkin();
        return skin.getInsertionPoint(e.getX(),  e.getY());
    }

    private void gotoDefinition(ReferenceNode node) {
        navigateTo(node.ref());
    }

    private void navigateTo(NamedNode node) {
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
        } else {
            AssignmentNode an = node.parentOfType(AssignmentNode.class);
            if (an != null) {
                membersList.getSelectionModel().select(an);
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
                .map(item -> (Named) item)
                .sorted(Comparator.comparing(Named::getName))
                .forEach(membersList.getItems()::add);
        }
    }



    private Node selectedNode(int pos) {
        Named node = membersList.getSelectionModel().getSelectedItem();
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
