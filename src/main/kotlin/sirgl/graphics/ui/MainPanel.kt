package sirgl.graphics.ui

import sirgl.graphics.canvas.Canvas
import sirgl.graphics.canvas.ScrolledCanvas
import sirgl.graphics.components.SplitPanel
import sirgl.graphics.core.App
import java.awt.Dimension
import java.awt.GridBagConstraints
import javax.swing.*

class MainPanel(app: App) : JPanel() {
    init {
        layout = BoxLayout(this, BoxLayout.X_AXIS)
        app.repaintAllObservable.subscribe {
            revalidate()
            repaint()
        }
        minimumSize = Dimension(800, 600)

        val scrolledCanvas = ScrolledCanvas(app, Canvas(app))
        val settingsPanelWrapper = SettingsPanel(app)
        add(SplitPanel(scrolledCanvas, settingsPanelWrapper, 2.5, 0.5) {
            rightConstraint.fill = GridBagConstraints.NONE
        })

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