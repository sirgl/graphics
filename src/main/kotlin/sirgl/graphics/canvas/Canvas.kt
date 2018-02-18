package sirgl.graphics.canvas

import sirgl.graphics.core.App
import sirgl.graphics.core.RefreshAllEvent
import sirgl.graphics.observable.transmitTo
import java.awt.Dimension
import java.awt.Graphics
import java.awt.image.BufferedImage
import javax.swing.JPanel

class Canvas(app: App) : JPanel() {
    private var image: BufferedImage? = null

    private val mouseObserver = MouseObserver()

    init {
        isVisible = false
        addMouseListener(mouseObserver)
        addMouseMotionListener(mouseObserver)
        mouseObserver.mouseDraggedObservable.transmitTo(app.mouseDraggedObservable)
        mouseObserver.currentPositionObservable.transmitTo(app.currentPositionObservable)
        app.imageToDrawObservable.subscribe {
            it ?: return@subscribe
            isVisible = true
            println("Image updated")
            preferredSize = Dimension(it.width, it.height)
            size = Dimension(it.width, it.height)
            image = it
            app.repaintAll()
        }
    }

    override fun paint(g: Graphics) {
        super.paint(g)
        val img = image ?: return
        g.drawImage(img, 0, 0, null)
    }
}