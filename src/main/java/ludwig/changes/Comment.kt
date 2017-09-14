package ludwig.changes

class Comment : Change<Comment>() {
    private var nodeId: String? = null
    private var comment: String? = null

    fun nodeId(): String? {
        return nodeId
    }

    fun nodeId(nodeId: String): Comment {
        this.nodeId = nodeId
        return this
    }

    fun comment(): String? {
        return comment
    }

    fun comment(comment: String): Comment {
        this.comment = comment
        return this
    }

    override fun <T> accept(visitor: ChangeVisitor<T>): T {
        return visitor.visitComment(this)
    }
}
