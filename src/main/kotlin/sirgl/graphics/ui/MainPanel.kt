package sirgl.graphics.ui

import sirgl.graphics.canvas.Canvas
import sirgl.graphics.canvas.ScrolledCanvas
import sirgl.graphics.core.App
import javax.swing.JPanel

class MainPanel(app: App) : JPanel() {
    init {
        app.repaintAllObservable.subscribe {
            revalidate()
            repaint()
        }
        add(ScrolledCanvas(app, Canvas(app)))
    }
}