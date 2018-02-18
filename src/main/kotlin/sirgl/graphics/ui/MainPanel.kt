package sirgl.graphics.ui

import sirgl.graphics.canvas.Canvas
import sirgl.graphics.canvas.ScrolledCanvas
import sirgl.graphics.components.SplitPanel
import sirgl.graphics.core.App
import javax.swing.JPanel

class MainPanel(app: App) : JPanel() {
    init {
        app.repaintAllObservable.subscribe {
            revalidate()
            repaint()
        }
        val scrolledCanvas = ScrolledCanvas(app, Canvas(app))
        val settingsPanel = SettingsPanel(app)
        add(SplitPanel(scrolledCanvas, settingsPanel, 1.5, 0.5))
    }
}