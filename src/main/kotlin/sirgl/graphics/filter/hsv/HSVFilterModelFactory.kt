package sirgl.graphics.filter.hsv

import sirgl.graphics.core.App
import sirgl.graphics.filter.FilterModelFactory
import javax.swing.Icon

class HSVFilterModelFactory : FilterModelFactory<HSVFilterModel> {
    override fun create(app: App) = HSVFilterModel(app, this)
    override val icon: Icon? = null // TODO
    override val name = "HSV"
}