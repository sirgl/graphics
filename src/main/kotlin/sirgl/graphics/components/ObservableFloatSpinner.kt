package sirgl.graphics.components

import sirgl.graphics.observable.SimpleObservable
import javax.swing.JSpinner
import javax.swing.SpinnerNumberModel

class ObservableFloatSpinner(min: Float, max: Float, current: Float, step: Float) : JSpinner(SpinnerNumberModel(
        current.toDouble(),
        min.toDouble(),
        max.toDouble(),
        step.toDouble()
)) {
    val observable = SimpleObservable(current)

    init {
        addChangeListener {
            observable.value = (value as Double).toFloat()
        }
    }
}