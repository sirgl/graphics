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
    private val panelHeight = 200

    init {
        preferredSize = Dimension(200, 200)
        gistObservable.subscribe {
            it ?: return@subscribe
            gist = it
            repaint()
        }
    }

    override fun paint(g: Graphics) {
        super.paint(g)
        val gistVal = gist
        gistVal ?: return
        val buckets = gistVal.buckets
        val maxSize = buckets.max() ?: return
        for ((index, bucketSize) in buckets.withIndex()) {
            drawBucket(index, bucketSize, g, gistVal, maxSize)
        }
    }

    fun drawBucket(bucketIndex: Int, bucketSize: Int, g: Graphics, gist: Gist, maxSize: Int) {
        val bucketHeight = Math.round(bucketSize.toDouble() / maxSize * 200).toInt()
        val x = bucketIndex * bucketBoxWidth
        val y = panelHeight - bucketHeight
        g.drawRect(x, y, bucketWidth, bucketHeight)
    }
}