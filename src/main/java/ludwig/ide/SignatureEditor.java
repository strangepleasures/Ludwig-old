package ludwig.ide;

import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import ludwig.changes.Comment;
import ludwig.changes.Rename;
import ludwig.model.*;
import ludwig.workspace.Environment;

import java.util.Objects;

import static java.util.Collections.singletonList;
import static ludwig.utils.NodeUtils.declaration;
import static ludwig.utils.NodeUtils.isReadonly;

public class SignatureEditor extends VBox {
    private final Environment environment;
    private Node<?> node;

    private final CheckBox lazyCheckbox = new CheckBox("Lazy");
    private final GridPane signatureView = new GridPane();

    public SignatureEditor(Environment environment) {
        this.environment = environment;

        getChildren().addAll(lazyCheckbox, signatureView);
    }

    public void setNode(Node<?> node) {
        this.node = node;

        NamedNode decl = (node instanceof OverrideNode) ? declaration((OverrideNode) node) : (NamedNode) node;

        signatureView.getChildren().clear();

        if (node == null) {
            return;
        }

        signatureView.add(new Label("Name"), 1, 1);
        signatureView.add(new Label("Description"), 2, 1);
        signatureView.add(nameTextField(decl), 1, 2);
        signatureView.add(commentTextField(decl), 2, 2);

        if (decl instanceof FunctionNode) {
            FunctionNode fn = (FunctionNode) decl;
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
        }
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

                setEditable(!isReadonly(node));
                setMinWidth(100);
            }

            private void applyChanges() {
                environment.getWorkspace().apply(singletonList(new Rename().setNodeId(node.id()).name(getText())));
            }
        };
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

                setEditable(!isReadonly(node));
                setPrefWidth(1000);
            }

            private void applyChanges() {
                environment.getWorkspace().apply(singletonList(new Comment().nodeId(node.id()).comment(getText())));
            }
        };
    }
}
