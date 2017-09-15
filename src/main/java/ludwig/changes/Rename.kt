package ludwig.changes

class Rename : Change() {
    lateinit var nodeId: String
    lateinit var  name: String

    override fun <T> accept(visitor: ChangeVisitor<T>): T {
        return visitor.visitRename(this)
    }
}
