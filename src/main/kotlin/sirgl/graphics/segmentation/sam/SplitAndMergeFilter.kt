@file:Suppress("NOTHING_TO_INLINE")

package sirgl.graphics.segmentation.sam

import sirgl.graphics.conversion.LAB
import sirgl.graphics.filter.ImageFilter
import sirgl.graphics.observable.Observable
import sirgl.graphics.observable.SimpleObservable
import sirgl.graphics.segmentation.*
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
        currentMark++
    }
    region.leafPass {
        val neighbors = it.findNeighbors()
        for (neighbor in neighbors) {
            it.tryMergeWith(neighbor, metricFunc)
        }
    }
    val marks = mutableSetOf<Int>()
    region.leafPass {
        marks.add(it.getAreaMark())
    }
    val markToColor = marks.associate { it to randomColor() }
    region.leafPass {
        it.matrix.forEach(it.xStart, it.yStart, it.xEnd, it.yEnd) { x, y, _ ->
            res.setRGB(x, y, markToColor[it.getAreaMark()]!!)
        }
    }
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
        matrix.forEach(xStart, yStart, xEnd, yEnd) { _, _, lab1 ->
            matrix.forEach(neighbor.xStart, neighbor.yStart, neighbor.xEnd, neighbor.yEnd) { _, _, lab2 ->
                val metric = metricFunc(lab1, lab2)
                if (metric > threshold) return false
            }
        }
        return true
    }

    private fun findNeighbors(neighbors: Neighbors) {
        var node: Region = parent ?: return
        val height = matrix.height
        val width = matrix.width
        var current = this
        while (true) {
            when {
                node.children[0] === current -> {
                    if (neighbors.right.isEmpty()) {
                        node.children[1].collectLeftSideChildren(neighbors.right)
                    }
                    if (neighbors.down.isEmpty()) {
                        node.children[2].collectTopSideChildren(neighbors.down)
                    }
                }
                node.children[3] === current -> {
                    if (neighbors.top.isEmpty()) {
                        node.children[1].collectDownSideChildren(neighbors.top)
                    }
                    if (neighbors.left.isEmpty()) {
                        node.children[2].collectRightSideChildren(neighbors.left)
                    }
                }
                node.children[1] === current -> {
                    if (neighbors.left.isEmpty()) {
                        node.children[0].collectRightSideChildren(neighbors.left)
                    }
                    if (neighbors.down.isEmpty()) {
                        node.children[3].collectTopSideChildren(neighbors.down)
                    }
                }
                node.children[2] === current -> {
                    if (neighbors.top.isEmpty()) {
                        node.children[0].collectDownSideChildren(neighbors.top)
                    }
                    if (neighbors.right.isEmpty()) {
                        node.children[3].collectLeftSideChildren(neighbors.right)
                    }
                }
            }
            if ((node.xStart == 0 || neighbors.left.isNotEmpty()) &&
                    (node.yStart == 0 || neighbors.top.isNotEmpty()) &&
                    (node.xEnd == width || neighbors.right.isNotEmpty()) &&
                    (node.yEnd == height || neighbors.down.isNotEmpty())
            ) return
            current = node
            node = node.parent ?: return
        }
    }

    private fun collectLeftSideChildren(list: MutableSet<Region>) {
        collectInner(list) {
            children[0].collectLeftSideChildren(list)
        }
        collectInner(list) {
            children[2].collectLeftSideChildren(list)
        }
    }

    private fun collectRightSideChildren(list: MutableSet<Region>) {
        collectInner(list) {
            children[1].collectRightSideChildren(list)
        }
        collectInner(list) {
            children[3].collectRightSideChildren(list)
        }
    }

    private fun collectTopSideChildren(list: MutableSet<Region>) {
        collectInner(list) {
            children[0].collectTopSideChildren(list)
        }
        collectInner(list) {
            children[1].collectTopSideChildren(list)
        }
    }

    private fun collectDownSideChildren(list: MutableSet<Region>) {
        collectInner(list) {
            children[2].collectDownSideChildren(list)
        }
        collectInner(list) {
            children[3].collectDownSideChildren(list)
        }
    }

    private inline fun collectInner(list: MutableSet<Region>, block: Region.() -> Unit) {
        if (isLeaf()) {
            list.add(this)
        } else {
            block()
        }
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

private class Area(region: Region) {
    val regions = mutableSetOf(region)
}

private class Neighbors(
        val left: MutableSet<Region> = mutableSetOf(),
        val right: MutableSet<Region> = mutableSetOf(),
        val top: MutableSet<Region> = mutableSetOf(),
        val down: MutableSet<Region> = mutableSetOf()
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