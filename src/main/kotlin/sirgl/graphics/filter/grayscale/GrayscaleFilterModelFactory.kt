package sirgl.graphics.filter.grayscale

import sirgl.graphics.core.App
import sirgl.graphics.filter.FilterModelFactory
import sirgl.graphics.filter.loadIcon
import javax.swing.Icon

class GrayscaleFilterModelFactory : FilterModelFactory<GrayscaleFilterModel> {
    override fun create(app: App) = GrayscaleFilterModel(this)

    override val icon: Icon? = loadIcon("grayscale.png")
    override val name = "Grayscale"

}