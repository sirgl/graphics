package sirgl.graphics.segmentation

import sirgl.graphics.conversion.LAB
import sirgl.graphics.conversion.fromRgb

@Suppress("NOTHING_TO_INLINE")
class LabMatrix(img: ImgLike) {
    val height = img.height
    val width = img.width
    val lBuffer = FloatArray(width * height)
    val aBuffer = FloatArray(width * height)
    val bBuffer = FloatArray(width * height)


    fun fillLabFromXY(x: Int, y: Int, lab: LAB) {
        val index = index(x, y)
        lab.l = lBuffer[index]
        lab.a = aBuffer[index]
        lab.b = bBuffer[index]
    }

    fun setXYLab(x: Int, y: Int, value: LAB) {
        val index = index(x, y)
        lBuffer[index] = value.l
        aBuffer[index] = value.a
        bBuffer[index] = value.b
    }

    inline fun index(x: Int, y: Int) = x + width * y

    init {
        val labBuffer = LAB()
        for (y in (0 until height)) {
            for (x in (0 until width)) {
                labBuffer.fromRgb(img.getRGB(x, y))
                setXYLab(x, y, labBuffer)
            }
        }
    }

    inline fun forEach(action: (Int, Int, LAB) -> Unit) {
        forEach(0, 0, width, height, action)
    }

    inline fun forEach(x1: Int, y1: Int, x2: Int, y2: Int, action: (Int, Int, LAB) -> Unit) {
        val labBuffer = LAB()
        for (y in (y1 until y2)) {
            for (x in (x1 until x2)) {
                fillLabFromXY(x, y, labBuffer)
                action(x, y, labBuffer)
            }
        }
    }
}