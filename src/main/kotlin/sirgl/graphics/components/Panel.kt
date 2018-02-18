package sirgl.graphics.components

import javax.swing.BoxLayout
import javax.swing.JComponent
import javax.swing.JPanel

fun panel(builder: JPanel.() -> Unit): JPanel {
    val panel = JPanel()
    builder(panel)
    return panel
}

fun box(horiziontal: Boolean, builder: JPanel.() -> Unit): JPanel {
    val panel = JPanel()
    panel.layout = BoxLayout(panel, if (horiziontal) BoxLayout.X_AXIS else BoxLayout.Y_AXIS)
    builder(panel)
    return panel
}

fun hBox(builder: JPanel.() -> Unit) = box(true, builder)

fun vBox(builder: JPanel.() -> Unit) = box(false, builder)


fun JComponent.addVBox(builder: JPanel.() -> Unit) = add(vBox(builder))
fun JComponent.addHBox(builder: JPanel.() -> Unit) = add(hBox(builder))
