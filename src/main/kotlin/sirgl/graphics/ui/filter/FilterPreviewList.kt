package sirgl.graphics.ui.filter

import sirgl.graphics.components.SplitPanel
import sirgl.graphics.filter.FilterModelFactory
import sirgl.graphics.observable.Observable
import sirgl.graphics.observable.SimpleObservable
import java.awt.Component
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.*


class FilterPreviewList(filterModelFactories: List<FilterModelFactory<*>>) : JPanel() {
    private val list = JList<FilterModelFactory<*>>(filterModelFactories.toTypedArray())
    val selectedFilter: Observable<FilterModelFactory<*>> = SimpleObservable<FilterModelFactory<*>>(null)

    init {
        layout = BoxLayout(this, BoxLayout.Y_AXIS)
        list.cellRenderer = object: ListCellRenderer<FilterModelFactory<*>?> {
            override fun getListCellRendererComponent(
                    list: JList<out FilterModelFactory<*>?>,
                    value: FilterModelFactory<*>?,
                    index: Int,
                    isSelected: Boolean,
                    cellHasFocus: Boolean
            ): Component {
                value ?: return JLabel()
                val label = JLabel(value.icon)
                return SplitPanel(label, JLabel(value.name), 1.0, 5.0)
            }
        }
        list.addMouseListener(object: MouseAdapter() {
            override fun mouseClicked(e: MouseEvent) {
                val index = list.locationToIndex(e.point)
                val fileModelFactory = list.model.getElementAt(index)
                selectedFilter.value = fileModelFactory
            }
        })
        add(JScrollPane(list))
    }
}