package sirgl.graphics.segmentation

import org.junit.Test
import sirgl.graphics.conversion.LAB
import sirgl.graphics.segmentation.sam.*
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
                byteArrayOf(1, 1, 15, 15, 7, 7, 7, 7),
                byteArrayOf(1, 1, 15, 15, 7, 7, 7, 7),
                byteArrayOf(2, 3, 6, 6, 7, 7, 7, 7),
                byteArrayOf(4, 5, 6, 6, 7, 7, 7, 7),
                byteArrayOf(8, 8, 9, 12, 0, 0, 0, 0),
                byteArrayOf(8, 8, 13, 14, 0, 0, 0, 0),
                byteArrayOf(10, 10, 11, 11, 0, 0, 0, 0),
                byteArrayOf(10, 10, 11, 11, 0, 0, 0, 0)
        ))
        val matrix = LabMatrix(img)
        val region = Region(img, matrix, 0.2f)
        split(region, metric)
        var currentMark = 0
        region.leafPass {
            it.area = Area(it)
            currentMark++
        }
        val target = region.leftTop.leftTop
        val neighbors = target.findNeighbors()
        val neighborNumbers = neighbors.map { img.arr[it.yStart][it.xStart] }.joinToString(", ")
        val text = neighbors.joinToString("\n") { it.coordinates().toString() }
        region.leafPass {
            for (neighbor in it.findNeighbors()) {
                it.mergeWith(neighbor)
            }
        }
        println("asd")
    }

    @Test
    fun test3() {
        val img = MonoArrayImg(arrayOf(
                byteArrayOf(1, 2),
                byteArrayOf(3, 4)
        ))
        val matrix = LabMatrix(img)
        val region = Region(img, matrix, 0.2f)
        split(region, metric)
        var currentMark = 0
        region.leafPass {
            it.area = Area(it)
            it.meanLab = it.findMeanLab()
            currentMark++
        }
        val target = region.leftTop
        val neighbors = target.findNeighbors()
        val neighborNumbers = neighbors.map { img.arr[it.yStart][it.xStart] }.joinToString(", ")
        val text = neighbors.joinToString("\n") { it.coordinates().toString() }
        var counter = 0
        region.leafPass {
            println("c: " + counter)
            counter++
            for (neighbor in it.findNeighbors()) {
                it.tryMergeWith(neighbor, metric)
            }
        }
        println("asd")
    }
}