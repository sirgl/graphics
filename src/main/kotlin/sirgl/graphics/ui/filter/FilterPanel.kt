package sirgl.graphics.ui.filter

import sirgl.graphics.core.App
import sirgl.graphics.filter.filterModelFactories
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import javax.swing.JPanel


class FilterPanel(app: App) : JPanel() {

    init {
        val filtersObservable = app.filtersObservable

        val appliedFilterList = AppliedFilterList(filtersObservable)
        appliedFilterList.listObservable.subscribe {
            when (it) {
                is ClearListEvt -> app.clearFilters()
                is ListElementSelectedEvt -> app.selectedFilterPanel.value = it.model.panel
            }
        }

        val filterPreviewList = FilterPreviewList(filterModelFactories)
        filterPreviewList.selectedFilter.subscribe {
            app.addFilter(it?.create(app) ?: return@subscribe)
        }

        val currentFilterSettingPanel = CurrentFilterSettingPanel(app.selectedFilterPanel)

        layout = GridBagLayout()
        val c = GridBagConstraints()
        c.weightx = 1.0
        c.weighty = 3.0
        c.gridx = 0
        c.gridy = 0
        c.fill = GridBagConstraints.BOTH
        add(appliedFilterList, c)

        c.gridy = 1
        c.weighty = 3.0
        add(filterPreviewList, c)

        c.gridy = 2
        c.weighty = 3.0
        add(currentFilterSettingPanel, c)
    }
}