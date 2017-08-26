package ludwig.ide;

import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public class ContextMenuFactory {
    public static ContextMenu menu(Object bean) {
        ContextMenu menu = new ContextMenu();

        for (Method method: bean.getClass().getDeclaredMethods()) {
            if (Modifier.isPublic(method.getModifiers()) && method.getParameterTypes().length == 0) {
                MenuItem menuItem = new MenuItem();
                menuItem.setText(mangle(method.getName()));

                menuItem.setOnAction(e -> {
                    try {
                        method.invoke(bean);
                    } catch (Exception ignore) {
                    }
                });

                menu.getItems().add(menuItem);
            }
        }

        return menu;
    }

    private static String mangle(String name) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < name.length(); i++) {
            char c = name.charAt(i);

            if (builder.length() == 0) {
                c = Character.toUpperCase(c);
            } else if (Character.isUpperCase(c)) {
                builder.append(' ');
            }

            builder.append(c);
        }
        return builder.toString();
    }
}
