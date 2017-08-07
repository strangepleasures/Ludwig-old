package ludwig.ide;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class Icons {
    public static ImageView icon(String name) {
        return new ImageView(new Image(Icons.class.getResourceAsStream("/icons/" + name + ".png")));
    }
}
