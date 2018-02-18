package sirgl.graphics.components

import javax.swing.BoxLayout
import javax.swing.JPanel

fun panel(builder: JPanel.() -> Unit) {
    val panel = JPanel()
    builder(panel)
}

fun box(horiziontal: Boolean, builder: JPanel.() -> Unit) {
    val panel = JPanel()
    panel.layout = BoxLayout(panel, if (horiziontal) BoxLayout.X_AXIS else BoxLayout.Y_AXIS)
    builder(panel)
}

fun hBox(builder: JPanel.() -> Unit) = box(true, builder)

fun vBox(builder: JPanel.() -> Unit) = box(false, builder)
