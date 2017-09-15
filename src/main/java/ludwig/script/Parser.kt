package ludwig.script

import ludwig.changes.Change
import ludwig.changes.InsertNode
import ludwig.changes.InsertReference
import ludwig.interpreter.ClassType
import ludwig.model.*
import ludwig.utils.NodeUtils.isField
import ludwig.workspace.Workspace
import org.pcollections.HashPMap
import org.pcollections.HashTreePMap
import java.io.IOException
import java.io.Reader

class Parser private constructor(private val tokens: List<String>, private val workspace: Workspace) {

    private var pos: Int = 0
    private var locals: HashPMap<String, NamedNode<*>> = HashTreePMap.empty()
    private var superFunction: Node<*>? = null

    @Throws(ParserException::class)
    private fun parse(projectNode: ProjectNode) {
        val packageNode = parseSignatures(projectNode)
        parseBodies(packageNode)
    }

    @Throws(ParserException::class)
    private fun parseSignatures(projectNode: ProjectNode): PackageNode {
        consume("(")
        consume("package")

        val packageName = nextToken()
        val packageNode = projectNode.children().stream().map { n -> n as PackageNode }
                .filter { n -> n.name() == packageName }
                .findFirst()
                .orElseGet { append(projectNode, PackageNode().name(packageName)) }

        consume(")")


        while (pos < tokens.size) {
            parseSignature(packageNode)
        }

        return packageNode
    }

    @Throws(ParserException::class)
    private fun parseSignature(packageNode: PackageNode) {
        consume("(")

        when (nextToken()) {
            "class" -> {
                val classNode = append(packageNode, ClassNode().name(nextToken()))
                if (currentToken() != ")") {
                    val superClass = find(nextToken()) as ClassNode?
                    appendRef(classNode, superClass)
                }
                while (currentToken() != ")") {
                    append(classNode, VariableNode().name(nextToken()))
                }
                consume(")")
                ClassType.of(classNode!!)
            }
            "def" -> {
                var lazy = false
                if (currentToken() == "lazy") {
                    lazy = true
                    consume("lazy")
                }
                val fn = append(packageNode, FunctionNode().name(nextToken()).lazy(lazy))
                while (currentToken() != ")") {
                    append(fn, VariableNode().name(nextToken()))
                }
                consume(")")

                skipBody()
            }
            "method" -> {
                while (nextToken() != ")");
                skipBody()
            }
        }
    }

    @Throws(ParserException::class)
    private fun skipBody() {
        consume("(")
        var level = 1
        while (level != 0 && pos < tokens.size) {
            when (nextToken()) {
                "(" -> level++
                ")" -> level--
            }
        }
    }

    @Throws(ParserException::class)
    private fun parseBodies(packageNode: PackageNode) {
        rewind()
        consume("(")
        consume("package")

        nextToken()
        consume(")")

        while (pos < tokens.size) {
            parseBody(packageNode)
        }
    }

    @Throws(ParserException::class)
    private fun parseBody(packageNode: PackageNode) {
        consume("(")
        when (nextToken()) {
            "class" -> while (nextToken() != ")");
            "def" -> {
                if (currentToken() == "lazy") {
                    consume("lazy")
                }
                val node = item(packageNode, nextToken()) as FunctionNode?
                locals = HashTreePMap.empty<String, NamedNode<*>>()
                for (child in node!!.children()) {
                    if (child !is VariableNode) {
                        break
                    }
                    locals = locals.plus(child.name()!!, child)
                }
                while (nextToken() != ")");
                consume("(")
                while (pos < tokens.size && currentToken() != ")") {
                    parseChild(node)
                }

                if (pos < tokens.size) {
                    nextToken()
                }
            }
            "method" -> {
                val classNode = find(nextToken()) as ClassNode
                val fn = find(nextToken()) as FunctionNode
                val node = append(packageNode, OverrideNode())
                appendRef(node, fn)

                superFunction = findSuper(classNode, fn)


                locals = HashTreePMap.empty<String, NamedNode<*>>()

                for (child in fn!!.children()) {
                    if (child !is VariableNode) {
                        break
                    }
                    locals = locals.plus(child.name()!!, child)
                }

                for (child in classNode!!.children()) {
                    if (child !is VariableNode) {
                        continue
                    }
                    locals = locals.plus(child.name()!!, child)
                }

                while (nextToken() != ")");
                consume("(")
                while (pos < tokens.size && currentToken() != ")") {
                    parseChild(node)
                }

                if (pos < tokens.size) {
                    nextToken()
                }

                ClassType.of(classNode).overrides().put(fn, node!!)
            }
            "field" -> {
                nextToken()
                consume(")")
            }
        }
    }

    private fun findSuper(classNode: ClassNode, fn: FunctionNode): Node<*> {
        var t: ClassType? = ClassType.of(classNode)
        val s = t!!.implementation(fn)

        while (t != null) {
            val s1 = t.implementation(fn)
            if (s1 !== s) {
                return s1
            }

            t = t.superClass()
        }
        return fn
    }


    @Throws(ParserException::class)
    private fun parseChild(parent: Node<*>?) {
        var level = 0
        while (currentToken() == "(") {
            level++
            nextToken()
        }

        try {
            val head = nextToken()

            when (head) {
                "call", "if", "else", "return", "throw", "try", "catch", "list", "break", "continue" -> {
                    val node = append(parent, createSpecial(head))
                    while (currentToken() != ")") {
                        parseChild(node)
                    }
                }
                "ref" -> {
                    val ref = append(parent, FunctionReferenceNode())
                    appendRef(ref, find(nextToken()))
                }
                "for" -> {
                    val node = append(parent, ForNode())
                    val `var` = VariableNode()
                    `var`.name(nextToken())
                    append(node, `var`)

                    val savedLocals = locals
                    locals = locals.plus(`var`.name()!!, `var`)

                    while (currentToken() != ")") {
                        parseChild(node)
                    }
                    locals = savedLocals
                }
                "=" -> {
                    val name = nextToken()
                    val node = append(parent, AssignmentNode())

                    var isField = false
                    val savedPos = pos

                    val f = find(name)
                    if (isField(f)) {
                        val r = appendRef(node, f)
                        parseChild(r)
                        if (currentToken() != ")") {
                            parseChild(node)
                            isField = true
                        }
                    }

                    if (!isField) {
                        pos = savedPos
                        node!!.children().clear()
                        if (locals.containsKey(name)) {
                            appendRef(node, locals[name])
                            parseChild(node)
                        } else {
                            val lhs = append(node, VariableNode().name(name))
                            locals = locals.plus(name, lhs)
                            parseChild(node)
                        }
                    }
                }
                "λ", "\\" -> {
                    val node = append(parent, LambdaNode())
                    val savedLocals = locals
                    while (currentToken() != ")") {
                        val param = append(node, VariableNode().name(nextToken()))
                        locals = locals.plus(param!!.name()!!, param)
                    }
                    consume(")")
                    consume("(")
                    while (currentToken() != ")") {
                        parseChild(node)
                    }
                    locals = savedLocals
                }

                else -> {
                    if (locals.containsKey(head)) {
                        val local = locals[head]
                        if (isField(local)) {
                            val savedPos = pos
                            val fn = local as VariableNode
                            val r = appendRef(parent, fn)
                            if (currentToken() == ")") {
                                pos = savedPos
                                parent!!.children().removeAt(parent.children().size - 1)
                            } else {
                                parseChild(r)
                                return
                            }
                        } else {
                            appendRef(parent, local)
                        }
                        return
                    }

                    val headNode = if ("super" == head) superFunction else find(head)
                    if (headNode is FunctionNode) {
                        val fn = headNode as FunctionNode?
                        val r = appendRef(parent, fn)
                        for (param in fn!!.children()) {
                            if (param !is VariableNode) {
                                break
                            }
                            parseChild(r)
                        }
                    } else if (headNode is OverrideNode) {
                        val fn = (headNode.children()[0] as ReferenceNode).ref() as FunctionNode
                        val r = appendRef(parent, headNode)
                        for (param in fn.children()) {
                            if (param !is VariableNode) {
                                break
                            }
                            parseChild(r)
                        }
                    } else if (headNode is ClassNode) {
                        val cn = headNode as ClassNode?
                        val r = appendRef(parent, cn)
                        while (currentToken() != ")") {
                            parseChild(r)
                        }
                    } else if (Lexer.isLiteral(head)) {
                        append(parent, LiteralNode(head))
                    } else {
                        throw ParserException("Unknown symbol: " + head)
                    }
                }
            }
        } finally {
            for (i in 0 until level) {
                consume(")")
            }
        }
    }

    private fun nextToken(): String {
        return tokens[pos++]
    }

    private fun currentToken(): String {
        return tokens[pos]
    }

    @Throws(ParserException::class)
    private fun consume(token: String) {
        if (nextToken() != token) {
            throw ParserException("Expected " + token)
        }
    }

    private fun rewind() {
        pos = 0
    }

    // TODO: Optimize
    private fun find(name: String): NamedNode<*>? {
        return workspace.projects
                .flatMap { it.children() }
                .map { it as PackageNode }
                .firstOrNull { item(it, name) != null }
                ?.let { item(it, name) }
    }

    private fun createSpecial(token: String): Node<*>? {
        when (token) {
            "call" -> return CallNode()
            "if" -> return IfNode()
            "else" -> return ElseNode()
            "return" -> return ReturnNode()
            "list" -> return ListNode()
            "throw" -> return ThrowNode()
            "try" -> return TryNode()
            "catch" -> return ClassNode()
            "break" -> return BreakNode()
            "continue" -> return ContinueNode()
        }
        return null
    }

    private fun <T : Node<*>> append(parent: Node<*>?, node: T?): T? {
        if (node is NamedNode<*>) {
            node.id(parent!!.id() + ":" + (node as NamedNode<*>).name())
        } else {
            node!!.id(Change.newId())
        }
        val change = InsertNode()
                .node(node)
                .parent(parent!!.id())
                .prev(if (parent.children().isEmpty()) null else parent.children()[parent.children().size - 1].id())
        workspace.apply(listOf(change))
        return workspace.node<Node<*>>(node.id()) as T?
    }

    private fun appendRef(parent: Node<*>?, node: Node<*>?): ReferenceNode {
        val change = InsertReference()
                .id(Change.newId())
                .parent(parent!!.id())
                .prev(if (parent.children().isEmpty()) null else parent.children()[parent.children().size - 1].id())
                .next(null)
                .ref(node!!.id()!!)
        workspace.apply(listOf(change))
        return workspace.node<Node<*>>(change.id()) as ReferenceNode
    }

    companion object {


        @Throws(ParserException::class, IOException::class, LexerException::class)
        fun parse(reader: Reader, workspace: Workspace, projectNode: ProjectNode) {
            parse(Lexer.read(reader), workspace, projectNode)
        }

        @Throws(ParserException::class)
        fun parse(tokens: List<String>, workspace: Workspace, projectNode: ProjectNode) {
            Parser(tokens, workspace).parse(projectNode)
        }

        fun item(findByName: PackageNode, name: String): NamedNode<*>? {
            return findByName.children().stream().filter { n -> n is NamedNode<*> }.map { n -> n as NamedNode<*> }.filter { it -> it.name() == name }.findFirst().orElse(null)
        }
    }
}
