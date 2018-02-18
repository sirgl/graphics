package sirgl.graphics.gist


const val stepsCount = 40
class Gist(values: DoubleArray) {
    val max = values.max() ?: throw IllegalStateException()
    val min = values.min() ?: throw IllegalStateException()
    val diff = max - min

    val step = diff / stepsCount

    val buckets =  IntArray(stepsCount)

    init {
        for (value in values) {
            var bucketIndex = Math.floor((value - min) / step).toInt()
            if (bucketIndex >= stepsCount) bucketIndex = stepsCount - 1
            buckets[bucketIndex]++
        }
    }
}