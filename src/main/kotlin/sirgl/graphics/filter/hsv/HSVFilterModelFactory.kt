package sirgl.graphics.filter.hsv

import sirgl.graphics.core.App
import sirgl.graphics.filter.FilterModelFactory
import sirgl.graphics.filter.loadIcon
import javax.swing.Icon

class HSVFilterModelFactory : FilterModelFactory<HSVFilterModel> {
    override fun create(app: App) = HSVFilterModel(app, this)
    override val icon: Icon = loadIcon("hsv.png")
    override val name = "HSV"
}