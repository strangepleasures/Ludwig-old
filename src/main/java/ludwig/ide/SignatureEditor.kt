package ludwig.ide

import javafx.scene.layout.VBox
import ludwig.model.NamedNode
import ludwig.model.Node
import ludwig.model.OverrideNode
import ludwig.model.VariableNode
import ludwig.utils.NodeUtils
import ludwig.workspace.Environment


class SignatureEditor(private val environment: Environment) : VBox() {
    private var node: Node<*>? = null

    fun setNode(node: Node<*>?) {
        this.node = node

        children.clear()

        val decl = if (node is OverrideNode) NodeUtils.declaration((node as OverrideNode?)!!) else node as NamedNode<*>?
        if (node == null) {
            return
        }

        children.add(SignatureItemEditor(environment, decl!!))

        for (n in decl.children) {
            if (n !is VariableNode) {
                break
            }
            children.add(SignatureItemEditor(environment, n as NamedNode<*>))
        }

    }
}
