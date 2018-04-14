@file:Suppress("NOTHING_TO_INLINE")

package sirgl.graphics.segmentation.sam

import sirgl.graphics.conversion.LAB
import sirgl.graphics.filter.ImageFilter
import sirgl.graphics.observable.Observable
import sirgl.graphics.observable.SimpleObservable
import sirgl.graphics.segmentation.*
import sirgl.graphics.segmentation.sam.Side.*
import java.awt.image.BufferedImage
import java.util.*

class SplitAndMergeFilter(val threshold: Observable<Float> = SimpleObservable(THRESHOLD)) : ImageFilter {
    override fun transform(src: BufferedImage, res: BufferedImage): Boolean {
        val srcImg = src.toImg()
        val resImg = res.toImg()
        splitAndMerge(srcImg, resImg, threshold = threshold.value ?: return false)
        return true
    }
}

fun splitAndMerge(src: ImgLike, res: ImgLike, metricFunc: (LAB, LAB) -> Double = ::computeCiede2000Metrics, threshold: Float = THRESHOLD) {
    val matrix = LabMatrix(src)
    val region = Region(src, matrix, threshold)
    split(region, metricFunc)
    var currentMark = 0
    region.leafPass {
        it.mark = currentMark
        it.meanLab = it.findMeanLab()
        currentMark++
    }
    region.leafPass {
        val neighbors = it.findNeighbors()
        for (neighbor in neighbors) {
            it.tryMergeWith(neighbor, metricFunc)
        }
    }
    val marks = mutableSetOf<Int>()
    val markToColor = mutableMapOf<Int, Int>()
    region.leafPass {
        val areaMark = it.getAreaMark()
        if (marks.add(areaMark)) {
            markToColor[areaMark] = src.getRGB(it.xStart, it.yStart)
        }
    }
    region.leafPass {
        it.matrix.forEach(it.xStart, it.yStart, it.xEnd, it.yEnd) { x, y, _ ->
            res.setRGB(x, y, markToColor[it.getAreaMark()]!!)
        }
    }
}

private fun Region.findMeanLab(): LAB {
    val meanLab = LAB()
    var count = 0
    matrix.forEach(xStart, yStart, xEnd, yEnd) { x, y, lab ->
        meanLab.l += lab.l
        meanLab.a += lab.a
        meanLab.b += lab.b
        count++
    }
    meanLab.l /= count
    meanLab.a /= count
    meanLab.b /= count
    return meanLab
}

val rand = Random(42)



const val THRESHOLD = 0.02f


fun split(root: Region, metricFunc: (LAB, LAB) -> Double) {
    root.prePass {
        it.children = it.splitIfPossible(metricFunc) ?: emptyArray()
    }
}

class Region(
        val matrix: LabMatrix,
        val xStart: Int,
        val yStart: Int,
        val xEnd: Int,
        val yEnd: Int,
        val threshold: Float,
        val parent: Region?
) {
    constructor(img: ImgLike, matrix: LabMatrix, threshold: Float) :
            this(matrix, 0, 0, img.width, img.height, threshold, null)

    var meanLab: LAB? = null // If is leaf it is not null

    var nextRegion: Region? = null
    var mark: Int = -1

    var children: Array<Region> = emptyArray()

    fun getAreaMark(): Int {
        var current: Region = this
        while(true) {
            current = current.nextRegion ?: break
        }
//        if (current != this) {
//            nextRegion = current
//        }
        return current.mark
    }

    fun prePass(action: (Region) -> Unit) {
        action(this)
        for (child in children) {
            child.prePass(action)
        }
    }

    fun leafPass(action: (Region) -> Unit) {
        prePass {
            if (it.isLeaf()) {
                action(it)
            }
        }
    }

    fun leafs(): List<Region> {
        val regions = mutableListOf<Region>()
        leafPass {
            regions.add(it)
        }
        return regions
    }

    fun coordinates(): MutableList<Pair<Int, Int>> {
        val pairs = mutableListOf<Pair<Int, Int>>()
        matrix.forEach(xStart, yStart, xEnd, yEnd) {x, y, lab ->
            pairs.add(x to y)
        }
        return pairs
    }

    fun childCoordinatesText() = children.joinToString("\n") { it.coordinates().toString() }

    fun splitIfPossible(metricFunc: (LAB, LAB) -> Double): Array<Region>? {
        return when {
            isHomogenous(metricFunc) || xStart == xEnd - 1 || yStart == yEnd - 1 -> null
            else -> split()
        }
    }

    fun isLeaf() = children.isEmpty()

    private fun split(): Array<Region> {
        val xMid = (xStart + xEnd) / 2
        val yMid = (yStart + yEnd) / 2
        return arrayOf(
                Region(matrix, xStart, yStart, xMid, yMid, threshold, this), // Left top
                Region(matrix, xMid, yStart, xEnd, yMid, threshold, this), // Right top
                Region(matrix, xStart, yMid, xMid, yEnd, threshold, this), // Left down
                Region(matrix, xMid, yMid, xEnd, yEnd, threshold, this) // Right down
        )
    }

    private fun isHomogenous(metricFunc: (LAB, LAB) -> Double): Boolean {
        matrix.forEach(xStart, yStart, xEnd, yEnd) { _, _, lab1 ->
            matrix.forEach(xStart, yStart, xEnd, yEnd) { _, _, lab2 ->
                val metric = metricFunc(lab1, lab2)
                if (metric > threshold) return false
            }
        }
        return true
    }

    fun findNeighbors(): List<Region> {
        val neighbors = Neighbors()
        findNeighbors(neighbors)
        return neighbors.flatten()
    }

    fun tryMergeWith(neighbor: Region, metricFunc: (LAB, LAB) -> Double) {
        if (shouldMerge(neighbor, metricFunc)) {
            mergeWith(neighbor)
        }
    }

    private fun mergeWith(neighbor: Region) {
        nextRegion = neighbor
    }

    // Local merge (considering only neighbor)
    private fun shouldMerge(neighbor: Region, metricFunc: (LAB, LAB) -> Double): Boolean {
        if (getAreaMark() == neighbor.getAreaMark()) return false
//        matrix.forEach(xStart, yStart, xEnd, yEnd) { _, _, lab1 ->
//            matrix.forEach(neighbor.xStart, neighbor.yStart, neighbor.xEnd, neighbor.yEnd) { _, _, lab2 ->
//                val metric = metricFunc(lab1, lab2)
//                if (metric > threshold) return false
//            }
//        }
        return metricFunc(meanLab!!, neighbor.meanLab!!) <= threshold
//        return true
    }

    private fun findNeighbors(neighbors: Neighbors) {
        var parentNode: Region = parent ?: return
        var childNode = this
        while (true) {
            val (firstNeighborPos, secondNeighborPos) = findNeighborsInQuad(childNode, parentNode)
            val sourcePosition = childNode.findPositionInParent(parentNode)
            childNode.fillNeighborsForPosition(sourcePosition, firstNeighborPos, neighbors, parentNode)
            childNode.fillNeighborsForPosition(sourcePosition, secondNeighborPos, neighbors, parentNode)
            if (
                    (neighbors.left.isNotEmpty()) &&
                    (neighbors.right.isNotEmpty()) &&
                    (neighbors.top.isNotEmpty()) &&
                    (neighbors.down.isNotEmpty())
            ) return
            childNode = parentNode
            parentNode = parentNode.parent ?: return

        }
    }

    private fun fillNeighborsForPosition(
            sourcePosition: Int,
            neighborPos: Int,
            neighbors: Neighbors,
            parentNode: Region
    ) {
        val side = findNeighborInternalSide(sourcePosition, neighborPos)
        val neighborSideToFill = when (side.opposite()) {
            LEFT -> neighbors.left
            TOP -> neighbors.top
            RIGHT -> neighbors.right
            DOWN -> neighbors.down
        }
        val neighbor = parentNode.children[neighborPos]
        neighbor.findAllChildrenAtSide(side, neighborSideToFill)
    }

    private fun findAllChildrenAtSide(side: Side, list: MutableList<Region>) {
        if (isLeaf()) {
            list.add(this)
            return
        }
        when (side) {
            LEFT -> {
                children[LEFT_TOP].findAllChildrenAtSide(side, list)
                children[LEFT_DOWN].findAllChildrenAtSide(side, list)
            }
            TOP -> {
                children[LEFT_TOP].findAllChildrenAtSide(side, list)
                children[RIGHT_TOP].findAllChildrenAtSide(side, list)
            }
            RIGHT -> {
                children[RIGHT_TOP].findAllChildrenAtSide(side, list)
                children[RIGHT_DOWN].findAllChildrenAtSide(side, list)
            }
            DOWN -> {
                children[LEFT_DOWN].findAllChildrenAtSide(side, list)
                children[RIGHT_DOWN].findAllChildrenAtSide(side, list)
            }

        }
    }

    private fun findPositionInParent(parentRegion: Region)  = when {
        parentRegion.children[LEFT_TOP] === this -> LEFT_TOP
        parentRegion.children[RIGHT_TOP] === this -> RIGHT_TOP
        parentRegion.children[LEFT_DOWN] === this -> LEFT_DOWN
        parentRegion.children[RIGHT_DOWN] === this -> RIGHT_DOWN
        else -> throw IllegalStateException("Not a parent node")
    }

    private inline fun findNeighborsInQuad(current: Region, parent: Region)  = when {
        parent.children[LEFT_TOP] === current -> RIGHT_TOP to LEFT_DOWN
        parent.children[RIGHT_TOP] === current -> LEFT_TOP to RIGHT_DOWN
        parent.children[LEFT_DOWN] === current -> LEFT_TOP to RIGHT_DOWN
        parent.children[RIGHT_DOWN] === current -> LEFT_DOWN to RIGHT_TOP
        else -> throw IllegalStateException("Not a parent node")
    }

    fun printDebug(img: MonoArrayImg): String {
        val arr = img.arr
        return buildString {
            for (y in (yStart until yEnd)) {
                for (x in (xStart until xEnd)) {
                    append(arr[y][x]).append(" ")
                }
                append("\n")
            }
        }
    }
}

enum class Side {
    LEFT,
    TOP,
    RIGHT,
    DOWN;

    fun opposite()  = when (this) {
        LEFT -> RIGHT
        TOP -> DOWN
        RIGHT -> LEFT
        DOWN -> TOP
    }
}

private inline fun findNeighborInternalSide(sourcePos: Int, neighborPos: Int) = when (sourcePos) {
    LEFT_TOP -> when (neighborPos) {
        RIGHT_TOP -> LEFT
        LEFT_DOWN -> TOP
        else -> throw IllegalStateException("bad side")
    }
    RIGHT_TOP -> when (neighborPos) {
        LEFT_TOP -> RIGHT
        RIGHT_DOWN -> TOP
        else -> throw IllegalStateException("bad side")
    }
    LEFT_DOWN -> when (neighborPos) {
        LEFT_TOP -> DOWN
        RIGHT_DOWN -> LEFT
        else -> throw IllegalStateException("bad side")
    }
    RIGHT_DOWN -> when (neighborPos) {
        LEFT_DOWN -> RIGHT
        RIGHT_TOP -> DOWN
        else -> throw IllegalStateException("bad side")
    }
    else -> throw IllegalStateException("bad side")
}

class Area (val region: Region) {
    val members: MutableList<Region> = mutableListOf(region)
}

private const val LEFT_TOP = 0
private const val RIGHT_TOP = 1
private const val LEFT_DOWN = 2
private const val RIGHT_DOWN = 3

private class Neighbors(
        val left: MutableList<Region> = mutableListOf(),
        val right: MutableList<Region> = mutableListOf(),
        val top: MutableList<Region> = mutableListOf(),
        val down: MutableList<Region> = mutableListOf()
) {
    fun flatten(): List<Region> {
        val mutableList = mutableListOf<Region>()
        mutableList.addAll(left)
        mutableList.addAll(right)
        mutableList.addAll(top)
        mutableList.addAll(down)
        return mutableList
    }
}