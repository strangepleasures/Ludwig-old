package ludwig.ide

import javafx.scene.control.TextField
import javafx.scene.layout.VBox
import ludwig.changes.Comment
import ludwig.changes.Rename
import ludwig.model.NamedNode
import ludwig.utils.isReadonly
import ludwig.workspace.Environment

class SignatureItemEditor(private val environment: Environment, private val node: NamedNode) : VBox() {
    private val nameTextField: TextField
    private val commentTextField: TextField

    init {

        nameTextField = object : TextField(node.name) {
            private var saved: String? = null

            init {
                setOnAction { e -> applyChanges() }

                this.focusedProperty().addListener { e ->
                    if (focusedProperty().get()) {
                        saved = text
                    } else {
                        if (text != saved) {
                            applyChanges()
                        }
                    }
                }

                isEditable = !isReadonly(node)
            }

            private fun applyChanges() {
                environment.workspace().apply(Rename().apply { nodeId = node.id; name = text })
            }
        }

        children.add(nameTextField)

        commentTextField = object : TextField(node.comment) {
            private var saved: String? = null

            init {
                setOnAction { e -> applyChanges() }

                this.focusedProperty().addListener { e ->
                    if (focusedProperty().get()) {
                        saved = text
                    } else {
                        if (text != saved) {
                            applyChanges()
                        }
                    }
                }

                isEditable = !isReadonly(node)
            }

            private fun applyChanges() {
                environment.workspace().apply(Comment().apply { nodeId = node.id; comment = text })
            }
        }

        children.add(commentTextField)
    }
}
