package sirgl.graphics.preview.generator

import sirgl.graphics.filter.ImageFilter
import sirgl.graphics.segmentation.norrm.slice.NormalizedSliceFilter
import java.awt.image.BufferedImage
import java.io.FileOutputStream
import java.nio.file.Path
import java.nio.file.Paths
import javax.imageio.ImageIO

fun main(args: Array<String>) {
    val image = ImageIO.read(Paths.get("/Users/jetbrains/a.jpg").toFile())
    val targetPath = Paths.get("/Users/jetbrains/IdeaProjects/g1/src/main/examples")
    testFilter(targetPath, image, NormalizedSliceFilter(), "normSlice")
}

fun testFilter(outputDir: Path, sourceImage: BufferedImage, filter: ImageFilter, name: String) {
    val copy = deepCopy(sourceImage)
    filter.transform(sourceImage, copy)
    val fileOutputStream = FileOutputStream(outputDir.resolve("$name.png").toString(), false)
    ImageIO.write(copy, "png", fileOutputStream)
}