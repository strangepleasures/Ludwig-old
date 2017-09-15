package ludwig.runtime


class Concatenator : (Any?) -> Unit {
    private val builder = StringBuilder()

    override fun invoke(o: Any?) {
        if (o is String) {
            builder.append(o)
        } else if (o is Double) {
            builder.append(o.toDouble())
        } else if (o is Long) {
            builder.append(o.toLong())
        } else {
            builder.append(o)
        }
    }

    override fun toString(): String {
        return builder.toString()
    }
}
