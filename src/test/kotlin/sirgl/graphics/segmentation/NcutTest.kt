package sirgl.graphics.segmentation

import org.junit.Test
import sirgl.graphics.conversion.LAB
import sirgl.graphics.segmentation.norrm.slice.normCut
import sirgl.graphics.segmentation.sam.splitAndMerge
import kotlin.test.assertEquals

class NcutTest {
    private val metric = { l1: LAB, l2: LAB -> if (l1 == l2) 0.0 else 1.0 }

    @Test
    fun test1() {
        val img = MonoArrayImg(
            arrayOf(
                byteArrayOf(1, 2),
                byteArrayOf(1, 2),
                byteArrayOf(2, 2)
            )
        )
        normCut(img, img, metric)

    }
}