package sirgl.graphics

import sirgl.graphics.core.App
import sirgl.graphics.ui.MainFrame

fun main(args: Array<String>) {
    val app = App()
    app.init()
    MainFrame(app).isVisible = true
}