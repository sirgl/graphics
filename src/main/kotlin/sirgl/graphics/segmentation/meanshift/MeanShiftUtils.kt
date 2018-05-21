package sirgl.graphics.segmentation.meanshift

import sirgl.graphics.conversion.getBlue
import sirgl.graphics.conversion.getGreen
import sirgl.graphics.conversion.getRed
import sirgl.graphics.filter.ImageFilter
import sirgl.graphics.segmentation.BufImg
import sirgl.graphics.segmentation.ImgLike
import sirgl.graphics.segmentation.deepCopy
import java.awt.Color
import java.awt.image.BufferedImage

open class MeanShift2(val delayFunc: (ImgLike) -> Unit) : ImageFilter {
    override fun transform(src: BufferedImage, res: BufferedImage): Boolean {
        val image = BufImg(deepCopy(src))
        MeanShift(30f, 5).apply(image)
        val width = image.width
        val height = image.height
        for (y in (0 until height)) {
            for (x in (0 until width)) {
                res.setRGB(x, y, image.getRGB(x, y))
            }
        }
        delayFunc(BufImg(src))
        return true
    }

}

class MeanShift(private val colorDistance: Float, private val radius: Int) {
    fun apply(src: ImgLike) {
        val width = src.width
        val height = src.height

        val pixelsF = Array(height) { Array(width) { FloatArray(3) } }

        var r: Int
        var g: Int
        var b: Int
        for (x in 0 until height) {
            for (y in 0 until width) {
                val rgb = src.getRGB(x, y)
                r = getRed(rgb)
                g = getGreen(rgb)
                b = getBlue(rgb)

                // You can use ColorConverter.RGBtoYIQ but you need to multiply the result with 255.
                // In this way its more fast because we spend less processor.
                pixelsF[x][y][0] = 0.299f * r + 0.587f * g + 0.114f * b
                pixelsF[x][y][1] = 0.5957f * r - 0.2744f * g - 0.3212f * b
                pixelsF[x][y][2] = 0.2114f * r - 0.5226f * g + 0.3111f * b
            }
        }

        var shift: Float
        var iters: Int

        for (x in 0 until height) {
            for (y in 0 until width) {
                var yc = y
                var xc = x
                var xcOld: Int
                var ycOld: Int
                var YcOld: Float
                var IcOld: Float
                var QcOld: Float
                var yiq = pixelsF[x][y]
                var Yc = yiq[0]
                var Ic = yiq[1]
                var Qc = yiq[2]

                iters = 0
                do {
                    xcOld = xc
                    ycOld = yc
                    YcOld = Yc
                    IcOld = Ic
                    QcOld = Qc

                    var mx = 0f
                    var my = 0f
                    var mY = 0f
                    var mI = 0f
                    var mQ = 0f
                    var num = 0

                    val radius2 = radius * radius
                    val colorDistance2 = colorDistance * colorDistance
                    for (rx in -radius..radius) {
                        val x2 = xc + rx
                        if (x2 >= 0 && x2 < height) {
                            for (ry in -radius..radius) {
                                val y2 = yc + ry
                                if (y2 >= 0 && y2 < width) {
                                    if (rx * rx + ry * ry <= radius2) {
                                        yiq = pixelsF[x2][y2]

                                        val Y2 = yiq[0]
                                        val I2 = yiq[1]
                                        val Q2 = yiq[2]

                                        val dY = Yc - Y2
                                        val dI = Ic - I2
                                        val dQ = Qc - Q2

                                        if (dY * dY + dI * dI + dQ * dQ <= colorDistance2) {
                                            mx += x2.toFloat()
                                            my += y2.toFloat()
                                            mY += Y2
                                            mI += I2
                                            mQ += Q2
                                            num++
                                        }
                                    }
                                }
                            }
                        }
                    }
                    val num_ = 1f / num
                    Yc = mY * num_
                    Ic = mI * num_
                    Qc = mQ * num_
                    xc = (mx * num_ + 0.5).toInt()
                    yc = (my * num_ + 0.5).toInt()
                    val dx = xc - xcOld
                    val dy = yc - ycOld
                    val dY = Yc - YcOld
                    val dI = Ic - IcOld
                    val dQ = Qc - QcOld

                    shift = (dx * dx).toFloat() + (dy * dy).toFloat() + dY * dY + dI * dI + dQ * dQ
                    iters++
                } while (shift > 3 && iters < 100)

                val r_ = (Yc + 0.9563f * Ic + 0.6210f * Qc).toInt()
                val g_ = (Yc - 0.2721f * Ic - 0.6473f * Qc).toInt()
                val b_ = (Yc - 1.1070f * Ic + 1.7046f * Qc).toInt()

                src.setRGB(x, y, Color(r_, g_, b_).rgb)
            }
        }
    }
}

class MeanShift2Filter(delayFunc: (ImgLike) -> Unit) : MeanShift2(delayFunc) {
    override fun transform(src: BufferedImage, res: BufferedImage): Boolean {
        val transform = super.transform(src, res)
        delayFunc(BufImg(src))
        return transform
    }
}
