package sirgl.graphics.filter

import sirgl.graphics.core.Filters
import javax.swing.Icon

interface FilterModelFactory<out T : FilterModel> : Presentable {
    fun create(filters: Filters): T
    override val icon: Icon?
    override val name: String
}