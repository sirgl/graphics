package sirgl.graphics.ui

import sirgl.graphics.core.App
import javax.swing.DefaultListModel
import javax.swing.JList
import javax.swing.JPanel

class FilterPanel(app: App) : JPanel() {
    private val filterListModel = DefaultListModel<String>()
    val filterList = JList<String>(filterListModel)

    init {
        add(filterList)
    }
}