package sirgl.graphics.filter

import javax.swing.JPanel

interface FilterModel : Presentable {
    val filter: ImageFilter
    val panel: JPanel
}