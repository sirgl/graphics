package sirgl.graphics.ui

import sirgl.graphics.components.vBox
import sirgl.graphics.core.App
import sirgl.graphics.filter.FilterModelFactory
import sirgl.graphics.filter.filterModelFactories
import java.awt.CardLayout
import java.awt.Dimension
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.JLabel
import javax.swing.JPanel

class FilterPreviewsPanel(app: App) : JPanel() {
    init {
        layout = CardLayout(10, 10)
        for (preview in filterModelFactories.map { FilterPreview(it, app) }) {
            add(preview)
        }
    }
}

class FilterPreview(filterModelFactory: FilterModelFactory<*>, app: App) : JPanel() {
    init {
        add(vBox {
            size = Dimension(200, 300)
            preferredSize = Dimension(200, 250)
            add(JLabel(filterModelFactory.name))
            val icon = JLabel(filterModelFactory.icon)
            icon.addMouseListener(object : MouseAdapter() {
                override fun mouseClicked(e: MouseEvent) {
                    app.addFilter(filterModelFactory.create(app))
                }
            })
            add(icon)
        })
    }
}