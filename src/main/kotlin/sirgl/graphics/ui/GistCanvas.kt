package sirgl.graphics.ui

import sirgl.graphics.gist.Gist
import sirgl.graphics.observable.Observable
import java.awt.Dimension
import java.awt.Graphics
import javax.swing.JPanel

private const val bucketBoxWidth = 5
private const val bucketWidth = bucketBoxWidth - 2


class GistCanvas(gistObservable: Observable<Gist>) : JPanel() {
    var gist: Gist? = null
    val panelHeight = 200

    init {
        size = Dimension(200, panelHeight)
        gistObservable.subscribe {
            it ?: return@subscribe
            repaint()
        }
    }

    override fun paint(g: Graphics) {
        super.paint(g)
        val gistVal = gist
        gistVal ?: return
        for ((index, bucketSize) in gistVal.buckets.withIndex()) {
            drawBucket(index, bucketSize, g, gistVal)
        }
    }

    fun drawBucket(bucketIndex: Int, bucketSize: Int, g: Graphics, gist: Gist) {
        val bucketHeight = Math.round(bucketSize - gist.min / gist.diff * 200).toInt()
        val x = bucketIndex * bucketBoxWidth
        val y = panelHeight - bucketHeight
        g.drawRect(x, y, bucketWidth, bucketHeight)
    }
}