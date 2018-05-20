package sirgl.graphics.segmentation.norrm.slice

import org.nd4j.linalg.api.buffer.DataBuffer
import org.nd4j.linalg.api.ndarray.INDArray
import org.nd4j.linalg.eigen.Eigen
import org.nd4j.linalg.factory.Nd4j
import sirgl.graphics.conversion.LAB
import sirgl.graphics.conversion.fromRgb
import sirgl.graphics.filter.ImageFilter
import sirgl.graphics.segmentation.ImgLike
import sirgl.graphics.segmentation.computeCiede2000Metrics
import sirgl.graphics.segmentation.toImg
import java.awt.image.BufferedImage
import java.util.*

class NormCut(private val metricFunc: (LAB, LAB) -> Double = ::computeCiede2000Metrics) : ImageFilter {
    override fun transform(src: BufferedImage, res: BufferedImage): Boolean {
        val srcImg = src.toImg()
        val resImg = res.toImg()
        normCut(srcImg, resImg, metricFunc)
        return true
    }
}

data class IntPair(val x: Int, val y: Int)

infix fun Int.to(y: Int) = IntPair(this, y)

private val neighborOffsets = arrayOf(
    -1 to 1,
    -1 to 0,
    -1 to -1,
    0 to 1,
    0 to -1,
    1 to -1,
    1 to 0,
    1 to 1
)

@Suppress("NOTHING_TO_INLINE")
inline fun getBigIndex(x: Int, y: Int, width: Int) = y * width + x

fun normCut(src: ImgLike, res: ImgLike, metricFunc: (LAB, LAB) -> Double = ::computeCiede2000Metrics) {
    val vertexCount = src.width * src.height
    val weightMatrix: INDArray = Nd4j.zeros(vertexCount, vertexCount)
    Nd4j.setDataType(DataBuffer.Type.FLOAT)

//    println("filling W")
    val l1 = LAB()
    val l2 = LAB()
    val height = src.height
    val width = src.width
    for (y in (0 until height)) {
        for (x in (0 until width)) {
            for ((xOffset, yOffset) in neighborOffsets) {
                val neighborX = x + xOffset
                val neighborY = y + yOffset
                if (neighborX < 0 || neighborX >= width || neighborY < 0 || neighborY >= height) continue
                val bigY = getBigIndex(neighborX, neighborY, width) // other point
                val bigX = getBigIndex(x, y, width) // our point
                if (weightMatrix.getDouble(bigY, bigX) == 0.0) {
                    l1.fromRgb(src.getRGB(x, y))
                    l2.fromRgb(src.getRGB(neighborX, neighborY))
                    val metric = Math.exp(-metricFunc(l1, l2))
                    weightMatrix.put(bigX, bigY, metric)
                    weightMatrix.put(bigY, bigX, metric)
                }
                weightMatrix.put(bigX, bigX, 1.0)
                weightMatrix.put(bigY, bigY, 1.0)
            }
        }
    }
//    println(weightMatrix.toString())


    val indices = IntArray(vertexCount)
    for (i in (0 until vertexCount)) {
        indices[i] = i
    }

//    println("Filling diag")
    val diagMatrix: INDArray = Nd4j.zeros(vertexCount, vertexCount)
    for (y in (0 until vertexCount)) {
        var rowAccumulator = 0.0
        for (x in (0 until vertexCount)) {
            rowAccumulator += weightMatrix.getDouble(y, x)
        }
        diagMatrix.put(y, y, rowAccumulator)
    }
//    println(diagMatrix.toString())


//    println("Partitioning")

    val stack: Deque<PartitionInfo> = ArrayDeque<PartitionInfo>()
    stack.push(PartitionInfo(weightMatrix, diagMatrix, indices))
    val areas = mutableListOf<IntArray>()
    while (stack.isNotEmpty()) {
        val partitionInfo = stack.pop()
        if (isHomogenous(partitionInfo.indices, src, 2.0, metricFunc)) {
            areas.add(partitionInfo.indices)
            continue
        }
        val partition = partition(partitionInfo.weightMatrix, partitionInfo.diagMatrix, partitionInfo.indices)
        val weights = partitionInfo.weightMatrix.dup()
        val diags = partitionInfo.diagMatrix.dup()
        stack.push(PartitionInfo(weights, diags, partition.firstPartitionIndices))
        stack.push(PartitionInfo(weights, diags, partition.secondPartitionIndices))
    }

    for (area in areas) {
        val color = src.getRGB(area[0] / width, area[0] % width)
        for (index in area) {
            val y = index / width
            val x = index % width
            res.setRGB(x, y, color)
        }
    }
}

class PartitionInfo(
    val weightMatrix: INDArray,
    val diagMatrix: INDArray,
    val indices: IntArray
)

fun isHomogenous(
    indexArr: IntArray,
    src: ImgLike,
    threshold: Double = 2.0,
    metricFunc: (LAB, LAB) -> Double = ::computeCiede2000Metrics
): Boolean {
    val width = src.width
    val l1 = LAB()
    val l2 = LAB()
    for (index in indexArr) {
        val y1 = index / width
        val x1 = index % width
        for (index2 in indexArr) {
            val y2 = index2 / width
            val x2 = index2 % width
            l1.fromRgb(src.getRGB(x1, y1))
            l2.fromRgb(src.getRGB(x2, y2))
            if (metricFunc(l1, l2) > threshold) return false
        }
    }
    return true
}

//class

class PartitionIndices(
    val firstPartitionIndices: IntArray,
    val secondPartitionIndices: IntArray
)

fun partition(weightMatrix: INDArray, diagMatrix: INDArray, indexArray: IntArray): PartitionIndices {
    val diagMinusWeight = diagMatrix.sub(weightMatrix)
//    println(diagMinusWeight.toString())
    val eigenvalues = Eigen.symmetricGeneralizedEigenvalues(diagMinusWeight, diagMatrix, true)
//    println(diagMinusWeight.toString())
    val doubles = eigenvalues.array
    val sortedArray = doubles.sortedArray()
    val eigenvalue = sortedArray[1]
    val indexOfSecondSmallestEigenvalue = doubles.indexOf(eigenvalue)
    if (indexOfSecondSmallestEigenvalue == -1) throw IllegalStateException()
    @Suppress("UnnecessaryVariable")
    val eigenvectors = diagMinusWeight // symmetricGeneralizedEigenvalues put vectors in columns of first arg
    val targetEigenvector = eigenvectors.getColumn(indexOfSecondSmallestEigenvalue)
    val partitionArray = targetEigenvector.array
    val median = partitionArray.average()
//    println(median)
    val firstPartitionSize = partitionArray.count { it > median }
    val secondPartitionSize = partitionArray.size - firstPartitionSize
    val firstPartition = IntArray(firstPartitionSize)
    val secondPartition = IntArray(secondPartitionSize)
    var firstPartitionIndex = 0
    var secondPartitionIndex = 0
    for ((index, value) in partitionArray.withIndex()) {
        if (value > median) {
            firstPartition[firstPartitionIndex] =
                    indexArray[index] // indexArray[index] - to translate to original array
            firstPartitionIndex++
        } else {
            secondPartition[secondPartitionIndex] = indexArray[index]
            secondPartitionIndex++
        }
    }
    return PartitionIndices(firstPartition, secondPartition)
}

private val INDArray.array get() = data().asDouble() as DoubleArray