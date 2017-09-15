package ludwig.changes

import com.fasterxml.jackson.annotation.JsonTypeInfo
import java.util.*

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME)
abstract class Change<T : Change<T>> {
    val changeId = newId()

    abstract fun <T> accept(visitor: ChangeVisitor<T>): T

    companion object {

        fun newId(): String {
            return UUID.randomUUID().toString()
        }
    }
}
