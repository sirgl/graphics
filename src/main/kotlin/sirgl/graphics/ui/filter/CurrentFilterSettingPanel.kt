package sirgl.graphics.ui.filter

import sirgl.graphics.observable.Observable
import sirgl.graphics.ui.Invisible
import javax.swing.JPanel
import javax.swing.JScrollPane

class CurrentFilterSettingPanel(selectedFilterPanel: Observable<JPanel>) : JPanel() {
    private val holder = JPanel()
    private val scrollPane = JScrollPane(holder)

    init {
        scrollPane.isVisible = false
        selectedFilterPanel.subscribe {
            holder.removeAll()
            if (it == null) {
                scrollPane.isVisible = false
                holder.invalidate()
                holder.repaint()
                invalidate()
                revalidate()
                repaint()
                return@subscribe
            }
            if (it !is Invisible) {
                scrollPane.isVisible = true
            }
            holder.add(it)
            holder.invalidate()
            holder.repaint()
            invalidate()
            revalidate()
            repaint()
        }
        add(scrollPane)
    }
}