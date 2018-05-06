package sirgl.graphics.preview.generator

import sirgl.graphics.filter.ImageFilter
import sirgl.graphics.filter.Matrix
import sirgl.graphics.filter.MatrixKernelInfo
import sirgl.graphics.filter.PerChanelMatrixFilter
import sirgl.graphics.filter.gabor.GaborFilter
import sirgl.graphics.filter.gabor.generateGaborKernel
import sirgl.graphics.filter.gauss.generateGaussMatrix
import sirgl.graphics.filter.grayscale.GrayscaleFilter
import sirgl.graphics.filter.hsv.HSVImageFilter
import sirgl.graphics.filter.sobel.SobelFilter
import sirgl.graphics.observable.SimpleObservable
import sirgl.graphics.observable.refresh
import sirgl.graphics.segmentation.meanshift.MeanShift2
import sirgl.graphics.segmentation.meanshift.MeanShiftFilter
import sirgl.graphics.segmentation.norrm.slice.NormalizedSliceFilter
import sirgl.graphics.segmentation.sam.SplitAndMergeFilter
import java.awt.Color
import java.awt.image.BufferedImage
import java.io.FileOutputStream
import java.nio.file.Path
import java.nio.file.Paths
import javax.imageio.ImageIO


fun main(args: Array<String>) {
    val hSlider = SimpleObservable(20)
    val sSlider = SimpleObservable(60)
    val vSlider = SimpleObservable(30)
    val filters: Map<String, ImageFilter> = mapOf(
            "grayscale" to GrayscaleFilter(),
            "hsv" to HSVImageFilter(hSlider, sSlider, vSlider),
            "sobel" to SobelFilter(),
            "gauss" to PerChanelMatrixFilter(
                    SimpleObservable(MatrixKernelInfo(generateGaussMatrix(7, 1f)))
            ),
            "gabor" to GaborFilter(
                    SimpleObservable(MatrixKernelInfo(generateGaborKernel(5, 0f)))
            ),
            "splitAndMerge" to SplitAndMergeFilter(SimpleObservable(3.0f)),
//            "normalizedSlice" to NormalizedSliceFilter(),
//            "meanShift" to MeanShiftFilter()
            "meanShift" to MeanShift2()
    )
    hSlider.refresh()
    sSlider.refresh()
    vSlider.refresh()
    val image = ImageIO.read(Paths.get("/Users/jetbrains/IdeaProjects/g1/src/main/resources/nanolena.png").toFile())
    val targetPath = Paths.get("/Users/jetbrains/IdeaProjects/g1/src/main/resources")

    val matrix = showMatrix(generateGaborKernel(15, 90f, lambda = 6.0f ))
//    generateMultiGauss(image, filters["gauss"]!!, targetPath)
    generatePreviews(targetPath, image, filters)
}

private fun generateMultiGauss(image: BufferedImage, filter: ImageFilter, targetPath: Path) {
    var srcCopy = deepCopy(image)
    var resCopy = deepCopy(image)
    for (i in (0..20)) {
        filter.transform(srcCopy, resCopy)
        srcCopy = resCopy
    }
    val name = "multigauss"
    val fileOutputStream = FileOutputStream(targetPath.resolve("$name.png").toString(), false)
    ImageIO.write(resCopy, "png", fileOutputStream)
}

fun generatePreviews(outputDir: Path, sourceImage: BufferedImage, filters: Map<String, ImageFilter>) {
    for ((name, filter) in filters) {
        val copy = deepCopy(sourceImage)
        filter.transform(sourceImage, copy)
        val fileOutputStream = FileOutputStream(outputDir.resolve("$name.png").toString(), false)
        ImageIO.write(copy, "png", fileOutputStream)
    }
}

fun deepCopy(bi: BufferedImage): BufferedImage {
    val cm = bi.colorModel
    val isAlphaPremultiplied = cm.isAlphaPremultiplied
    val raster = bi.copyData(null)
    return BufferedImage(cm, raster, isAlphaPremultiplied, null)
}

private fun showMatrix(matrix: Matrix = generateGaussMatrix(27, 2.0f)): BufferedImage {
    val min = matrix.values.min()!!
    val max = matrix.values.max()!! - min
    val size = matrix.width
    val img = BufferedImage(size, size, BufferedImage.TYPE_INT_RGB)
    for (y in (0 until size)) {
        for (x in (0 until size)) {
            val value = matrix.getXY(x, y) - min
            val brightness = (value / max * 255.0f).toInt()
            img.setRGB(x, y, Color(brightness, brightness, brightness).rgb)
        }
    }
    return img
}
