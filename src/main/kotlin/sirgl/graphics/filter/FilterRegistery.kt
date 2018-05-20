package sirgl.graphics.filter

import sirgl.graphics.filter.gabor.GaborFilterModelFactory
import sirgl.graphics.filter.gauss.GaussFilterFactory
import sirgl.graphics.filter.grayscale.GrayscaleFilterModelFactory
import sirgl.graphics.filter.hsv.HSVFilterModelFactory
import sirgl.graphics.filter.sobel.SobelFilterModelFactory
import sirgl.graphics.segmentation.meanshift.MeanShiftModelFactory
import sirgl.graphics.segmentation.norrm.slice.NormalizedCutFilterModelFactory
import sirgl.graphics.segmentation.sam.SplitAndMergeFilterModelFactory

val filterModelFactories = listOf<FilterModelFactory<*>>(
    HSVFilterModelFactory(),
    SobelFilterModelFactory(),
    GrayscaleFilterModelFactory(),
    GaussFilterFactory(),
    GaborFilterModelFactory(),

    // Segmentation
    SplitAndMergeFilterModelFactory(),
    MeanShiftModelFactory(),
    NormalizedCutFilterModelFactory()
)