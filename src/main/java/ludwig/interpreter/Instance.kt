package ludwig.interpreter

import ludwig.model.VariableNode

import java.util.HashMap

class Instance(private val type: ClassType) {
    private val data = HashMap<VariableNode, Any?>()

    fun type(): ClassType {
        return type
    }

    operator fun <R> get(field: VariableNode): R {
        return data[field] as R
    }

    operator fun <R> set(field: VariableNode, value: R) {
        data.put(field, value)
    }

    override fun toString(): String {
        val builder = StringBuilder()
        builder.append(type).append("{")
        var first = true
        for (field in type.fields()) {
            if (!first) {
                builder.append(", ")
            }
            builder.append(field).append(": ").append(data[field])
            first = false
        }
        return builder.append("}").toString()
    }
}
