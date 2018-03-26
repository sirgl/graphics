@file:Suppress("NOTHING_TO_INLINE")

package sirgl.graphics.segmentation.norrm.slice

import sirgl.graphics.conversion.LAB
import sirgl.graphics.filter.ImageFilter
import sirgl.graphics.segmentation.*
import java.awt.image.BufferedImage
import kotlin.math.min

class NormalizedSliceFilter(private val metricFunc: (LAB, LAB) -> Double = ::computeCiede2000Metrics) : ImageFilter {
    override fun transform(src: BufferedImage, res: BufferedImage): Boolean {
        val srcImg = src.toImg()
        val resImg = res.toImg()
        normalizedSliceSegm(srcImg, resImg, metricFunc)
        return true
    }
}

fun normalizedSliceSegm(src: ImgLike, res: ImgLike, metricFunc: (LAB, LAB) -> Double = ::computeCiede2000Metrics) {
    val grid = Grid(src)
    grid.makeEdges(metricFunc)
    grid.segment()
    val components = mutableSetOf<GridComponent>()
    for (node in grid.nodes) {
        components.add(node.component)
    }
    println("${components.size} components found")
    val componentToColor = components.associate { it to randomColor() }
    val width = grid.matrix.width
    for (component in components) {
        val color = componentToColor[component]!!
        for (node in component.regionNodes) {
            val x = node.index % width
            val y = node.index / width
            res.setRGB(x, y, color)
        }
    }
}

class Edge(
        val n1: Node,
        val n2: Node,
        val weight: Float = 0f,
        var isMst: Boolean = false
) {
    fun createsLoopIfAddToMst(): Boolean {
        return n1.edges.any { it.isMst } && n2.edges.any { it.isMst }
    }

    override fun toString() = "$weight"
}

class Node(val index: Int) {
    var edges: MutableList<Edge> = mutableListOf()
    var component = GridComponent(this)

    fun addEdge(edge: Edge) {
        edges.add(edge)
        edge.n2.edges.add(edge)
    }
}

class GridComponent(node: Node) {
    val regionNodes = mutableSetOf(node)
    val regionEdges = mutableListOf<Edge>()

    fun merge(c : GridComponent) {
        val borderEdges = findBorderEdges(this, c)
        for (node in c.regionNodes) {
            node.component = this
        }
        regionNodes.addAll(c.regionNodes)
        regionEdges.addAll(c.regionEdges)
        regionEdges.addAll(borderEdges)
    }
}

/**
 * ala D(C1, C2)
 */
fun borderPredicate(c1: GridComponent, c2: GridComponent) : Boolean {
    val dif = componentwiseDifference(c1, c2) ?: return false
    val mInt = minimalIntensityFluctuations(c1, c2) ?: return false
    return dif > mInt
}

private fun minimalIntensityFluctuations(c1: GridComponent, c2: GridComponent) : Float? {
    val i1 = internalDifference(c1) ?: return null
    val i2 = internalDifference(c2) ?: return null
//    if (i1 < 0.004 && i2 < 0.004) return 0f //
    return min(
            i1 + regulationParam(c1),
            i2 + regulationParam(c2)
    ).toFloat()
}

const val k = 3.0

private fun regulationParam(c: GridComponent): Double {
    return k / c.regionNodes.size
}

private fun internalDifference(c: GridComponent): Float? {
//    if (c.regionNodes.size == 1) return 0f
    buildMst(c.regionEdges)
    val weight = c.regionEdges.filter { it.isMst }.maxBy { it.weight }?.weight
    removeMstMarkers(c)
    return weight
}

private fun removeMstMarkers(c: GridComponent) {
    for (regionEdge in c.regionEdges) {
        regionEdge.isMst = false
    }
}

private fun componentwiseDifference(c1: GridComponent, c2: GridComponent) : Float? {
    val borderEdges = findBorderEdges(c1, c2)
    return borderEdges.minBy { it.weight }?.weight
}

private fun findBorderEdges(c1: GridComponent, c2: GridComponent): List<Edge> {
    val borderEdges = mutableListOf<Edge>()
    for (n1 in c1.regionNodes) {
        for (edge in n1.edges) {
            val n2 = edge.n2
            if (n2 in c2.regionNodes) {
                borderEdges.add(edge)
            }
        }
    }
    return borderEdges
}

private fun buildMst(edges: MutableList<Edge>) {
    edges.sortBy { it.weight }
    for (edge in edges) {
        if (!edge.createsLoopIfAddToMst()) {
            edge.isMst = true
        }
    }
}

class Grid(
        img: ImgLike
) {
    val matrix = LabMatrix(img)
    val nodes = toGrid(img)
    val edges = mutableListOf<Edge>()

    private fun toGrid(imgLike: ImgLike): List<Node> {
        val nodes = mutableListOf<Node>()
        imgLike.forEach { x, y, _ ->
            nodes.add(Node(index(imgLike.width, y, x)))
        }
        return nodes
    }

    fun makeEdges(metricFunc: (LAB, LAB) -> Double) {
        val width = matrix.width
        val height = matrix.height
        val l1 = LAB()
        val l2 = LAB()
        forEachNode { x, y, node ->
            matrix.fillLabFromXY(x, y, l1)
            val xShifted = x + 1
            if (xShifted != width) {
                val neighbor = getNode(xShifted, y)
                matrix.fillLabFromXY(xShifted, y, l2)
                val metric = metricFunc(l1, l2)
                val edge = Edge(node, neighbor, metric.toFloat())
                edges.add(edge)
                node.addEdge(edge)
            }
            val yShifted = y + 1
            if (yShifted != height) {
                val neighbor = getNode(x, yShifted)
                matrix.fillLabFromXY(x, yShifted, l2)
                val metric = metricFunc(l1, l2)
                val edge = Edge(node, neighbor, metric.toFloat())
                edges.add(edge)
                node.addEdge(edge)
            }
        }
    }

    fun segment() {
        edges.sortBy { it.weight }
        println("Edges count: ${edges.size}")
        for ((index, edge) in edges.withIndex()) {
            if (index % 100000 == 0) {
                println("Current edge: $index")
            }
            val v1 = edge.n1
            val v2 = edge.n2
            val c1 = v1.component
            val c2 = v2.component
            if (c1 != c2 && !borderPredicate(c1, c2)) {
                c1.merge(c2)
            }
        }
    }

    inline fun forEachNode(block: (Int, Int, Node) -> Unit) {
        val width = matrix.width
        val height = matrix.height
        for (y in (0 until height)) {
            for (x in (0 until width)) {
                block(x, y, getNode(x, y))
            }
        }
    }

    fun getNode(x: Int, y: Int) = nodes[index(matrix.width, y, x)]

}

private inline fun index(width: Int, y: Int, x: Int) = width * y + x

