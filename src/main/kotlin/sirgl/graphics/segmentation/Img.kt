package sirgl.graphics.segmentation

import sirgl.graphics.conversion.getRed
import sirgl.graphics.segmentation.sam.rand
import java.awt.Color
import java.awt.image.BufferedImage


interface ImgLike {
    val height: Int
    val width: Int

    fun getRGB(x: Int, y: Int): Int

    fun setRGB(x: Int, y: Int, rgb: Int)
}

inline fun ImgLike.forEach(block: (Int, Int, Int) -> Unit) {
    for (y in (0 until height)) {
        for (x in (0 until width)) {
            block(y, x, getRGB(x, y))
        }
    }
}

fun BufferedImage.toImg() = BufImg(this)

class BufImg(val img: BufferedImage) : ImgLike {
    override fun setRGB(x: Int, y: Int, rgb: Int) {
        img.setRGB(x, y, rgb)
    }

    override val height = img.height
    override val width = img.width

    override fun getRGB(x: Int, y: Int): Int {
        return img.getRGB(x, y)
    }
}

/**
 * For testing purpose
 */
class MonoArrayImg(val arr: Array<ByteArray>) : ImgLike {
    override val height = arr.size
    override val width = arr[0].size

    override fun getRGB(x: Int, y: Int): Int {
        val value = arr[y][x]
        return Color(value.toInt(), value.toInt(), value.toInt()).rgb
    }

    override fun setRGB(x: Int, y: Int, rgb: Int) {
        arr[y][x] = getRed(rgb).toByte()
    }

    fun copy() : MonoArrayImg {
        val arr = arr.clone()
        for ((index, bytes) in this.arr.withIndex()) {
            arr[index] = bytes.clone()
        }
        return MonoArrayImg(arr)
    }

    override fun toString() = buildString {
        for (bytes in arr) {
            for (byte in bytes) {
                append(byte).append(" ")
            }
            append("\n")
        }
    }


}

fun randomColor() : Int {
    // Will produce only bright / light colours:
    val r = rand.nextFloat() / 2f + 0.5f
    val g = rand.nextFloat() / 2f + 0.5f
    val b = rand.nextFloat() / 2f + 0.5f
    return Color(r, g, b).rgb
}