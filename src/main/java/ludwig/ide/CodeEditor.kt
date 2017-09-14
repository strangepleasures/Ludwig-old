package ludwig.ide

import com.sun.javafx.scene.control.skin.TextAreaSkin
import impl.org.controlsfx.autocompletion.AutoCompletionTextFieldBinding
import javafx.application.Platform
import javafx.event.EventHandler
import javafx.scene.control.TextArea
import javafx.scene.control.TextField
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyEvent
import javafx.stage.Popup
import ludwig.changes.Change
import ludwig.changes.Delete
import ludwig.changes.InsertNode
import ludwig.changes.InsertReference
import ludwig.model.*
import ludwig.script.Lexer
import ludwig.utils.NodeUtils
import ludwig.utils.PrettyPrinter
import ludwig.workspace.Environment
import java.util.*


class CodeEditor(private val environment: Environment) : TextArea() {
    private var node: Node<*>? = null

    init {

        setOnKeyPressed { e ->
            when (e.code) {
                KeyCode.LEFT -> selectPrevNode()
                KeyCode.RIGHT -> selectNextNode()
                KeyCode.UP -> selectPrevLine()
                KeyCode.DOWN -> selectNextLine()
                KeyCode.BACK_SPACE -> {
                    selectPrevNode()
                    deleteNode()
                }
                KeyCode.DELETE -> deleteNode()
                else -> if (!isEditable && e.text != null && !e.text.isEmpty()) {
                    showEditor(e.text, false)
                }
            }
            e.consume()
        }

        onKeyReleased = EventHandler<KeyEvent> { it.consume() }
        onKeyTyped = EventHandler<KeyEvent> { it.consume() }

        setOnMouseClicked { e ->
            if (e.clickCount == 2) {
                if (!isEditable) {
                    return@setOnMouseClicked
                }

                val n = selectedNode() ?: return@setOnMouseClicked

                if (n is PlaceholderNode) {
                    showEditor("", true)
                } else {
                    showEditor(n.toString(), true)
                }
            }
        }
    }

    private fun deleteNode() {

    }

    fun setContent(node: Node<*>?) {
        this.node = node
        if (node == null) {
            text = ""
            isEditable = false
        } else {
            text = PrettyPrinter.print(node)
            isEditable = !NodeUtils.isReadonly(node)
        }
    }

    private fun selectedNode(pos: Int): Node<*>? {
        if (node == null) {
            return null
        }
        val nodes = NodeUtils.expandNode(node!!)
        val index = EditorUtils.tokenIndex(text, pos)
        return if (index < 0) {
            null
        } else nodes[index + NodeUtils.arguments(node!!).size]

    }

    fun selectedNode(): Node<*>? {
        return selectedNode(selection.start)
    }

    fun locate(node: Node<*>) {
        for (i in 0..text.length - 1) {
            if (selectedNode(i) === node) {
                selectRange(i, i)
                break
            }
        }
    }

    private fun selectNextNode() {
        val start = selection.start
        val current = selectedNode(start)
        val length = text.length
        for (i in start + 1..length - 1) {
            if (selectedNode(i) !== current) {
                selectRange(i, i)
                break
            }
        }
    }

    private fun selectPrevNode() {
        val start = selection.start
        val current = selectedNode(start)

        for (i in start - 1 downTo 0) {
            val sel = selectedNode(i)
            if (sel !== current) {
                selectRange(i, i)
                for (j in i - 1 downTo 0) {
                    if (selectedNode(j) !== sel) {
                        selectRange(j + 1, j + 1)
                        return
                    }
                }
            }
        }

        selectRange(0, 0)
    }

    private fun selectNextLine() {
        val text = text
        val start = selection.start
        val length = text.length
        for (i in start + 1..length - 1) {
            if (text[i] == '\n') {
                for (j in i + 1..length - 1) {
                    if (text[j] != ' ') {
                        selectRange(j, j)
                        return
                    }
                }

                break
            }
        }
        selectRange(length + 1, length + 1)
    }

    private fun selectPrevLine() {
        val text = text
        val start = selection.start
        var first = true
        for (i in start - 1 downTo 0) {
            if (text[i] == '\n') {
                if (first) {
                    first = false
                } else {
                    selectRange(i, i)
                    selectNextNode()
                    return
                }
            }
        }
        selectRange(0, 0)
    }

    fun insertNode(node: Node<*>) {
        val sel = selectedNode()
        val head = if (node is NamedNode<*>) InsertReference().id(Change.newId()).ref(node.id()!!) else InsertNode().node(node)
        val changes = ArrayList<Change<*>>()
        val selectedItem = this.node
        if (selectedItem !is FunctionNode || !isEditable) {
            return
        }
        val target = selectedItem as FunctionNode?
        if (sel != null) {
            head.parent(sel.parent()!!.id())
            val index = sel.parent()!!.children().indexOf(sel)
            head.prev(if (index == 0) null else sel.parent()!!.children()[index - 1].id())
            head.next(if (index == sel.parent()!!.children().size - 1) null else sel.parent()!!.children()[index + 1].id())
            changes.add(Delete().id(sel.id()!!))
        } else {
            head.parent(target!!.id())
            head.prev(if (target.children().isEmpty()) null else target.children()[target.children().size - 1].id())
        }

        changes.add(head)

        var prev: String? = null

        for (arg in NodeUtils.arguments(node)) {
            val insertPlaceholder = InsertNode()
                    .node(PlaceholderNode().parameter(arg).id(Change.newId()))
                    .parent((head as InsertReference).id())
                    .prev(prev)
            changes.add(insertPlaceholder)
            prev = insertPlaceholder.node()!!.id()
        }


        environment.workspace().apply(changes)

        selectNextNode()
    }


    private fun showEditor(text: String, selectAll: Boolean) {
        val popup = Popup()
        val autoCompleteTextField = TextField()
        autoCompleteTextField.text = text

        val autoCompletionTextFieldBinding = AutoCompletionTextFieldBinding<Node<*>>(
                autoCompleteTextField,
                { param ->
                    val suggestions = ArrayList<Node<*>>()
                    if (param.userText.isEmpty() || "= variable value".startsWith(param.userText)) {
                        suggestions.add(AssignmentNode()
                                .add(PlaceholderNode().parameter("variable").id(Change.newId()))
                                .add(PlaceholderNode().parameter("value").id(Change.newId())))
                    }
                    if (param.userText.isEmpty() || param.userText.startsWith("λ") || param.userText.startsWith("\\")) {
                        suggestions.add(LambdaNode()
                                .add(PlaceholderNode().parameter("args...").id(Change.newId())))
                    }
                    if (param.userText.isEmpty() || "ref fn".startsWith(param.userText)) {
                        suggestions.add(FunctionReferenceNode()
                                .add(PlaceholderNode().parameter("fn").id(Change.newId())))
                    }
                    if (param.userText.isEmpty() || "call fn args...".startsWith(param.userText)) {
                        suggestions.add(CallNode()
                                .add(PlaceholderNode().parameter("fn").id(Change.newId()))
                                .add(PlaceholderNode().parameter("args...").id(Change.newId())))
                    }
                    if (param.userText.isEmpty() || "if condition statements...>".startsWith(param.userText)) {
                        suggestions.add(IfNode()
                                .add(PlaceholderNode().parameter("condition").id(Change.newId()))
                                .add(PlaceholderNode().parameter("statements...").id(Change.newId())))
                    }
                    if (param.userText.isEmpty() || "else statements...".startsWith(param.userText)) {
                        suggestions.add(ElseNode()
                                .add(PlaceholderNode().parameter("statements...").id(Change.newId())))
                    }
                    if (param.userText.isEmpty() || "for var seq statements...".startsWith(param.userText)) {
                        suggestions.add(ForNode()
                                .add(PlaceholderNode().parameter("var").id(Change.newId()))
                                .add(PlaceholderNode().parameter("seq").id(Change.newId()))
                                .add(PlaceholderNode().parameter("statements...").id(Change.newId())))
                    }
                    if (param.userText.isEmpty() || "break loop-var".startsWith(param.userText)) {
                        suggestions.add(BreakNode()
                                .add(PlaceholderNode().parameter("loop-var").id(Change.newId())))
                    }
                    if (param.userText.isEmpty() || "continue loop-var".startsWith(param.userText)) {
                        suggestions.add(ContinueNode()
                                .add(PlaceholderNode().parameter("loop-var").id(Change.newId())))
                    }
                    if (param.userText.isEmpty() || "return result".startsWith(param.userText)) {
                        suggestions.add(ReturnNode()
                                .add(PlaceholderNode().parameter("result").id(Change.newId())))
                    }

                    suggestions.addAll(NodeUtils.collectLocals(this.node!!, selectedNode()!!, param.userText))
                    suggestions.addAll(environment.symbolRegistry().symbols(param.userText))

                    suggestions
                },
                NodeStringConverter())

        autoCompletionTextFieldBinding.setVisibleRowCount(20)
        var ref: Node<*>? = null
        autoCompletionTextFieldBinding.setOnAutoCompleted { e -> ref = e.completion }

        popup.content.add(autoCompleteTextField)
        val skin = skin as TextAreaSkin
        val caretBounds = localToScreen(skin.caretBounds)
        autoCompleteTextField.setOnAction { ev ->
            if (Lexer.isLiteral(autoCompleteTextField.text)) {
                insertNode(LiteralNode(autoCompleteTextField.text).id(Change.newId()))
            } else if (ref != null) {
                insertNode(ref!!)
            } else if (!autoCompleteTextField.text.isEmpty()) {
                insertNode(VariableNode().name(autoCompleteTextField.text).id(Change.newId()))
            }
            popup.hide()
            autoCompletionTextFieldBinding.dispose()
        }
        autoCompleteTextField.setOnKeyPressed { e ->
            if (e.code == KeyCode.ESCAPE) {
                popup.hide()
                autoCompletionTextFieldBinding.dispose()
            }
        }

        popup.show(this, caretBounds.minX, caretBounds.minY)

        if (!selectAll) {
            Platform.runLater { autoCompleteTextField.selectRange(text.length, text.length) }
        }
    }
}
