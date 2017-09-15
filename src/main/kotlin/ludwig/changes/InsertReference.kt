package ludwig.changes

class InsertReference : Insert() {
    lateinit var id: String
    lateinit var ref: String

    override fun <T> accept(visitor: ChangeVisitor<T>): T {
        return visitor.visitInsertReference(this)
    }
}
