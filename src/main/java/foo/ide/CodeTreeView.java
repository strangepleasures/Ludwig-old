package foo.ide;

import foo.model.FunctionNode;
import foo.model.Node;
import javafx.event.EventHandler;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.util.Callback;

public class CodeTreeView extends TreeView<Node> {
    public CodeTreeView() {
        super();

//        setCellFactory(p -> new TreeCell<Node>() {
//            private TextField textField;
//            private void createTextField() {
//
//
//                textField = new TextField(getString());
//                textField.setOnKeyReleased(t -> {
////                        if (t.getCode() == KeyCode.ENTER) {
////                            commitEdit(textField.getText());
////                        } else if (t.getCode() == KeyCode.ESCAPE) {
////                            cancelEdit();
////                        }
//                });
//            }
//
//            @Override
//            public void updateItem(Node item, boolean empty) {
//                super.updateItem(item, empty);
//
//                if (empty) {
//                    setText(null);
//                    setGraphic(null);
//                } else {
//                    if (isEditing()) {
//                        if (textField != null) {
//                            textField.setText(getString());
//                        }
//                        setText(null);
//                        setGraphic(textField);
//                    } else {
//                        setText(getString());
//                        setGraphic(getTreeItem().getGraphic());
//                    }
//                }
//            }
//
//
//
//            private String getString() {
//
//                return getItem() == null ? "" : getItem().accept(ExpandedToStringVisitor.INSTANCE);
//            }
//        });
    }

    public CodeTreeView(FunctionNode function) {
        super(new TreeNode(function));
    }

    public void setFunction(FunctionNode function) {
        setRoot(new TreeNode(function));

        setShowRoot(false);
    }

}
