package sirgl.graphics.segmentation

import sirgl.graphics.conversion.getRed
import java.awt.Color
import java.awt.image.BufferedImage
import java.util.*


interface ImgLike {
    val height: Int
    val width: Int

    fun getRGB(x: Int, y: Int): Int

    fun setRGB(x: Int, y: Int, rgb: Int)
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