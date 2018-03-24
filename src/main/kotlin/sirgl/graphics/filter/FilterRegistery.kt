package sirgl.graphics.filter

import sirgl.graphics.filter.gabor.GaborFilterModelFactory
import sirgl.graphics.filter.gauss.GaussFilterFactory
import sirgl.graphics.filter.grayscale.GrayscaleFilterModelFactory
import sirgl.graphics.filter.hsv.HSVFilterModelFactory
import sirgl.graphics.filter.sobel.SobelFilterModelFactory

val filterModelFactories = listOf<FilterModelFactory<*>>(
        HSVFilterModelFactory(),
        SobelFilterModelFactory(),
        GrayscaleFilterModelFactory(),
        GaussFilterFactory(),
        GaborFilterModelFactory()
)