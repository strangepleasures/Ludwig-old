package ludwig.changes

class Delete : Change<Delete>() {
    private var id: String? = null

    fun id(): String? {
        return id
    }

    fun id(id: String): Delete {
        this.id = id
        return this
    }

    override fun <T> accept(visitor: ChangeVisitor<T>): T {
        return visitor.visitDelete(this)
    }
}
