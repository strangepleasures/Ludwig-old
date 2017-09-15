package ludwig.interpreter

import ludwig.model.FunctionNode
import ludwig.model.PackageNode
import ludwig.model.PlaceholderNode
import ludwig.model.VariableNode
import java.lang.reflect.Method
import java.lang.reflect.Modifier
import java.util.*

object Builtins {
    private val callables = HashMap<FunctionNode, Callable>()

    fun callable(fn: FunctionNode): Callable? {
        return callables[fn]
    }

    fun of(clazz: Class<*>): PackageNode {
        val p = PackageNode()
        val packageName: String
        if (clazz.isAnnotationPresent(Name::class.java)) {
            packageName = clazz.getAnnotation(Name::class.java).value
        } else {
            packageName = clazz.simpleName.toLowerCase()
        }
        p.name(packageName).id(packageName)

        for (method in clazz.declaredMethods) {
            if (Modifier.isPublic(method.modifiers)) {
                val fn = function(method)
                callables.put(fn, NativeFunction(method))
                p.add(fn)
            }
        }
        return p
    }

    private fun function(method: Method): FunctionNode {
        val f = FunctionNode()

        f.lazy(method.isAnnotationPresent(Lazy::class.java))

        val methodName = if (method.isAnnotationPresent(Name::class.java)) method.getAnnotation(Name::class.java).value else method.name
        f.name(methodName)

        val packageName: String
        if (method.declaringClass.isAnnotationPresent(Name::class.java)) {
            packageName = method.declaringClass.getAnnotation(Name::class.java).value
        } else {
            packageName = method.declaringClass.simpleName.toLowerCase()
        }

        f.id(packageName + ":" + methodName)

        if (method.isAnnotationPresent(Description::class.java)) {
            f.comment(method.getAnnotation(Description::class.java).value)
        }

        if (method.isAnnotationPresent(Visibility::class.java)) {
            f.visibility(method.getAnnotation(Visibility::class.java).value)
        }

        for (parameter in method.parameters) {
            val param = VariableNode()
            val paramName = if (parameter.isAnnotationPresent(Name::class.java)) parameter.getAnnotation(Name::class.java).value else parameter.name
            param.name(paramName)
                    .id(f.id() + ":" + paramName)
            if (parameter.isAnnotationPresent(Description::class.java)) {
                param.comment(parameter.getAnnotation(Description::class.java).value)
            }
            f.add(param)
        }
        f.add(PlaceholderNode().parameter("Built-in function").id(f.id()!! + ":body"))

        return f
    }
}

