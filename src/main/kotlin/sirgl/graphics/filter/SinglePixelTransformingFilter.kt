package sirgl.graphics.filter

import java.awt.image.BufferedImage

abstract class SinglePixelTransformingFilter : ImageFilter {
    override fun transform(src: BufferedImage, res: BufferedImage): Boolean {
        val height = src.height
        val width = src.width
        for (y in 0 until height) {
            for (x in 0 until width) {
                res.setRGB(x, y, transformPixel(src.getRGB(x, y)))
            }
        }
        return true
    }

    abstract fun transformPixel(rgb: Int): Int
}