package sirgl.graphics.core

import sirgl.graphics.conversion.toLab
import sirgl.graphics.gist.Gist
import sirgl.graphics.observable.Observable
import sirgl.graphics.observable.SimpleObservable
import sirgl.graphics.observable.map
import java.awt.Color
import java.awt.image.BufferedImage

interface AppGists : ImageChangeListener {
    val gistObservable: Observable<Gist>
    val gistTypeObservable: Observable<GistType>
}

class Gists(imageObservable: Observable<BufferedImage>) : AppGists {
    override fun notifyOriginalImageChanged() {
        needNewGistData = true
    }

    var needNewGistData = true
    var gistData = FloatArray(1)

    override val gistObservable: Observable<Gist> = imageObservable.map { recomputeGist(it ?: return@map null) }
    override val gistTypeObservable: Observable<GistType> = SimpleObservable<GistType>(GistType.L)

    private fun recomputeGist(img: BufferedImage): Gist? {
        if (needNewGistData) {
            gistData = FloatArray(img.height * img.width)
            needNewGistData = false
        }
        val gistType = gistTypeObservable.value ?: return null
        var counter = 0
        for (y in 0 until img.height) {
            for (x in 0 until img.width) {
                val lab = Color(img.getRGB(x, y)).toLab()
                val value = when (gistType) {
                    GistType.L -> lab.l
                    GistType.A -> lab.a
                    GistType.B -> lab.b
                }
                gistData[counter] = value
                counter++
            }
        }
        return Gist(gistData)
    }
}