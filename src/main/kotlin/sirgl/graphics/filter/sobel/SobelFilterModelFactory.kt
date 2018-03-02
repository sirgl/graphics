package sirgl.graphics.filter.sobel

import sirgl.graphics.core.App
import sirgl.graphics.filter.FilterModelFactory
import sirgl.graphics.filter.loadIcon

class SobelFilterModelFactory : FilterModelFactory<SobelFilterModel> {
    override fun create(app: App) = SobelFilterModel(this)
    override val name = "Sobel"
    override val icon = loadIcon("sobel.png")
}