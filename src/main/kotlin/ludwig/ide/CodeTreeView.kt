package ludwig.ide

import javafx.scene.control.TreeItem
import javafx.scene.control.TreeView
import javafx.scene.control.cell.TextFieldTreeCell
import javafx.scene.control.cell.TextFieldTreeCell.forTreeView
import javafx.util.Callback
import javafx.util.StringConverter
import ludwig.model.*
import ludwig.utils.argCount
import ludwig.utils.hasOpenArgs
import ludwig.utils.inline
import ludwig.workspace.Environment


class CodeTreeView(private val environment: Environment) : TreeView<Node>(TreeItem<Node>(ListNode())) {


    init {
        isShowRoot = false

        val defaultCellFactory = forTreeView<Node>(CollapsedConverter)

        this.cellFactory = Callback {
            (defaultCellFactory.call(it) as TextFieldTreeCell<Node>).apply {
                treeItemProperty().addListener { obs, oldTreeItem, newTreeItem ->
                    if (newTreeItem != null) {
                        converter = if (newTreeItem.isExpanded) ExpandedConverter else CollapsedConverter
                    }
                }
            }
        }
    }


    fun setNode(node: Node?) {
        root.children.clear()

        if (node is FunctionNode) {
            for (i in 0 until node.size) {
                if (node[i] !is VariableNode) {
                    setContent(root, node.subList(i, node.size))
                    break
                }
            }
        } else if (node is OverrideNode) {
            for (i in 1 until node.size) {
                if (node[i] !is VariableNode) {
                    setContent(root, node.subList(i, node.size))
                    break
                }
            }
        }
    }

    private fun setContent(parent: TreeItem<Node>, nodes: List<Node>) {
        val head = parent.value
        val skip = when (head) {
            is ForNode -> 1
            is AssignmentNode -> 1
            is LambdaNode -> argCount(head)
            else -> 0
        }

        for (node in nodes.drop(skip)) {
            val item = TreeItem(node)
            parent.children.add(item)
            item.isExpanded = expand(node)
            setContent(item, node)
        }
    }

    fun selectedNode(): Node? {
        val selectedItem = selectionModel.selectedItem
        return selectedItem?.value
    }

    fun locate(node: Node) {

    }

    private fun expand(node: Node): Boolean = !node.isEmpty() && (node is LambdaNode || node.take(node.size - 1).any { expandInner(it) } || expand(node.last()))

    private fun expandInner(node: Node): Boolean = node is LambdaNode || hasOpenArgs(node) || node.any { expandInner(it) }
}

object ExpandedConverter : StringConverter<Node>() {
    override fun toString(node: Node?): String = when (node) {
        null -> ""
        is AssignmentNode -> "= " + toString(node[0])
        is ForNode -> "for " + toString(node[0])
        is LambdaNode -> "λ " + node.take(argCount(node)).joinToString(separator = " ")
        else -> node.toString()
    }

    override fun fromString(string: String?): Node {
        TODO("not implemented")
    }
}

object CollapsedConverter : StringConverter<Node>() {
    override fun toString(node: Node?): String = node?.let { inline(it) } ?: ""

    override fun fromString(string: String?): Node {
        TODO("not implemented")
    }

}