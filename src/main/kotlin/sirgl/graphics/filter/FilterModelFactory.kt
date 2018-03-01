package sirgl.graphics.filter

import sirgl.graphics.core.App
import javax.swing.Icon

interface FilterModelFactory<out T : FilterModel> : Presentable {
    fun create(app: App): T
    override val icon: Icon?
    override val name: String
}