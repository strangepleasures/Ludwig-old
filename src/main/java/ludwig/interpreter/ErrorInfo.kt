package ludwig.interpreter

class ErrorInfo(private val error: Error) {

    override fun toString(): String {
        return error.toString()
    }
}
