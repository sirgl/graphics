package sirgl.graphics.filter

import java.awt.image.BufferedImage

interface ImageFilter {
    fun transform(src: BufferedImage, res: BufferedImage)
}