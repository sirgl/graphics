package sirgl.graphics.filter

import javax.swing.ImageIcon

fun loadIcon(name: String): ImageIcon {
    val resource = Presentable::class.java.getResource("/" + name)
    return ImageIcon(resource)
}