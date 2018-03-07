package sirgl.graphics.ui.filter

import sirgl.graphics.components.vBox
import sirgl.graphics.filter.FilterModel
import sirgl.graphics.observable.Observable
import sirgl.graphics.observable.SimpleObservable
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import javax.swing.JButton
import javax.swing.JList
import javax.swing.JPanel
import javax.swing.JScrollPane

sealed class ListEvt

class ClearListEvt : ListEvt()
class ListElementSelectedEvt(val model: FilterModel) : ListEvt()

class AppliedFilterList(filtersList: Observable<MutableList<FilterModel>>) : JPanel() {
    val listObservable: Observable<ListEvt> = SimpleObservable<ListEvt>(null)
    val list = JList<String>()
    val clearButton = JButton("Clear")

    init {
        layout = GridBagLayout()
        val c = GridBagConstraints()
        c.weightx = 1.0
        c.weighty = 8.0
        c.gridx = 0
        c.gridy = 0
        c.fill = GridBagConstraints.BOTH

        filtersList.subscribe {
            val names = it?.map { it.name }?.toTypedArray() ?: emptyArray()
            list.setListData(names)
        }

        list.addListSelectionListener {
            val selectionModel = it.source as JList<*>
            val index = selectionModel.minSelectionIndex
            if (index < 0) return@addListSelectionListener
            val filterList = filtersList.value ?: return@addListSelectionListener
            val filter = filterList[index]
            listObservable.value = ListElementSelectedEvt(filter)
        }

        add(JScrollPane(list), c)

        c.weighty = 1.0
        c.gridy = 1
        clearButton.addActionListener {
            listObservable.value = ClearListEvt()
        }
        add(vBox { add(clearButton) }, c)
    }
}