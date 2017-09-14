package ludwig.ide

import com.sun.javafx.scene.control.skin.TreeViewSkin
import javafx.scene.control.TreeItem
import javafx.scene.control.TreeView
import ludwig.model.*
import ludwig.workspace.Environment


class CodeTreeView(private val environment: Environment) : TreeView<Node<*>>(TreeItem<Node<*>>(ListNode())) {

    init {
        isShowRoot = false
    }

    fun setNode(node: Node<*>?) {
        root.children.clear()

        if (node is FunctionNode) {
            for (i in 0..node.children().size - 1) {
                if (node.children()[i] !is VariableNode) {
                    setContent(root, node.children().subList(i, node.children().size))
                    break
                }
            }
        } else if (node is OverrideNode) {
            for (i in 1..node.children().size - 1) {
                if (node.children()[i] !is VariableNode) {
                    setContent(root, node.children().subList(i, node.children().size))
                    break
                }
            }
        }
    }

    private fun setContent(parent: TreeItem<Node<*>>, nodes: List<Node<*>>) {
        for (node in nodes) {
            val item = TreeItem(node)
            parent.children.add(item)
            item.isExpanded = true
            setContent(item, node.children())
        }
    }

    fun selectedNode(): Node<*>? {
        val selectedItem = selectionModel.selectedItem
        return selectedItem?.value
    }

    fun locate(node: Node<*>) {

    }
}
