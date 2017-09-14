package ludwig.utils

import ludwig.interpreter.ClassType
import ludwig.model.*
import org.apache.commons.lang.StringEscapeUtils

import java.util.*
import java.util.stream.Collectors

object NodeUtils {

    fun parseLiteral(s: String): Any? {
        when (s) {
            "true" -> return true
            "false" -> return false
            "null" -> return null
            else -> {
                if (s.startsWith("'")) {
                    return StringEscapeUtils.unescapeJavaScript(s.substring(1, s.length - 1))
                }
                try {
                    return java.lang.Long.parseLong(s)
                } catch (e1: NumberFormatException) {
                    return java.lang.Double.parseDouble(s)
                }

            }
        }
    }

    fun formatLiteral(o: Any?): String {
        return if (o is String) {
            '\'' + StringEscapeUtils.escapeJavaScript(o.toString()) + '\''
        } else o.toString()
    }

    fun expandNode(node: Node<*>): List<Node<*>> {
        val nodes = ArrayList<Node<*>>()
        expandNode(node, true, nodes)
        return nodes
    }

    private fun expandNode(node: Node<*>, onlyChildren: Boolean, nodes: MutableList<Node<*>>) {
        if (!onlyChildren) {
            nodes.add(node)
        }
        for (child in node.children()) {
            expandNode(child, false, nodes)
        }
    }

    fun signature(obj: Any): String {
        if (obj is Node<*>) {
            if (obj is OverrideNode) {
                return signature(declaration(obj))
            }
            val builder = StringBuilder(obj.toString())
            for (child in obj.children()) {
                if (child is VariableNode) {
                    builder.append(' ')
                    builder.append(child.toString())
                } else {
                    break
                }
            }
            return builder.toString()
        }
        return obj.toString()
    }

    fun arguments(node: Node<*>): List<String> {
        if (node is ClassNode) {
            return ClassType.of(node).fields().stream().map( { it.toString() }).collect(Collectors.toList())
        }
        if (node is VariableNode) {
            return listOf("it")
        }
        if (node is OverrideNode) {
            return arguments(declaration(node))
        }
        val args = ArrayList<String>()
        for (child in node.children()) {
            if (child !is VariableNode) {
                break
            }
            args.add(child.name()!!)
        }
        return args
    }

    fun declaration(node: OverrideNode): FunctionNode {
        return (node.children()[0] as ReferenceNode).ref() as FunctionNode
    }

    fun isReadonly(node: Node<*>?): Boolean {
        return node == null || node.parentOfType(ProjectNode::class.java)!!.readonly()
    }

    private fun collectLocals(root: Node<*>, stop: Node<*>, filter: String, locals: MutableList<Node<*>>) {
        if (root === stop) {
            return
        }
        if (root is VariableNode && root.name()!!.startsWith(filter)) {
            locals.add(root)
        }
        root.children().forEach { child -> collectLocals(child, stop, filter, locals) }
    }

    fun collectLocals(root: Node<*>, stop: Node<*>, filter: String): List<Node<*>> {
        val locals = ArrayList<Node<*>>()
        collectLocals(root, stop, filter, locals)
        locals.sortBy { it.toString() }
        return locals
    }

    fun isField(node: Node<*>?): Boolean {
        return node is VariableNode && node.parent() is ClassNode
    }

    fun argumentsCount(node: Node<*>): Int {
        return node.accept(ArgumentsCount())!!
    }
}