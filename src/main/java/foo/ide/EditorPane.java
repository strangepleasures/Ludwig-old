package foo.ide;

import foo.model.*;
import foo.utils.CodeFormatter;
import foo.utils.CodeLine;
import foo.workspace.Workspace;
import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.event.EventHandler;
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

        ListView<NamedNode> membersList = new ListView<>();
        membersList.setMinWidth(120);

        packageTree.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            fillMembers(membersList, newValue);
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

            NamedNode node = membersList.getSelectionModel().getSelectedItem();
            if (node != null) {
                signatureView.getItems().add(node);
                CodeFormatter codeFormatter = new CodeFormatter();

                if (node instanceof FunctionNode) {
                    FunctionNode functionNode = (FunctionNode) node;
                    signatureView.getItems().addAll(functionNode.parameters());
                }

                node.children().forEach(n -> codeFormatter.child(n, false));
                List<CodeLine> lines = codeFormatter.getLines();
                codeView.setItems(FXCollections.observableList(lines));
            }
        });


        getItems().addAll(packageTree, memberListPane, methodPane);

        membersList.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> {
            if (event.getClickCount() == 2
                && membersList.getSelectionModel().selectedItemProperty().getValue() != null
                && anotherPane != null) {
                anotherPane.insertNode(membersList.getSelectionModel().selectedItemProperty().getValue());
            }
        });


        ContextMenu codeContextMenu = new ContextMenu();
    }

    private void insertNode(NamedNode value) {

    }


    private void fillMembers(ListView<NamedNode> memberList, TreeItem<NamedNode> newValue) {
        NamedNode node = newValue.getValue();

        if (node instanceof PackageNode) {
            PackageNode packageNode = (PackageNode) node;
            memberList.getItems().clear();

            packageNode.children()
                .stream()
                .filter(item -> !(item instanceof PackageNode))
                .map(item -> (NamedNode) item)
                .sorted(Comparator.comparing(NamedNode::getName))
                .forEach(memberList.getItems()::add);
        }
    }

}
