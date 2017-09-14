package ludwig.interpreter

@FunctionalInterface
interface Delayed<T> {
    fun get(): T
}
