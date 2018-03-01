package sirgl.graphics.filter.hsv

import sirgl.graphics.components.ObservableSlider
import sirgl.graphics.observable.Observable
import sirgl.graphics.observable.transmitTo
import java.awt.Dimension
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import javax.swing.JLabel
import javax.swing.JPanel

class HSVFilterPanel(
        private val hSliderPosition: Observable<Int>,
        private val sSliderPosition: Observable<Int>,
        private val vSliderPosition: Observable<Int>
) : JPanel() {

    init {
        size = Dimension(200, 50)
        layout = GridBagLayout()
        val c = GridBagConstraints()
        c.gridx = 0
        c.gridy = 0
        c.weightx = 1.0
        c.weighty = 1.0
        add(slider("H", hSliderPosition), c)
        c.gridy = 1
        add(slider("S", sSliderPosition), c)
        c.gridy = 2
        add(slider("V", vSliderPosition), c)

    }

    private fun slider(name: String, destObservable: Observable<Int>): JPanel {
        val c = GridBagConstraints()
        val panel = JPanel(GridBagLayout())
        c.gridx = 0
        c.gridy = 0
        c.weightx = 1.0
        c.weighty = 1.0
        panel.add(JLabel(name))

        c.gridx = 1
        c.weighty = 5.0
        val slider = ObservableSlider()
        slider.observable.transmitTo(destObservable)
        panel.add(slider, c)
        return panel
    }
}