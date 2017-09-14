package ludwig.changes

class InsertReference : Insert<InsertReference>() {
    private var id: String? = null
    private var ref: String? = null

    fun id(): String? {
        return id
    }

    fun id(id: String): InsertReference {
        this.id = id
        return this
    }

    fun ref(): String? {
        return ref
    }

    fun ref(ref: String): InsertReference {
        this.ref = ref
        return this
    }

    override fun <T> accept(visitor: ChangeVisitor<T>): T {
        return visitor.visitInsertReference(this)
    }
}
