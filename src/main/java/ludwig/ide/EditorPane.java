package ludwig.ide;

import javafx.scene.control.SplitPane;
import javafx.scene.control.TreeItem;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import ludwig.changes.Change;
import ludwig.model.*;
import ludwig.workspace.Environment;

public class EditorPane extends SplitPane {
    private final Environment environment;
    private final Settings settings;
    private final MemberList membersList;
    private final PackageTreeView packageTree;

    private final SignatureEditor signatureEditor;
    private final CodeTreeView codeEditor;

    private EditorPane anotherPane;

    public EditorPane(Environment environment, Settings settings) {
        this.environment = environment;
        this.settings = settings;

        membersList = new MemberList(environment);
        packageTree = new PackageTreeView(environment.workspace());
        signatureEditor = new SignatureEditor(environment);
        codeEditor = new CodeTreeView(environment);

        membersList.setMinWidth(120);

        packageTree.getSelectionModel().selectedItemProperty().addListener(observable -> fillMembers());

        membersList.setPrefHeight(1E6);
        codeEditor.setPrefHeight(1E6);

        codeEditor.setContextMenu(ContextMenuFactory.menu(new CodeEditorActions()));

        membersList.getSelectionModel().selectedItemProperty().addListener(observable -> displayMember());

        getItems().addAll(packageTree, membersList, new VBox(signatureEditor, codeEditor));

        membersList.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> {
            if (event.getClickCount() == 2
                && membersList.getSelectionModel().selectedItemProperty().getValue() != null
                && anotherPane != null) {
              //  anotherPane.codeEditor.insertNode(membersList.getSelectionModel().selectedItemProperty().getValue());
            }
        });

        environment.workspace().changeListeners().add(this::processChanges);
    }

    private void displayMember() {
        Node sel = selectedMember();
        signatureEditor.setNode(sel);
        codeEditor.setNode(sel);
    }

    private Node selectedMember() {
        if (membersList.getSelectionModel() == null) {
            return null;
        }
        return membersList.getSelectionModel().getSelectedItem();
    }

    private void processChanges(Change change) {
        if (!environment.workspace().isBatchUpdate()) {
            refresh();
        }
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
                codeEditor.locate(decl);
            }
        }
    }

    private void fillMembers() {
        membersList.getItems().clear();
        TreeItem<NamedNode> selectedItem = packageTree.getSelectionModel().getSelectedItem();

        if (selectedItem != null) {
            NamedNode node = selectedItem.getValue();
            membersList.setPackage(node instanceof PackageNode ? (PackageNode) node : null);
        }
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

        codeSelection = codeEditor.selectedNode();

        packageTree.recreateTree();
        packageTree.getSelectionModel().clearSelection();
        membersList.getSelectionModel().clearSelection();

        displayMember();

        if (memberSelection != null) {
            navigateTo(memberSelection);
        } else if (packageSelection != null) {
            navigateTo(packageSelection);
        }

        if (codeSelection != null) {
            codeEditor.locate(codeSelection);
        }
    }

    public class CodeEditorActions {
        public void goToDefinition() {
            Node sel = codeEditor.selectedNode();
            if (sel instanceof ReferenceNode) {
                navigateTo(((ReferenceNode) sel).ref());
            }
        }
    }


    public EditorPane anotherPane() {
        return anotherPane;
    }

    public EditorPane anotherPane(EditorPane anotherPane) {
        this.anotherPane = anotherPane;
        return this;
    }
}
