package ludwig.ide;

import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import ludwig.changes.Comment;
import ludwig.changes.Rename;
import ludwig.model.NamedNode;
import ludwig.workspace.Environment;

import java.util.Objects;

import static java.util.Collections.singletonList;
import static ludwig.utils.NodeUtils.isReadonly;

public class SignatureItemEditor extends VBox {
    private final Environment environment;
    private final NamedNode<?> node;
    private final TextField nameTextField;
    private final TextField commentTextField;

    public SignatureItemEditor(Environment environment, NamedNode<?> node) {
        this.environment = environment;
        this.node = node;

        nameTextField = new TextField(node.name()) {
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
            }

            private void applyChanges() {
                SignatureItemEditor.this.environment.getWorkspace().apply(singletonList(new Rename().setNodeId(node.id()).name(getText())));
            }
        };

        getChildren().add(nameTextField);

        commentTextField = new TextField(node.comment()) {
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
            }

            private void applyChanges() {
                environment.getWorkspace().apply(singletonList(new Comment().nodeId(node.id()).comment(getText())));
            }
        };

        getChildren().add(commentTextField);
    }
}
