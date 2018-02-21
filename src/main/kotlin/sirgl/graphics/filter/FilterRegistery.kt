package sirgl.graphics.filter

import sirgl.graphics.filter.hsv.HSVFilterModelFactory

val filterModelFactories = listOf<FilterModelFactory<*>>(
        HSVFilterModelFactory()
)