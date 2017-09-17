package ludwig.workspace

import ludwig.changes.*
import ludwig.interpreter.Builtins
import ludwig.model.*
import ludwig.runtime.StdLib
import ludwig.script.Parser
import java.io.InputStreamReader
import java.util.*
import java.util.function.Consumer

class Workspace {

    private val nodes = HashMap<String, Node>()
    private val appliedChanges = mutableListOf<Change>()
    val projects = mutableListOf<ProjectNode>()
    private val changeListeners = mutableListOf<(Array<out Change>) -> Unit>()
    private val builtins = ProjectNode()
    var isLoading: Boolean = false
        private set

    fun init() {
        builtins.name = "Runtime"
        builtins.readonly = true
        builtins.id = "Runtime"
        builtins.add(Builtins.of(StdLib))

        addNode(builtins)

        try {
            InputStreamReader(Workspace::class.java.getResourceAsStream("/system.lw")).use { reader -> Parser.parse(reader, this, builtins) }
            InputStreamReader(Workspace::class.java.getResourceAsStream("/system-tests.lw")).use { reader -> Parser.parse(reader, this, builtins) }
        } catch (e: Exception) {
            throw RuntimeException(e)
        }

    }

    fun changeListeners(): MutableList<(Array<out Change>) -> Unit> {
        return changeListeners
    }

    private fun place(node: Node, insert: Insert): Problem? {
        addNode(node)
        val parent = node(insert.parent)
        node.parent = parent

        if (parent != null) {
            val prev = node(insert.prev)
            val next = node(insert.next)

            if (!parent.isOrdered) {
                parent.add(node)
            } else {
                val items = parent

                if (next == null) {
                    if (!items.isEmpty() && items[items.size - 1] === prev || items.isEmpty() && prev == null) {
                        parent.add(node)
                    }
                } else if (prev == null) {
                    if (!items.isEmpty() && items[0] === next) {
                        items.add(0, node)
                    }
                } else {
                    val prevIndex = items.indexOf(prev)
                    val nextIndex = items.indexOf(next)

                    if (nextIndex == prevIndex + 1) {
                        items.add(nextIndex, next)
                    }
                }
            }
        }
        return null
    }

    fun apply(vararg changes: Change): List<Problem> {
        val problems = mutableListOf<Problem>()
        for (change in changes) {
            when (change) {
                is Create -> {
                    val node = Class.forName(Node::class.java.`package`.name + "." + change.nodeType).newInstance() as Node
                    node.id = change.changeId
                    place(node, change)
                }
                is Delete -> node(change.nodeId)!!.delete()
                is Comment -> node(change.nodeId)!!.comment = change.comment
                is Rename -> (node(change.nodeId) as NamedNode).name = change.name
                is Value -> {
                    val node = node(change.nodeId)
                    when (node) {
                        is LiteralNode -> node.text = change.value
                        is ReferenceNode -> node.ref = node(change.value)!!
                    }
                }
            }
        }

        changeListeners.forEach({ it(changes) })

        if (problems.isEmpty()) { // TODO: Make a distinction between warnings and errors
            appliedChanges.addAll(changes)


        } else {
            restore()
        }
        return problems
    }

    fun load(changes: Array<out Change>): List<Problem> {
        isLoading = true
        try {
            return apply(*changes)
        } finally {
            isLoading = false
        }
    }

    private fun restore() {
        nodes.clear()
        projects.clear()

        val changes = ArrayList(appliedChanges)
        appliedChanges.clear()
        apply(*changes.toTypedArray())
    }

    fun node(id: String?): Node? {
        return if (id == null) null else nodes[id]
    }

    fun addNode(node: Node) {
        nodes.put(node.id, node)
        if (node is ProjectNode) {
            projects.add(node)
        }
        node.forEach(Consumer { this.addNode(it) })
    }
}

fun changeListener(consumer: (Change) -> Unit): (Array<out Change>) -> Unit = { changes -> changes.forEach(consumer) }