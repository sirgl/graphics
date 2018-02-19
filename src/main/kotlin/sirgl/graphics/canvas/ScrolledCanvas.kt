package sirgl.graphics.canvas

import sirgl.graphics.core.App
import java.awt.Dimension
import javax.swing.JScrollPane

class ScrolledCanvas(app: App, canvas: Canvas) : JScrollPane(canvas) {
    init {
//        isVisible = false
        app.imageObservable.subscribe {
            it ?: return@subscribe
//            isVisible = true
            preferredSize = Dimension(it.width, it.height)
        }
    }
}