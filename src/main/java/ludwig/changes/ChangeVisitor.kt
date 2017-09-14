package ludwig.changes

interface ChangeVisitor<T> {

    fun visitInsertNode(insert: InsertNode): T

    fun visitInsertReference(insert: InsertReference): T

    fun visitDelete(delete: Delete): T

    fun visitComment(comment: Comment): T

    fun visitRename(rename: Rename): T
}