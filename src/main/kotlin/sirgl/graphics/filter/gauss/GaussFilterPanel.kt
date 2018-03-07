package sirgl.graphics.filter.gauss

import sirgl.graphics.components.ObservableSlider
import sirgl.graphics.components.SplitPanel
import sirgl.graphics.observable.SimpleObservable
import sirgl.graphics.observable.map
import sirgl.graphics.observable.transmitTo
import javax.swing.BoxLayout
import javax.swing.JLabel
import javax.swing.JPanel

class GaussFilterPanel : JPanel() {
    val gaussDataObservable = SimpleObservable<GaussInputData>(null)

    init {
        layout = BoxLayout(this, BoxLayout.Y_AXIS)
        val sigmaSlider = ObservableSlider(0, 200)
        val sigmaPanel = SplitPanel(JLabel("sigma (0..2)"), sigmaSlider, 1.0, 2.0)
        add(sigmaPanel)
        val radiusSlider = ObservableSlider(1, 10)
        val radiusPanel = SplitPanel(JLabel("radius"), radiusSlider, 1.0, 2.0)
        val sigmaObservable = sigmaSlider.observable.map { (it ?: return@map null) / 100f }
        val sizeObservable = radiusSlider.observable.map { (it ?: return@map null) * 2 + 1 }
        sigmaObservable
                .map { GaussInputData(sizeObservable.value ?: return@map null, it ?: return@map null) }
                .transmitTo(gaussDataObservable)
        sizeObservable
                .map { GaussInputData(it ?: return@map null, sigmaObservable.value ?: return@map null) }
        add(radiusPanel)
    }
}

class GaussInputData(
        val size: Int,
        val sigma: Float
)