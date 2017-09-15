package ludwig.ide

import javafx.scene.control.SplitPane
import javafx.scene.input.MouseEvent
import javafx.scene.layout.VBox
import ludwig.changes.Change
import ludwig.model.*
import ludwig.workspace.Environment
import java.util.function.Consumer

class EditorPane(private val environment: Environment, private val settings: Settings) : SplitPane() {
    private val membersList: MemberList
    private val packageTree: PackageTreeView

    private val signatureEditor: SignatureEditor
    private val codeEditor: CodeTreeView

    private var anotherPane: EditorPane? = null

    init {

        membersList = MemberList(environment)
        packageTree = PackageTreeView(environment.workspace())
        signatureEditor = SignatureEditor(environment)
        codeEditor = CodeTreeView(environment)

        membersList.minWidth = 120.0

        packageTree.selectionModel.selectedItemProperty().addListener { observable -> fillMembers() }

        membersList.prefHeight = 1E6
        codeEditor.prefHeight = 1E6

        codeEditor.contextMenu = ContextMenuFactory.menu(CodeEditorActions())

        membersList.selectionModel.selectedItemProperty().addListener { observable -> displayMember() }

        items.addAll(packageTree, membersList, VBox(signatureEditor, codeEditor))

        membersList.addEventHandler(MouseEvent.MOUSE_CLICKED) { event ->
            if (event.clickCount == 2
                    && membersList.selectionModel.selectedItemProperty().value != null
                    && anotherPane != null) {
                //  anotherPane.codeEditor.insertNode(membersList.getSelectionModel().selectedItemProperty().getValue());
            }
        }

        environment.workspace().changeListeners().add(Consumer<Change> { this.processChanges(it) })
    }

    private fun displayMember() {
        val sel = selectedMember()
        signatureEditor.setNode(sel)
        codeEditor.setNode(sel)
    }

    private fun selectedMember(): Node<*>? {
        return if (membersList.selectionModel == null) {
            null
        } else membersList.selectionModel.selectedItem
    }

    private fun processChanges(change: Change) {
        if (!environment.workspace().isBatchUpdate) {
            refresh()
        }
    }

    private fun navigateTo(node: Node<*>) {
        packageTree.select(node.parentOfType(PackageNode::class.java))
        val fn = node.parentOfType(FunctionNode::class.java)
        if (fn != null) {
            membersList.selectionModel.select(fn)
            var decl: Node<*>? = node.parentOfType(AssignmentNode::class.java)
            if (decl == null) {
                decl = node.parentOfType(ForNode::class.java)
            }
            if (decl == null) {
                decl = node.parentOfType(LambdaNode::class.java)
            }
            if (decl != null) {
                codeEditor.locate(decl)
            }
        }
    }

    private fun fillMembers() {
        membersList.items.clear()
        val selectedItem = packageTree.selectionModel.selectedItem

        if (selectedItem != null) {
            val node = selectedItem.value
            membersList.setPackage(node as? PackageNode)
        }
    }

    private fun refresh() {
        var packageSelection: Node<*>? = null
        var memberSelection: Node<*>? = null
        val signatureSelection: NamedNode<*>? = null
        var codeSelection: Node<*>? = null

        val selectedItem = packageTree.selectionModel.selectedItem
        if (selectedItem != null) {
            packageSelection = selectedItem.value
        }

        memberSelection = selectedMember()

        codeSelection = codeEditor.selectedNode()

        packageTree.recreateTree()
        packageTree.selectionModel.clearSelection()
        membersList.selectionModel.clearSelection()

        displayMember()

        if (memberSelection != null) {
            navigateTo(memberSelection)
        } else if (packageSelection != null) {
            navigateTo(packageSelection)
        }

        if (codeSelection != null) {
            codeEditor.locate(codeSelection)
        }
    }

    inner class CodeEditorActions {
        fun goToDefinition() {
            val sel = codeEditor.selectedNode()
            if (sel is ReferenceNode) {
                navigateTo(sel.ref)
            }
        }
    }


    fun anotherPane(): EditorPane? {
        return anotherPane
    }

    fun anotherPane(anotherPane: EditorPane): EditorPane {
        this.anotherPane = anotherPane
        return this
    }
}
