package sirgl.graphics.preview.generator

import sirgl.graphics.filter.*
import sirgl.graphics.filter.gauss.generateGaussMatrix
import sirgl.graphics.filter.grayscale.GrayscaleFilter
import sirgl.graphics.filter.hsv.HSVImageFilter
import sirgl.graphics.filter.sobel.SobelFilter
import sirgl.graphics.observable.SimpleObservable
import sirgl.graphics.observable.refresh
import java.awt.image.BufferedImage
import java.io.FileOutputStream
import java.nio.file.Path
import java.nio.file.Paths
import javax.imageio.ImageIO


fun main(args: Array<String>) {
    val hSlider = SimpleObservable(20)
    val sSlider = SimpleObservable(60)
    val vSlider = SimpleObservable(30)
    val filters = mapOf(
            "grayscale" to GrayscaleFilter(),
            "hsv" to HSVImageFilter(hSlider, sSlider, vSlider),
            "sobel" to SobelFilter(),
            "gauss" to PerChanelMatrixFilter(
                    SimpleObservable(MatrixKernelData(3, NormalizationType.Natural, generateGaussMatrix(3, 1f)))
            )
    )
    hSlider.refresh()
    sSlider.refresh()
    vSlider.refresh()
    val image = ImageIO.read(Paths.get("/Users/jetbrains/IdeaProjects/g1/src/main/resources/nanolena.png").toFile())
    generatePreviews(Paths.get("/Users/jetbrains/IdeaProjects/g1/src/main/resources"), image, filters)
}

fun generatePreviews(outputDir: Path, sourceImage: BufferedImage, filters: Map<String, ImageFilter>) {
    for ((name, filter) in filters) {
        val copy = deepCopy(sourceImage)
        filter.transform(sourceImage, copy)
        val fileOutputStream = FileOutputStream(outputDir.resolve(name + ".png").toString(), false)
        ImageIO.write(copy, "png", fileOutputStream)
    }
}

fun deepCopy(bi: BufferedImage): BufferedImage {
    val cm = bi.colorModel
    val isAlphaPremultiplied = cm.isAlphaPremultiplied
    val raster = bi.copyData(null)
    return BufferedImage(cm, raster, isAlphaPremultiplied, null)
}