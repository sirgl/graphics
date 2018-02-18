package sirgl.graphics.components

import sirgl.graphics.observable.Observable
import javax.swing.JLabel

class ObservableLabel<T>(name: String, observable: Observable<T>, toString: (T) -> String = { it.toString() }) : JLabel(" ") {
    init {
        observable.subscribe {
            it ?: return@subscribe
            text = "$name = ${toString(it)}"
        }
    }
}

fun doubleLabel(name: String, observable: Observable<Double>): ObservableLabel<Double> {
    return ObservableLabel(name, observable) { String.format("%.2f", it) }
}