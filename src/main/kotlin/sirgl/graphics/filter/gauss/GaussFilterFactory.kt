package sirgl.graphics.filter.gauss

import sirgl.graphics.core.App
import sirgl.graphics.filter.FilterModelFactory
import sirgl.graphics.filter.loadIcon

class GaussFilterFactory : FilterModelFactory<GaussFilterModel> {
    override fun create(app: App) = GaussFilterModel(this, app)

    override val icon = loadIcon("gauss.png")
    override val name = "Gauss"

}