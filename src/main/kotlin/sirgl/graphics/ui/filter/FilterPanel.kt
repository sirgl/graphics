package sirgl.graphics.ui.filter

import sirgl.graphics.core.Filters
import sirgl.graphics.filter.filterModelFactories
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import javax.swing.JPanel


class FilterPanel(filters: Filters) : JPanel() {

    init {
        val filtersObservable = filters.filtersObservable

        val appliedFilterList = AppliedFilterList(filtersObservable, filters)
        appliedFilterList.listObservable.subscribe {
            when (it) {
                is ClearListEvt -> filters.clearFilters()
                is ListElementSelectedEvt -> filters.selectedFilterPanel.value = it.model.panel
            }
        }

        val filterPreviewList = FilterPreviewList(filterModelFactories, filters)
        filterPreviewList.selectedFilter.subscribe {
            filters.addFilter(it?.create(filters) ?: return@subscribe)
        }

        val currentFilterSettingPanel = CurrentFilterSettingPanel(filters.selectedFilterPanel)

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