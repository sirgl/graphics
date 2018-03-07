package sirgl.graphics.ui

import sirgl.graphics.canvas.Canvas
import sirgl.graphics.canvas.ScrolledCanvas
import sirgl.graphics.components.SplitPanel
import sirgl.graphics.core.App
import sirgl.graphics.core.Filters
import sirgl.graphics.ui.filter.FilterPanel
import java.awt.Dimension
import java.awt.GridBagConstraints
import javax.swing.*

class MainPanel(val app: App) : JPanel() {
    init {
        layout = BoxLayout(this, BoxLayout.X_AXIS)
        app.repaintAllObservable.subscribe {
            revalidate()
            repaint()
        }
        minimumSize = Dimension(800, 600)

        val scrolledCanvas = ScrolledCanvas(app, Canvas(app))
        val settingsPanel = createSettingsPanel()
        add(SplitPanel(scrolledCanvas, settingsPanel, 2.5, 0.5) {
            rightConstraint.fill = GridBagConstraints.NONE
        })

        app.selectedRegionText.subscribe {
            it ?: return@subscribe
            val frame = JFrame()
            frame.size = Dimension(200, 50)
            val area = JTextArea(it, 1, 30)
            frame.add(area)
            frame.defaultCloseOperation = WindowConstants.DISPOSE_ON_CLOSE
            frame.isVisible = true
        }
    }

    private fun createSettingsPanel(): JComponent {
        val pane = JTabbedPane()
        pane.preferredSize = Dimension(250, 550)
        pane.addTab("General", GeneralSettingsPanel(app))
        pane.addTab("Filters", FilterPanel(app.filters as Filters))
        return pane
    }
}