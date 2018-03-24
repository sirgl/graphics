package sirgl.graphics.filter.gabor

import sirgl.graphics.components.ObservableFloatSpinner
import sirgl.graphics.components.ObservableIntSpinner
import sirgl.graphics.components.SplitPanel
import sirgl.graphics.observable.SimpleObservable
import java.awt.Dimension
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import javax.swing.JButton
import javax.swing.JLabel
import javax.swing.JPanel

class GaborFilterPanel() : JPanel() {
    val gaborInfoObservable = SimpleObservable(GaborInfo(1, 45f))
    init {
        size = Dimension(200, 50)
        layout = GridBagLayout()
        val c = GridBagConstraints()
        c.gridx = 0
        c.gridy = 0
        c.weightx = 1.0
        c.weighty = 1.0
        val thetaSpinner = ObservableFloatSpinner(0f, 180f, 45f, 45f)
        add(SplitPanel(JLabel("theta"), thetaSpinner, 1.0, 2.0), c)
        c.gridy++
        val radiusSpinner = ObservableIntSpinner(1, 10, 1)
        add(SplitPanel(JLabel("radius"), radiusSpinner, 1.0, 2.0), c)
        c.gridy++
        val applyButton = JButton("Apply")
        applyButton.addActionListener {
            val radius = radiusSpinner.observable.value ?: return@addActionListener
            val theta = thetaSpinner.observable.value ?: return@addActionListener
            gaborInfoObservable.value = GaborInfo(radius, theta)
        }
        add(applyButton)
    }
}