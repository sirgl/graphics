package sirgl.graphics.filter.hsv

import sirgl.graphics.components.ObservableSlider
import sirgl.graphics.observable.Observable
import sirgl.graphics.observable.transmitTo
import java.awt.Dimension
import java.awt.GridBagConstraints
import javax.swing.JLabel
import javax.swing.JPanel

class HSVFilterPanel : JPanel() {
    private lateinit var hSliderPosition: Observable<Int>
    private lateinit var sSliderPosition: Observable<Int>
    private lateinit var vSliderPosition: Observable<Int>

    init {
        size = Dimension(200, 50)
        addSlider("H", hSliderPosition)
        addSlider("S", sSliderPosition)
        addSlider("V", vSliderPosition)
    }

    private fun JPanel.addSlider(name: String, destObservable: Observable<Int>) {
        val c = GridBagConstraints()
        c.gridx = 0
        c.gridy = 0
        c.weightx = 1.0
        c.weighty = 1.0
        add(JLabel(name))

        c.gridx = 1
        c.weighty = 5.0
        val slider = ObservableSlider()
        slider.observable.transmitTo(destObservable)
        add(slider, c)
    }
}