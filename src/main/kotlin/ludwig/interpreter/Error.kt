package ludwig.interpreter

class Error(val message: String) : Signal {

    init {
        error.set(this)
    }

    override fun toString(): String {
        return "Error: " + message
    }

    companion object {
        private val error = ThreadLocal<Error?>()

        fun error(): Error? {
            return error.get()
        }

        fun reset() {
            error.remove()
        }
    }
}
