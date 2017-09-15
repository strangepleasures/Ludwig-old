package ludwig.repository

import ludwig.changes.Change

import java.io.IOException

interface ChangeRepository {
    @Throws(IOException::class)
    fun push(changes: List<Change>)

    @Throws(IOException::class)
    fun pull(sinceChangeId: String?): List<Change>
}
