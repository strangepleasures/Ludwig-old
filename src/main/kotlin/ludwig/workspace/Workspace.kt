package ludwig.workspace

import ludwig.changes.*
import ludwig.interpreter.Builtins
import ludwig.model.NamedNode
import ludwig.model.Node
import ludwig.model.ProjectNode
import ludwig.model.ReferenceNode
import ludwig.runtime.StdLib
import ludwig.script.Parser
import java.io.InputStreamReader
import java.util.*
import java.util.function.Consumer

class Workspace {

    private val nodes = HashMap<String, Node>()
    private val appliedChanges = ArrayList<Change>()
    val projects = ArrayList<ProjectNode>()
    private val changeListeners = ArrayList<(Change) -> Unit>()
    private val builtins = ProjectNode()
    var isBatchUpdate: Boolean = false
        private set
    var isLoading: Boolean = false
        private set

    fun init() {
        builtins.name = "Runtime"
        builtins.readonly = true
        builtins.id = "Runtime"
        builtins.children.add(Builtins.of(StdLib))

        addNode(builtins)

        try {
            InputStreamReader(Workspace::class.java.getResourceAsStream("/system.lw")).use { reader -> Parser.parse(reader, this, builtins!!) }
            InputStreamReader(Workspace::class.java.getResourceAsStream("/system-tests.lw")).use { reader -> Parser.parse(reader, this, builtins!!) }
        } catch (e: Exception) {
            throw RuntimeException(e)
        }

    }

    fun changeListeners(): MutableList<(Change) -> Unit> {
        return changeListeners
    }

    private val changeVisitor = object : ChangeVisitor<Problem?> {

        override fun visitInsertNode(insert: InsertNode): Problem? {
            return place(insert.node, insert)
        }

        override fun visitInsertReference(insert: InsertReference): Problem? {
            val ref = ReferenceNode(node(insert.ref)!!)
            ref.id = insert.id
            return place(ref, insert)
        }

        override fun visitDelete(delete: Delete): Problem? {
            val node = node<Node>(delete.id)
            node!!.parent!!.children.remove(node)
            node.delete()
            return null
        }

        override fun visitComment(comment: Comment): Problem? {
            val node = node<Node>(comment.nodeId)
            node!!.comment = comment.comment
            return null
        }

        override fun visitRename(rename: Rename): Problem? {
            val node = node<NamedNode>(rename.nodeId)
            node!!.name = rename.name
            return null
        }
    }

    private fun place(node: Node, insert: Insert): Problem? {
        addNode(node)
        val parent = node<Node>(insert.parent)
        node.parent = parent

        if (parent != null) {
            val prev = node<Node>(insert.prev)
            val next = node<Node>(insert.next)

            if (!parent.isOrdered) {
                parent.children.add(node)
            } else {
                val items = parent.children

                if (next == null) {
                    if (!items.isEmpty() && items[items.size - 1] === prev || items.isEmpty() && prev == null) {
                        parent.children.add(node)
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

    fun getProjects(): List<ProjectNode> {
        return projects
    }

    fun apply(changes: List<Change>): List<Problem> {
        val problems = ArrayList<Problem>()
        for (i in changes.indices) {
            val change = changes[i]
            isBatchUpdate = i < changes.size - 1
            val problem = change.accept(changeVisitor)
            if (problem != null) {
                problems.add(problem)
                if (problems.size == MAX_PROBLEMS) {
                    break
                }
            } else {
                changeListeners.forEach { listener -> listener(change) }
            }
        }

        if (problems.isEmpty()) { // TODO: Make a distinction between warnings and errors
            appliedChanges.addAll(changes)


        } else {
            restore()
        }
        return problems
    }

    fun load(changes: List<Change>): List<Problem> {
        isLoading = true
        try {
            return apply(changes)
        } finally {
            isLoading = false
        }
    }

    private fun restore() {
        nodes.clear()
        projects.clear()

        val changes = ArrayList(appliedChanges)
        appliedChanges.clear()
        apply(changes)
    }

    fun <T : Node> node(id: String?): T? {
        return if (id == null) null else nodes[id] as T
    }

    fun addNode(node: Node) {
        nodes.put(node.id, node)
        if (node is ProjectNode) {
            projects.add(node)
        }
        node.children.forEach(Consumer { this.addNode(it) })
    }

    companion object {
        val MAX_PROBLEMS = 10
    }
}
