package sirgl.graphics

import sirgl.graphics.core.App
import sirgl.graphics.ui.GistFrame
import sirgl.graphics.ui.MainFrame

fun main(args: Array<String>) {
    val app = App()
    GistFrame(app).isVisible = true
    MainFrame(app).isVisible = true
}