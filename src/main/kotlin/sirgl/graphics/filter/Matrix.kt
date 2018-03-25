package sirgl.graphics.filter

@Suppress("NOTHING_TO_INLINE")
class Matrix(val width: Int, val height: Int, val values: FloatArray = FloatArray(width * height)) {
    init {
        val expected = width * height
        if (expected != values.size) {
            throw IllegalStateException("Bad matrix size, expected: $expected, but array size was: ${values.size}")
        }
    }

    inline fun getXY(x: Int, y: Int): Float {
        return values[x + width * y]
    }

    inline fun setXY(x: Int, y: Int, value: Float) {
        values[x + width * y] = value
    }

    inline fun forEach(action: XYValueAction) {
        for (y in (0 until height)) {
            for (x in (0 until width)) {
                val value = getXY(x, y)
                action(x, y, value)
            }
        }
    }
}

typealias XYValueAction = (Int, Int, Float) -> Unit