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

abstract class Insert : Change() {
    var parent: String? = null
    var prev: String? = null
    var next: String? = null
}

class Create : Insert() {
    lateinit var nodeType: String
}

class Comment : Change() {
    lateinit var nodeId: String
    var comment: String? = null
}

class Value : Change() {
    lateinit var nodeId: String
    lateinit var value: String
}

class Delete : Change() {
    lateinit var nodeId: String
}

class Rename : Change() {
    lateinit var nodeId: String
    lateinit var name: String
}

class Lazy : Change() {
    lateinit var nodeId: String
    var lazy: Boolean = false
}