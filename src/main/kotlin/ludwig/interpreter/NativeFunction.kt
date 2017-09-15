package ludwig.interpreter

import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Method
import java.lang.reflect.Proxy

class NativeFunction(private val method: Method) : Callable {

    private val paramTypes: Array<Class<*>>
    override val isLazy: Boolean

    init {
        this.paramTypes = method.parameterTypes

        isLazy = method.isAnnotationPresent(Lazy::class.java)
    }

    override fun tail(args: Array<Any?>): Any? {
        try {
            for (i in args.indices) {
                args[i] = cast(args[i], paramTypes[i])
            }
            return method.invoke(null, *args)
        } catch (e: IllegalAccessException) {
            throw RuntimeException(e)
        } catch (e: InvocationTargetException) {
            throw RuntimeException(e)
        }

    }

    override fun argCount(): Int {
        return paramTypes.size
    }

    companion object {
        private val EMPTY = arrayOf<Any?>()

        private fun cast(o: Any?, type: Class<*>): Any? {
            if (o == null) {
                return null
            }
            if (type.isInstance(o)) {
                return o
            }

            if (type == Double::class.java || type == Double::class.javaPrimitiveType) {
                return (o as Number).toDouble()
            }

            if (type == Long::class.javaPrimitiveType) {
                return o
            }

            if (o is Callable) {
                val theMethod = functionalMethod(type)
                return Proxy.newProxyInstance(Callable::class.java.classLoader, arrayOf(type)) { proxy, method, args ->
                    {
                        if (method == theMethod) {
                            o.call(args ?: EMPTY);
                        } else {
                            method.invoke(o, args)
                        }
                    }
                }
            }

            return type.cast(o)
        }

        private fun functionalMethod(type: Class<*>?): Method {
            var type = type
            if (type!!.isInterface) {
                while (type != null) {
                    for (m in type.declaredMethods) {
                        if (!m.isDefault) {
                            return m
                        }
                    }
                    type = type.superclass
                }
            }
            throw RuntimeException("Cannot find a functional method in " + type!!)
        }
    }
}
