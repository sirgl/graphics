package sirgl.graphics.ui

import sirgl.graphics.components.ObservableLabel
import sirgl.graphics.components.doubleLabel
import sirgl.graphics.components.vBox
import sirgl.graphics.core.App
import sirgl.graphics.observable.map
import javax.swing.JLabel
import javax.swing.JPanel

class SettingsPanel(app: App) : JPanel() {
    init {
        vBox {
            add(JLabel("RGB"))
            add(ObservableLabel(app.currentRGB.map { it?.red }))
            add(ObservableLabel(app.currentRGB.map { it?.green }))
            add(ObservableLabel(app.currentRGB.map { it?.blue }))
        }
        vBox {
            add(JLabel("HSV"))
            add(doubleLabel(app.currentHSV.map { it?.h }))
            add(doubleLabel(app.currentHSV.map { it?.s }))
            add(doubleLabel(app.currentHSV.map { it?.v }))
        }
        vBox {
            add(JLabel("LAB"))
            add(doubleLabel(app.currentLAB.map { it?.l }))
            add(doubleLabel(app.currentLAB.map { it?.a }))
            add(doubleLabel(app.currentLAB.map { it?.b }))
        }
    }
}