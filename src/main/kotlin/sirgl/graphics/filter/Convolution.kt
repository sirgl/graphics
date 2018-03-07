@file:Suppress("NOTHING_TO_INLINE")

package sirgl.graphics.filter

import sirgl.graphics.conversion.getBlue
import sirgl.graphics.conversion.getGreen
import sirgl.graphics.conversion.getRed
import java.awt.image.BufferedImage

/**
 * Expects, that [x] and [y] are far enough from bounds
 */
inline fun convolveChanel(
        matrix: FloatArray,
        kernelSize: Int,
        img: BufferedImage,
        x: Int,
        y: Int,
        chanelType: ChanelType
): Float {
    val radius = kernelSize / 2
    var sum = 0f
    for (currentY in (y - radius..y + radius)) {
        for (currentX in (x - radius..x + radius)) {
            val rgb = img.getRGB(currentX, currentY)
            val chanelValue = selectChanel(chanelType, rgb)
            val matrixX = currentX - x + radius
            val matrixY = currentY - y + radius
            val kernelVal = matrix.getXY(matrixX, matrixY, kernelSize)
            sum += kernelVal * chanelValue
        }
    }
    return sum
}

inline fun selectChanel(chanelType: ChanelType, rgb: Int) = when (chanelType) {
    ChanelType.R -> getRed(rgb)
    ChanelType.G -> getGreen(rgb)
    ChanelType.B -> getBlue(rgb)
}

enum class ChanelType {
    R,
    G,
    B
}
