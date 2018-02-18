package sirgl.graphics.canvas

import sirgl.graphics.observable.Observable
import sirgl.graphics.observable.SimpleObservable
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.awt.image.BufferedImage


class Point(val x: Int, val y: Int) {
    fun isInside(img: BufferedImage) = x >= 0 && y >= 0 && img.width > x && img.height > y
}

class MouseDraggedEvt(val oldPoint: Point, val newPoint: Point)

class MouseObserver : MouseAdapter() {
    val currentPositionObservable: Observable<Point> = SimpleObservable<Point>(null)
    val mouseDraggedObservable: Observable<MouseDraggedEvt> = SimpleObservable<MouseDraggedEvt>(null)

    private var startPoint: Point? = null

    override fun mousePressed(e: MouseEvent?) {
        startPoint = e.point()
    }

    override fun mouseReleased(e: MouseEvent?) {
        val startPointVal = startPoint ?: return
        val newPoint = e.point()
        if (newPoint == null) {
            startPoint = null
            return
        }
        mouseDraggedObservable.value = MouseDraggedEvt(startPointVal, newPoint)
    }

    override fun mouseMoved(e: MouseEvent?) {
        currentPositionObservable.value = e.point()
    }

    private fun MouseEvent?.point() = if (this != null) Point(x, y) else null
}