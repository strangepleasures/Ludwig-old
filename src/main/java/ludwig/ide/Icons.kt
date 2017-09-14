package ludwig.ide

import javafx.scene.image.Image
import javafx.scene.image.ImageView

object Icons {
    fun icon(name: String): ImageView {
        return ImageView(Image(Icons::class.java.getResourceAsStream("/icons/$name.png")))
    }
}
