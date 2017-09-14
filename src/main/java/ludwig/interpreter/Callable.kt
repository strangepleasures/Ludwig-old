package ludwig.interpreter

interface Callable {
    fun call(args: Array<Any?>): Any? {
        val result = tail(args)

        return if (result is Delayed<*>) {
            result.get()
        } else result

    }

    fun tail(args: Array<Any?>): Any?

    val isLazy: Boolean
        get() = false

    fun argCount(): Int
}
