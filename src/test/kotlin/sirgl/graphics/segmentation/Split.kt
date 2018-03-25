package sirgl.graphics.segmentation

import org.junit.Test
import sirgl.graphics.conversion.LAB
import sirgl.graphics.segmentation.sam.splitAndMerge
import kotlin.test.assertEquals

class RegionSplitTest {
    @Test
    fun test1() {
        val img = MonoArrayImg(arrayOf(
                byteArrayOf(1, 1, 5, 5),
                byteArrayOf(1, 1, 5, 5),
                byteArrayOf(1, 1, 8, 8),
                byteArrayOf(9, 1, 8, 8)
        ))
        splitAndMerge(img, img, { l1: LAB, l2: LAB -> if (l1 == l2) 0.0 else 1.0 })
        assertEquals("""-36 -36 -122 -122
-36 -36 -122 -122
-36 -36 -117 -117
-93 -36 -117 -117
""", img.toString())
    }
}