package ludwig.changes

import com.fasterxml.jackson.annotation.JsonTypeInfo
import java.util.*

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME)
abstract class Change {
    var changeId = newId()
}

fun newId(): String {
    return UUID.randomUUID().toString()
}

class Create(var nodeType: String = "", var parent: String? = null, var prev: String? = null, var next: String? = null) : Change()

class Comment(var nodeId: String = "", var comment: String? = null) : Change()

class Value(var nodeId: String = "", var value: String = "") : Change()

class Delete(var nodeId: String = "") : Change()

class Rename(var nodeId: String = "", var name: String = "") : Change()

class Lazy(var nodeId: String = "", var lazy: Boolean = false) : Change()