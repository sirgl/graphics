package sirgl.graphics.components

import java.awt.GridBagConstraints
import java.awt.GridBagConstraints.BOTH
import java.awt.GridBagLayout
import javax.swing.JComponent
import javax.swing.JPanel

class SplitPanel(left: JComponent, right: JComponent, leftWeight: Double, rightWeight: Double) : JPanel() {
    init {
        layout = GridBagLayout()
        val constraints = GridBagConstraints()
        constraints.weightx = leftWeight
        constraints.weighty = 1.0
        constraints.gridx = 0
        constraints.gridy = 0
        constraints.fill = BOTH
        add(left, constraints)
        constraints.weightx = rightWeight
        constraints.weighty = 1.0
        constraints.gridx = 1
        constraints.gridy = 0
        constraints.fill = BOTH
        add(right, constraints)
    }
}