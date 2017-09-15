package ludwig.changes

class Comment : Change() {
    lateinit var nodeId: String
    var comment: String? = null

    override fun <T> accept(visitor: ChangeVisitor<T>): T {
        return visitor.visitComment(this)
    }
}
