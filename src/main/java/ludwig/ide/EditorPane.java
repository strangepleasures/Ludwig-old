package ludwig.ide;

import javafx.util.Callback;
import ludwig.model.*;
import ludwig.utils.CodeFormatter;
import ludwig.utils.CodeLine;
import ludwig.workspace.Workspace;
import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import lombok.Getter;
import lombok.Setter;

import java.util.Comparator;
import java.util.List;

public class EditorPane extends SplitPane {
    private final Workspace workspace;
    private final Settings settings;

    @Getter
    @Setter
    private EditorPane anotherPane;

    public EditorPane(Workspace workspace, Settings settings) {
        this.workspace = workspace;
        this.settings = settings;

        PackageTreeView packageTree = new PackageTreeView(workspace);
        packageTree.setMinWidth(120);

        ListView<Named> membersList = new ListView<>();
        membersList.setMinWidth(120);

        packageTree.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            fillMembers(membersList.getItems(), newValue);
        });

        VBox memberListPane = new VBox();
        ToolBar memberListToolBar = new ToolBar();
        memberListPane.getChildren().addAll(memberListToolBar, membersList);
        membersList.setPrefHeight(1E6);

        Button addMethodButton = new Button("+M");
        memberListToolBar.getItems().add(addMethodButton);

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

        ListView<CodeLine> codeView = new ListView<>();
        codeView.setPrefHeight(1E6);
        methodPane.getChildren().add(codeView);


        membersList.getSelectionModel().selectedItemProperty().addListener(observable -> {
            signatureView.getItems().clear();
            codeView.getItems().clear();

            Named node = membersList.getSelectionModel().getSelectedItem();
            if (node != null) {

                CodeFormatter codeFormatter = new CodeFormatter();

                if (node instanceof FunctionNode) {
                    FunctionNode fn = (FunctionNode) node;
                    signatureView.getItems().add(fn);
                    for (Node n: fn.children()) {
                        if (n instanceof SeparatorNode) {
                            break;
                        }
                        signatureView.getItems().add((NamedNode) n);
                    }

                    boolean body = false;
                    for (Node child: fn.children()) {
                        if (child instanceof SeparatorNode) {
                            body = true;
                        } else if (body) {
                            codeFormatter.child(child, false);
                        }
                    }
                }

                if (node instanceof AssignmentNode) {
                    AssignmentNode an = (AssignmentNode) node;
                    signatureView.getItems().add((NamedNode) an.children().get(0));
                    codeFormatter.child(an.children().get(1), false);
                }

                List<CodeLine> lines = codeFormatter.getLines();
                codeView.setItems(FXCollections.observableList(lines));
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

        getItems().addAll(packageTree, memberListPane, methodPane);

//        membersList.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> {
//            if (event.getClickCount() == 2
//                && membersList.getSelectionModel().selectedItemProperty().getValue() != null
//                && anotherPane != null) {
//                anotherPane.insertNode(membersList.getSelectionModel().selectedItemProperty().getValue());
//            }
//        });


        ContextMenu codeContextMenu = new ContextMenu();
    }

    private void insertNode(NamedNode value) {

    }


    private void fillMembers(List<Named> memberList, TreeItem<NamedNode> newValue) {
        NamedNode node = newValue.getValue();

        if (node instanceof PackageNode) {
            PackageNode packageNode = (PackageNode) node;
            memberList.clear();

            packageNode.children()
                .stream()
                .filter(item -> !(item instanceof PackageNode))
                .map(item -> (Named) item)
                .sorted(Comparator.comparing(Named::getName))
                .forEach(memberList::add);
        }
    }

}
