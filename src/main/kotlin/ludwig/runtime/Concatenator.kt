package ludwig.runtime


class Concatenator : (Any?) -> Unit {
    private val builder = StringBuilder()

    override fun invoke(o: Any?) {
        when (o) {
            is String -> builder.append(o)
            is Double -> builder.append(o.toDouble())
            is Long -> builder.append(o.toLong())
            else -> builder.append(o)
        }
    }

    override fun toString(): String {
        return builder.toString()
    }
}
