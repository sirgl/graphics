package sirgl.graphics.segmentation

import org.junit.Test
import sirgl.graphics.conversion.LAB
import sirgl.graphics.segmentation.norrm.slice.Grid
import sirgl.graphics.segmentation.norrm.slice.normalizedSliceSegm

class NormalizedSlice {
    private val metric = { l1: LAB, l2: LAB -> if (l1 == l2) 0.0 else 1.0 }

    @Test
    fun test1() {
        val img = MonoArrayImg(arrayOf(
                byteArrayOf(1, 1, 5, 5),
                byteArrayOf(1, 1, 5, 5),
                byteArrayOf(1, 1, 8, 8),
                byteArrayOf(9, 1, 8, 8)
        ))
        val grid = Grid(img)
        normalizedSliceSegm(img, img, metric)
        val x = 2
    }


}