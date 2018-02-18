package sirgl.graphics.ui

import sirgl.graphics.canvas.Canvas
import sirgl.graphics.canvas.ScrolledCanvas
import sirgl.graphics.components.SplitPanel
import sirgl.graphics.core.App
import java.awt.Dimension
import javax.swing.JFrame
import javax.swing.JPanel
import javax.swing.JTextArea
import javax.swing.WindowConstants

class MainPanel(app: App) : JPanel() {
    init {
        app.repaintAllObservable.subscribe {
            revalidate()
            repaint()
        }
        val scrolledCanvas = ScrolledCanvas(app, Canvas(app))
        val settingsPanel = SettingsPanel(app)
        add(SplitPanel(scrolledCanvas, settingsPanel, 1.5, 0.5))

        app.selectedRegionText.subscribe {
            it ?: return@subscribe
            val frame = JFrame()
            frame.size = Dimension(200, 50)
            val area = JTextArea(it,1, 30)
            frame.add(area)
            frame.defaultCloseOperation = WindowConstants.DISPOSE_ON_CLOSE
            frame.isVisible = true
        }
    }
}