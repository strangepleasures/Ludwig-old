package ludwig.ide

import com.sun.javafx.collections.ObservableListWrapper
import impl.org.controlsfx.autocompletion.AutoCompletionTextFieldBinding
import javafx.scene.control.Alert
import javafx.scene.control.ListView
import javafx.scene.control.TextInputDialog
import ludwig.changes.*
import ludwig.interpreter.Callable
import ludwig.interpreter.CallableRef
import ludwig.model.*
import ludwig.script.Lexer
import ludwig.script.LexerException
import ludwig.utils.NodeUtils
import ludwig.workspace.Environment
import java.io.IOException
import java.io.StringReader
import java.util.*
import java.util.stream.Collectors


class MemberList(private val environment: Environment) : ListView<Node>() {
    private var packageNode: PackageNode? = null

    init {

        setCellFactory { listView -> SignatureListCell() }
        contextMenu = ContextMenuFactory.menu(Actions())
    }

    fun setPackage(packageNode: PackageNode?) {
        this.packageNode = packageNode
        items.clear()

        if (packageNode != null) {
            items = ObservableListWrapper(packageNode
                    .stream()
                    .filter { item -> item !is PackageNode }
                    .sorted(Comparator.comparing<Node, String> { n -> NodeUtils.signature(n).toLowerCase() })
                    .collect(Collectors.toList()))

            if (!items.isEmpty()) {
                selectionModel.select(0)
            }
        }
    }

    inner class Actions {
        fun addFunction() {
            if (NodeUtils.isReadonly(packageNode)) {
                return
            }

            val dialog = TextInputDialog()
            dialog.title = "Add a function"
            dialog.headerText = "Enter function signature"

            dialog.showAndWait().ifPresent { signature ->
                var parts = emptyList<String>()
                try {
                    parts = Lexer.read(StringReader(signature))
                            .stream()
                            .filter { s -> s != "(" && s != ")" }
                            .collect(Collectors.toList())
                } catch (t: IOException) {
                } catch (t: LexerException) {
                }

                if (!parts.isEmpty()) {
                    val changes = mutableListOf<Change>()

                    val insertFn = InsertNode()
                            .apply { parent = packageNode!!.id; node = FunctionNode().apply { name = parts[0]; id = newId() } }

                    changes.add(insertFn)

                    var prev: String? = null
                    for (i in 1 until parts.size) {
                        val id = newId()
                        changes.add(InsertNode()
                                .apply { parent = insertFn.node.id; this.prev = prev; node = VariableNode().apply { name = parts[i]; this.id = id } })
                        prev = id
                    }

                    environment.workspace().apply(changes)

                    val fn = environment.workspace().node<Node>(insertFn.node.id)
                    selectionModel.select(fn)
                }
            }
        }


        fun override() {
            val dialog = TextInputDialog()
            dialog.title = "Override"
            dialog.headerText = ""

            val autoCompletionTextFieldBinding = AutoCompletionTextFieldBinding<Node>(
                    dialog.editor,
                    { param ->
                        val suggestions = mutableListOf<Node>()
                        suggestions.addAll(environment.symbolRegistry().symbols(param.userText))

                        suggestions
                    },
                    NodeStringConverter())

            autoCompletionTextFieldBinding.setVisibleRowCount(20)
            var ref: Node? = null
            autoCompletionTextFieldBinding.setOnAutoCompleted { e -> ref = e.completion }


            dialog.showAndWait().ifPresent { signature ->
                if (ref != null) {
                    val changes = mutableListOf<Change>()

                    val insertOverride = InsertNode()
                            .apply { parent = packageNode!!.id; node = OverrideNode().apply { id = newId() } }

                    changes.add(insertOverride)

                    changes.add(InsertReference()
                            .apply { id = newId(); this.ref = ref!!.id; parent = insertOverride.node.id })

                    environment.workspace().apply(changes)

                    val o = environment.workspace().node<Node>(insertOverride.node.id)
                    selectionModel.select(o)
                }
            }
        }

        fun run() {
            val fn = selectionModel.selectedItem as? NamedNode ?: return
            try {
                val callable = if (fn is Callable) fn else CallableRef(fn)
                val result: Any?
                if (callable.argCount() > 0) {
                    val dialog = TextInputDialog()
                    dialog.title = "Execute function"
                    dialog.headerText = "Enter function arguments"
                    dialog.contentText = fn.name

                    val params = dialog.showAndWait()
                    if (params.isPresent) {
                        val args = Lexer.read(StringReader(params.get()))
                                .stream()
                                .filter { s -> s != "(" && s != ")" }
                                .map({ NodeUtils.parseLiteral(it) })
                                .toArray()
                        result = callable.call(args)
                    } else {
                        return
                    }

                } else {
                    result = callable.call(arrayOf<Any?>())
                }
                Alert(Alert.AlertType.INFORMATION, "Result: " + NodeUtils.formatLiteral(result)).show()
            } catch (err: Exception) {
                err.printStackTrace()
                Alert(Alert.AlertType.ERROR, "Error: " + err.toString()).show()
            }

        }

        fun delete() {
            if (NodeUtils.isReadonly(packageNode)) {
                return
            }
            val selectedItem = selectionModel.selectedItem
            if (selectedItem != null) {
                environment.workspace().apply(listOf<Change>(Delete().apply { id = selectedItem.id }))
            }
        }
    }
}
