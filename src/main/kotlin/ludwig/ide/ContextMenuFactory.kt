package ludwig.ide

import javafx.scene.control.ContextMenu
import javafx.scene.control.MenuItem
import java.lang.reflect.Modifier

object ContextMenuFactory {
    fun menu(bean: Any): ContextMenu {
        val menu = ContextMenu()

        for (method in bean.javaClass.declaredMethods) {
            if (Modifier.isPublic(method.modifiers) && method.parameterTypes.isEmpty()) {
                val menuItem = MenuItem()
                menuItem.text = mangle(method.name)

                menuItem.setOnAction { e ->
                    try {
                        method.invoke(bean)
                    } catch (ignore: Exception) {
                    }
                }

                menu.items.add(menuItem)
            }
        }

        return menu
    }

    private fun mangle(name: String): String {
        val builder = StringBuilder()
        for (i in 0 until name.length) {
            var c = name[i]

            if (builder.isEmpty()) {
                c = Character.toUpperCase(c)
            } else if (Character.isUpperCase(c)) {
                builder.append(' ')
            }

            builder.append(c)
        }
        return builder.toString()
    }
}
