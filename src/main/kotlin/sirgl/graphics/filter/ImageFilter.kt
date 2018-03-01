package sirgl.graphics.filter

import java.awt.image.BufferedImage

interface ImageFilter {
    /**
     * Returns true, if res image has been changed
     */
    fun transform(src: BufferedImage, res: BufferedImage): Boolean
}