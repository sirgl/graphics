package sirgl.graphics.ui

import sirgl.graphics.components.*
import sirgl.graphics.conversion.FormatType
import sirgl.graphics.conversion.write
import sirgl.graphics.core.App
import sirgl.graphics.observable.Observable
import sirgl.graphics.observable.map
import sirgl.graphics.observable.transmitTo
import java.awt.Dimension
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import java.io.BufferedWriter
import java.io.OutputStreamWriter
import javax.swing.*
import kotlin.concurrent.thread

class SettingsPanel(private val app: App) : JPanel() {
    private val formatTypesDropdown = Dropdown(FormatType.values().map { it.name }, "Format type")
    private val saveButton = JButton("Save")

    init {
        layout = GridBagLayout()
        val c = GridBagConstraints()
        c.weightx = 1.0
        c.weighty = 1.0
        c.gridx = 0
        c.gridy = 0
        c.fill = GridBagConstraints.BOTH

        val label = JLabel()
        preferredSize = Dimension(200, 700)
        maximumSize = Dimension(200, 700)
        add(label, c)
        app.currentPositionObservable.subscribe {
            it ?: return@subscribe
            label.text = "x = ${it.x}, y = ${it.y}"
        }

        c.gridy = 1
        add(vBox {
            preferredSize = Dimension(200, 50)
            add(JLabel("RGB"))
            add(ObservableLabel("r", app.currentRGB.map { it?.red }))
            add(ObservableLabel("g", app.currentRGB.map { it?.green }))
            add(ObservableLabel("b", app.currentRGB.map { it?.blue }))
        }, c)
        c.gridy = 2
        add(vBox {
            Dimension(200, 50)
            add(JLabel("HSV"))
            add(doubleLabel("h", app.currentHSV.map {
                it ?:  return@map null
                return@map it.h * 360.0
            }))
            add(doubleLabel("s", app.currentHSV.map {
                it ?:  return@map null
                return@map it.s * 100.0
            }))
            add(doubleLabel("v", app.currentHSV.map {
                it ?:  return@map null
                return@map it.v * 100.0
            }))
        }, c)
        c.gridy = 3
        add(vBox {
            Dimension(200, 50)
            add(JLabel("LAB"))
            add(doubleLabel("l", app.currentLAB.map { it?.l }))
            add(doubleLabel("a", app.currentLAB.map { it?.a }))
            add(doubleLabel("b", app.currentLAB.map { it?.b }))
        }, c)
        c.gridy = 4
        add(hBox {
            Dimension(200, 50)
            add(formatTypesDropdown)
            formatTypesDropdown.observable.map {
                it ?: return@map null
                return@map FormatType.valueOf(it)
            }.transmitTo(app.saveTypeObservable)
            add(saveButton)
            saveButton.addActionListener {
                saveToFile()
            }
        }, c)
        c.gridy = 5
        add(vBox {
            Dimension(200, 50)
            addSlider(app.hSliderPosition)
            addSlider(app.sSliderPosition)
            addSlider(app.vSliderPosition)
        }, c)
    }

    private fun JPanel.addSlider(destObservable: Observable<Int>) {
        val slider = ObservableSlider()
        slider.observable.transmitTo(destObservable)
        add(slider)
    }

    private fun saveToFile() {
        val fileChooser = JFileChooser()
        if (fileChooser.showDialog(this, "Save") == JFileChooser.APPROVE_OPTION) {
            val file = fileChooser.selectedFile
            val writer = BufferedWriter(OutputStreamWriter(file.outputStream()))
            val saveType = app.saveTypeObservable.value ?: return
            val img = app.imageToDrawObservable.value ?: return
            thread {
                img.write(saveType, writer)
            }
        }
    }
}