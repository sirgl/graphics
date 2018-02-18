package sirgl.graphics.components

import sirgl.graphics.observable.Observable
import sirgl.graphics.observable.SimpleObservable
import javax.swing.JSlider

class ObservableSlider(defaultValue: Int = 50, maxValue: Int = 100) : JSlider(0, maxValue, defaultValue) {
    val observable: Observable<Int> = SimpleObservable(50)

    init {
        addChangeListener {
            observable.value = value
        }
    }
}