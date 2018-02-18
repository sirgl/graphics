package sirgl.graphics.components

import sirgl.graphics.observable.Observable
import sirgl.graphics.observable.SimpleObservable
import javax.swing.JComboBox
import javax.swing.JLabel
import javax.swing.JPanel

class Dropdown(values: List<String>, name: String) : JPanel() {
    val observable: Observable<String> = SimpleObservable<String>(null)
    val dropdown = JComboBox(values.toTypedArray())
    val label = JLabel(name)

    init {
        add(hBox {
            add(label)
            dropdown.addActionListener {
                observable.value = dropdown.selectedItem as String
            }
            add(dropdown)
        })
    }
}