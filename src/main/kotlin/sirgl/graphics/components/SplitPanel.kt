package sirgl.graphics.components

import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import javax.swing.JComponent
import javax.swing.JPanel

class SplitPanel(
        left: JComponent,
        right: JComponent,
        leftWeight: Double,
        rightWeight: Double,
        init: SplitPanel.() -> Unit = {}
) : JPanel() {
    val leftConstraint = GridBagConstraints()
    val rightConstraint = GridBagConstraints()

    init {
        layout = GridBagLayout()
        leftConstraint.weightx = leftWeight
        leftConstraint.weighty = 1.0
        leftConstraint.gridx = 0
        leftConstraint.gridy = 0
        leftConstraint.fill = GridBagConstraints.BOTH

        rightConstraint.weightx = rightWeight
        rightConstraint.weighty = 1.0
        rightConstraint.gridx = 1
        rightConstraint.gridy = 0
        rightConstraint.fill = GridBagConstraints.BOTH
        init()
        add(left, leftConstraint)
        add(right, rightConstraint)
    }
}