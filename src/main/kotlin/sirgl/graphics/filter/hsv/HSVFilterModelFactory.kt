package sirgl.graphics.filter.hsv

import sirgl.graphics.core.Filters
import sirgl.graphics.filter.FilterModelFactory
import sirgl.graphics.filter.loadIcon
import javax.swing.Icon

class HSVFilterModelFactory : FilterModelFactory<HSVFilterModel> {
    override fun create(filters: Filters) = HSVFilterModel(filters, this)
    override val icon: Icon = loadIcon("hsv.png")
    override val name = "HSV"
}