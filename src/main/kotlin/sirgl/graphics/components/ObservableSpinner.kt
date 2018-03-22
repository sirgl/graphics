package sirgl.graphics.components

import sirgl.graphics.observable.SimpleObservable
import javax.swing.JSpinner
import javax.swing.SpinnerNumberModel

class ObservableSpinner(val min: Int, val max: Int, val current: Int) : JSpinner(SpinnerNumberModel(current, min, max, 1)) {
    val observable = SimpleObservable(current)

    init {
        addChangeListener {
            observable.value = value as Int
        }
    }
}