package sirgl.graphics.filter.gauss

import sirgl.graphics.components.ObservableFloatSpinner
import sirgl.graphics.components.ObservableIntSpinner
import sirgl.graphics.components.SplitPanel
import sirgl.graphics.observable.SimpleObservable
import sirgl.graphics.observable.map
import javax.swing.BoxLayout
import javax.swing.JButton
import javax.swing.JLabel
import javax.swing.JPanel

class GaussFilterPanel : JPanel() {
    val gaussDataObservable = SimpleObservable<GaussInputData>(null)

    init {
        layout = BoxLayout(this, BoxLayout.Y_AXIS)
        val sigmaSlider = ObservableFloatSpinner(0f, 5f, 1f, 0.1f)
        val sigmaPanel = SplitPanel(JLabel("sigma"), sigmaSlider, 1.0, 2.0)
        add(sigmaPanel)
        val radiusSlider = ObservableIntSpinner(1, 10, 1)
        val radiusPanel = SplitPanel(JLabel("radius"), radiusSlider, 1.0, 2.0)
        val sigmaObservable = sigmaSlider.observable.map { (it ?: return@map null) / 100f }
        val sizeObservable = radiusSlider.observable.map { (it ?: return@map null) * 2 + 1 }
        add(radiusPanel)
        val applyButton = JButton("Apply")
        applyButton.addActionListener {
            val sigma = sigmaObservable.value ?: return@addActionListener
            val size = sizeObservable.value ?: return@addActionListener
            gaussDataObservable.value = GaussInputData(size, sigma)
            println("sigma: $sigma, size: $size")
        }
        add(applyButton)
    }
}

class GaussInputData(
        val size: Int,
        val sigma: Float
)