package sirgl.graphics.segmentation.sam

import sirgl.graphics.components.ObservableFloatSpinner
import sirgl.graphics.components.ObservableIntSpinner
import sirgl.graphics.components.SplitPanel
import sirgl.graphics.filter.gabor.GaborInfo
import sirgl.graphics.observable.SimpleObservable
import java.awt.Dimension
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import javax.swing.JButton
import javax.swing.JLabel
import javax.swing.JPanel

class SplitAndMergeFilterPanel : JPanel() {
    val thresholdObservable = SimpleObservable(2f)

    init {
        size = Dimension(200, 50)
        layout = GridBagLayout()
        val c = GridBagConstraints()
        c.gridx = 0
        c.gridy = 0
        c.weightx = 1.0
        c.weighty = 1.0
        val thresholdSpinner = ObservableFloatSpinner(1f, 20f, 2f, 0.5f)
        add(SplitPanel(JLabel("threshold"), thresholdSpinner, 1.0, 2.0), c)
        c.gridy++
        val applyButton = JButton("Apply")
        applyButton.addActionListener {
            val threshold = thresholdSpinner.observable.value ?: return@addActionListener
            thresholdObservable.value = threshold
        }
        add(applyButton)
    }
}