package sirgl.graphics.ui

import sirgl.graphics.core.App
import java.awt.Dimension
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import javax.swing.JButton
import javax.swing.JList
import javax.swing.JPanel

class FilterPanel(val app: App) : JPanel() {
    val appliedFilters = JList<String>(emptyArray())
    val clearButton = JButton("Clear")
    val selectedFilterPanel = JPanel()

    init {
        layout = GridBagLayout()
        val c = GridBagConstraints()
        c.weightx = 1.0
        c.weighty = 1.0
        c.gridx = 0
        c.gridy = 0
        c.fill = GridBagConstraints.BOTH
        size = Dimension(200, 600)
        app.filtersObservable.subscribe {
            val names = it?.map { it.name }?.toTypedArray() ?: emptyArray()
            appliedFilters.setListData(names)
        }
        add(appliedFilters, c)
        appliedFilters.addListSelectionListener {
            val filters = app.filtersObservable.value ?: return@addListSelectionListener
            val index = (it.source as JList<*>).selectedIndex
            if (index < 0) return@addListSelectionListener
            val filterModel = filters[index]
            app.selectedFilterPanel.value = filterModel.panel
        }
        c.gridy = 1
        clearButton.addActionListener {
            app.clearFilters()
            app.selectedFilterPanel.value = null
        }
        add(clearButton, c)
        c.gridy = 2
        add(FilterPreviewsPanel(app), c)
        c.gridy = 3
        app.selectedFilterPanel.subscribe {
            selectedFilterPanel.removeAll()
            if (it == null) {
                selectedFilterPanel.invalidate()
                selectedFilterPanel.repaint()
                invalidate()
                revalidate()
                repaint()
                return@subscribe
            }
            selectedFilterPanel.add(it)
            selectedFilterPanel.invalidate()
            selectedFilterPanel.repaint()
            invalidate()
            revalidate()
            repaint()
        }
        add(selectedFilterPanel, c)
    }
}