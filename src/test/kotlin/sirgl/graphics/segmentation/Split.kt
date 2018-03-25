package sirgl.graphics.segmentation

import org.junit.Test
import sirgl.graphics.conversion.LAB
import sirgl.graphics.segmentation.sam.Region
import sirgl.graphics.segmentation.sam.SplitMatrix
import sirgl.graphics.segmentation.sam.split
import sirgl.graphics.segmentation.sam.splitAndMerge
import kotlin.test.assertEquals

class RegionSplitTest {
    private val metric = { l1: LAB, l2: LAB -> if (l1 == l2) 0.0 else 1.0 }

    @Test
    fun test1() {
        val img = MonoArrayImg(arrayOf(
                byteArrayOf(1, 1, 5, 5),
                byteArrayOf(1, 1, 5, 5),
                byteArrayOf(1, 1, 8, 8),
                byteArrayOf(9, 1, 8, 8)
        ))
        splitAndMerge(img, img, metric)
        assertEquals("""-36 -36 -122 -122
-36 -36 -122 -122
-36 -36 -117 -117
-93 -36 -117 -117
""", img.toString())
    }


    @Test
    fun test2() {
        val img = MonoArrayImg(arrayOf(
                byteArrayOf(1, 1, 5, 5, 3, 3, 3, 3),
                byteArrayOf(1, 1, 5, 5, 3, 3, 3, 3),
                byteArrayOf(2, 2, 4, 6, 3, 3, 3, 3),
                byteArrayOf(2, 2, 7, 8, 3, 3, 3, 3),
                byteArrayOf(9, 9, 9, 9, 0, 0, 0, 0),
                byteArrayOf(9, 9, 9, 9, 0, 0, 0, 0),
                byteArrayOf(9, 9, 9, 9, 0, 0, 0, 0),
                byteArrayOf(9, 9, 9, 9, 0, 0, 0, 0)
        ))
        val matrix = SplitMatrix(img)
        val region = Region(img, matrix, 0.2f)
        split(region, metric)
        var currentMark = 0
        region.leafPass {
            it.mark = currentMark
            currentMark++
        }
        val target = region.children[0].children[3].children[1]
        val neighbors = target.findNeighbors()
        val text = neighbors.joinToString("\n") { it.coordinates().toString() }
        println("asd")
    }
}