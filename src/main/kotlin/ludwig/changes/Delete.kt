package ludwig.changes

class Delete : Change() {
    lateinit var id: String

    override fun <T> accept(visitor: ChangeVisitor<T>): T {
        return visitor.visitDelete(this)
    }
}
