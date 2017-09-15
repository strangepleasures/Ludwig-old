package ludwig.ide

import javafx.scene.control.TextInputDialog
import javafx.scene.control.TreeItem
import javafx.scene.control.TreeView
import ludwig.changes.Change
import ludwig.changes.Delete
import ludwig.changes.InsertNode
import ludwig.changes.newId
import ludwig.model.NamedNode
import ludwig.model.PackageNode
import ludwig.utils.NodeUtils
import ludwig.workspace.Workspace

internal class PackageTreeView : TreeView<NamedNode> {
    private val workspace: Workspace

    constructor(workspace: Workspace) : super(createRoot(workspace)) {
        this.workspace = workspace
    }

    init {
        isShowRoot = false

        contextMenu = ContextMenuFactory.menu(Actions())

        prefWidth = 120.0
    }


    fun select(packageNode: NamedNode?) {
        selectionModel.select(find(packageNode))
    }

    private fun find(node: NamedNode?): TreeItem<NamedNode>? {
        if (node == null) {
            return null
        }
        val parentItem = if (node.parent == null) root else find(node.parent as NamedNode?)
        return parentItem!!.children
                .stream()
                .filter { i -> i.value === node }
                .findFirst()
                .orElse(null)
    }

    fun recreateTree() {
        root = createRoot(workspace)
    }

    inner class Actions {
        fun newPackage() {
            val selectedItem = selectionModel.selectedItem
            if (selectedItem != null) {
                val parent = selectedItem.value

                if (NodeUtils.isReadonly(parent)) {
                    return
                }

                val dialog = TextInputDialog()
                dialog.title = "Add a package"
                dialog.headerText = ""
                dialog.contentText = "Package name"

                dialog.showAndWait().ifPresent { name ->
                    val packageNode = PackageNode().apply { this.name = name; id = newId() }
                    val insert = InsertNode()
                            .apply { node = packageNode; this.parent = parent.id }
                    workspace.apply(listOf<Change>(insert))
                    select(packageNode)
                }
            }
        }

        fun deletePackage() {
            val selectedItem = selectionModel.selectedItem
            if (selectedItem != null) {
                val packageNode = selectedItem.value
                if (NodeUtils.isReadonly(packageNode)) {
                    return
                }
                val parent = packageNode.parent as NamedNode?
                workspace.apply(listOf(Delete().apply { id = packageNode.id }))
                select(parent)
            }
        }
    }
}

private fun createRoot(workspace: Workspace): TreeItem<NamedNode> {
    val root = TreeItem<NamedNode>()

    for (projectNode in workspace.projects) {
        val projectItem = TreeItem<NamedNode>(projectNode)

        root.children.add(projectItem)

        for (packageNode in projectNode.children) {
            processPackage(projectItem, packageNode as PackageNode)
        }
    }

    return root
}

private fun processPackage(parent: TreeItem<NamedNode>, packageNode: PackageNode) {
    val packageItem = TreeItem<NamedNode>(packageNode)
    parent.children.add(packageItem)

    for (node in packageNode.children) {
        if (node is PackageNode) {
            processPackage(packageItem, node)
        }
    }
}
