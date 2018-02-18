package sirgl.graphics.ui

import sirgl.graphics.components.Dropdown
import sirgl.graphics.components.vBox
import sirgl.graphics.core.App
import sirgl.graphics.core.GistType
import sirgl.graphics.observable.map
import sirgl.graphics.observable.transmitTo
import java.awt.Dimension
import javax.swing.JFrame
import javax.swing.WindowConstants


class GistFrame(app: App) : JFrame() {
    init {
        size = Dimension(250, 350)
        minimumSize = Dimension(250, 350)
        add(vBox {
            val dropdown = Dropdown(GistType.values().map { it.name }, "Gist type")
            dropdown.observable.map {
                it ?: return@map null
                GistType.valueOf(it)
            }.transmitTo(app.gistTypeObservable)
            add(dropdown)
            add(GistCanvas(app.gistObservable))
        })

        defaultCloseOperation = WindowConstants.DISPOSE_ON_CLOSE
    }
}