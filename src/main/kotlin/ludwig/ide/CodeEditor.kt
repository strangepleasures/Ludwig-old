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
import ludwig.model.Node
import ludwig.model.PlaceholderNode
import ludwig.utils.NodeUtils
import ludwig.utils.PrettyPrinter
import ludwig.workspace.Environment

class CodeEditor(private val environment: Environment) : TextArea() {
    private var node: Node? = null

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

    fun setContent(node: Node?) {
        this.node = node
        if (node == null) {
            text = ""
            isEditable = false
        } else {
            text = PrettyPrinter.print(node)
            isEditable = !NodeUtils.isReadonly(node)
        }
    }

    private fun selectedNode(pos: Int): Node? {
        if (node == null) {
            return null
        }
        val nodes = NodeUtils.expandNode(node!!)
        val index = EditorUtils.tokenIndex(text, pos)
        return if (index < 0) {
            null
        } else nodes[index + NodeUtils.arguments(node!!).size]

    }

    fun selectedNode(): Node? {
        return selectedNode(selection.start)
    }

    fun locate(node: Node) {
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

    fun insertNode(node: Node) {
//        val sel = selectedNode()
//        val head = if (node is NamedNode) InsertReference().apply { nodeId = newId(); ref = node.id } else InsertNode().apply { this.node = node }
//        val changes = mutableListOf<Change>()
//        val selectedItem = this.node
//        if (selectedItem !is FunctionNode || !isEditable) {
//            return
//        }
//        val target = selectedItem as FunctionNode?
//        if (sel != null) {
//            head.parent = sel.parent!!.id
//            val index = sel.parent!!.indexOf(sel)
//            head.prev = if (index == 0) null else sel.parent!![index - 1].id
//            head.next = if (index == sel.parent!!.size - 1) null else sel.parent!![index + 1].id
//            changes.add(Delete().apply { nodeId = sel.id })
//        } else {
//            head.parent = target!!.id
//            head.prev = if (target.isEmpty()) null else target[target.size - 1].id
//        }
//
//        changes.add(head)
//
//        var prev: String? = null
//
//        for (arg in NodeUtils.arguments(node)) {
//            val insertPlaceholder = InsertNode()
//                    .apply { parent = (head as InsertReference).nodeId; this.prev = prev; this.node = PlaceholderNode().apply { parameter = arg; id = newId() } }
//            changes.add(insertPlaceholder)
//            prev = insertPlaceholder.node.id
//        }
//
//
//        environment.workspace().apply(changes)
//
//        selectNextNode()
    }


    private fun showEditor(text: String, selectAll: Boolean) {
        val popup = Popup()
        val autoCompleteTextField = TextField()
        autoCompleteTextField.text = text

        val autoCompletionTextFieldBinding = AutoCompletionTextFieldBinding<Node>(
                autoCompleteTextField,
                { param ->
                    val suggestions = mutableListOf<Node>()
//                    if (param.userText.isEmpty() || "= variable value".startsWith(param.userText)) {
//                        suggestions.add(AssignmentNode()
//                                .add(PlaceholderNode().apply { parameter = "variable"; nodeId = newId()})
//                                .add(PlaceholderNode().apply { parameter = "value"; nodeId = newId()}))
//                    }
//                    if (param.userText.isEmpty() || param.userText.startsWith("λ") || param.userText.startsWith("\\")) {
//                        suggestions.add(LambdaNode()
//                                .add(PlaceholderNode().apply { parameter = "args..."; nodeId = newId()}))
//                    }
//                    if (param.userText.isEmpty() || "ref fn".startsWith(param.userText)) {
//                        suggestions.add(FunctionReferenceNode()
//                                .add(PlaceholderNode().apply { parameter = "fn"; nodeId = newId()}))
//                    }
//                    if (param.userText.isEmpty() || "call fn args...".startsWith(param.userText)) {
//                        suggestions.add(CallNode()
//                                .add(PlaceholderNode().apply { parameter = "fn"; nodeId = newId()})
//                                .add(PlaceholderNode().apply { parameter = "args..."; nodeId = newId()}))
//                    }
//                    if (param.userText.isEmpty() || "if condition statements...>".startsWith(param.userText)) {
//                        suggestions.add(IfNode()
//                                .add(PlaceholderNode().apply { parameter = "condition"; nodeId = newId()})
//                                .add(PlaceholderNode().apply { parameter = "statements"; nodeId = newId()}))
//                    }
//                    if (param.userText.isEmpty() || "else statements...".startsWith(param.userText)) {
//                        suggestions.add(ElseNode()
//                                .add(PlaceholderNode().apply { parameter = "statements..."; nodeId = newId()}))
//                    }
//                    if (param.userText.isEmpty() || "for var seq statements...".startsWith(param.userText)) {
//                        suggestions.add(ForNode()
//                                .add(PlaceholderNode().apply { parameter = "var"; nodeId = newId()})
//                                .add(PlaceholderNode().apply { parameter = "seq"; nodeId = newId()})
//                                .add(PlaceholderNode().apply { parameter = "statements"; nodeId = newId()}))
//                    }
//                    if (param.userText.isEmpty() || "break".startsWith(param.userText)) {
//                        suggestions.add(BreakNode())
//                    }
//                    if (param.userText.isEmpty() || "continue".startsWith(param.userText)) {
//                        suggestions.add(ContinueNode())
//                    }
//                    if (param.userText.isEmpty() || "return result".startsWith(param.userText)) {
//                        suggestions.add(ReturnNode()
//                                .add(PlaceholderNode().apply { parameter = "result"; nodeId = newId()}))
//                    }

                    suggestions.addAll(NodeUtils.collectLocals(this.node!!, selectedNode()!!, param.userText))
                    suggestions.addAll(environment.symbolRegistry().symbols(param.userText))

                    suggestions
                },
                NodeStringConverter())

        autoCompletionTextFieldBinding.setVisibleRowCount(20)
        var ref: Node? = null
        autoCompletionTextFieldBinding.setOnAutoCompleted { e -> ref = e.completion }

        popup.content.add(autoCompleteTextField)
        val skin = skin as TextAreaSkin
        val caretBounds = localToScreen(skin.caretBounds)
        autoCompleteTextField.setOnAction { ev ->
            //            if (Lexer.isLiteral(autoCompleteTextField.text)) {
//                insertNode(LiteralNode(autoCompleteTextField.text).apply { id = newId() })
//            } else if (ref != null) {
//                insertNode(ref!!)
//            } else if (!autoCompleteTextField.text.isEmpty()) {
//                insertNode(VariableNode().apply { name = autoCompleteTextField.text; id = newId() })
//            }
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
