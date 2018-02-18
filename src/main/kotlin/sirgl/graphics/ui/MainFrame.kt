package sirgl.graphics.ui

import sirgl.graphics.core.App
import java.awt.Dimension
import javax.imageio.ImageIO
import javax.swing.*

class MainFrame(val app: App) : JFrame() {

    init {
        add(MainPanel(app))
        createMenu()

        minimumSize = Dimension(800, 600)

        defaultCloseOperation = WindowConstants.DISPOSE_ON_CLOSE
    }

    private fun createMenu() {
        val menuBar = JMenuBar()
        val fileMenu = JMenu("File")
        val openFileItem = JMenuItem("Open")
        openFileItem.addActionListener {
            openFile()
        }
        fileMenu.add(openFileItem)
        menuBar.add(fileMenu)
        jMenuBar = menuBar
    }

    private fun openFile() {
        val fileChooser = JFileChooser()
        val returnValue = fileChooser.showOpenDialog(this)
        if (returnValue == JFileChooser.APPROVE_OPTION) {
            val file = fileChooser.selectedFile
            val image = ImageIO.read(file)
            app.imageObservable.value = image
        }
    }
}