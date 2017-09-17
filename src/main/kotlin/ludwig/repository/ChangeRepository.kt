package ludwig.repository

import ludwig.changes.Change

import java.io.IOException

interface ChangeRepository {
    @Throws(IOException::class)
    fun push(changes: Array<out Change>)

    @Throws(IOException::class)
    fun pull(sinceChangeId: String?): Array<out Change>
}
