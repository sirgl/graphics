package sirgl.graphics.components

import sirgl.graphics.observable.Observable
import javax.swing.JLabel

class ObservableLabel<T>(observable: Observable<T>, toString: (T) -> String = { it.toString() }) : JLabel() {
    init {
        observable.subscribe {
            it ?: return@subscribe
            text = toString(it)
        }
    }
}

fun doubleLabel(observable: Observable<Double>): ObservableLabel<Double> {
    return ObservableLabel(observable) { String.format("%.2f", it) }
}