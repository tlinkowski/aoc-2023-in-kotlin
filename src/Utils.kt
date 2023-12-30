import java.math.BigInteger
import java.security.MessageDigest
import kotlin.io.path.Path
import kotlin.io.path.readLines
import kotlin.math.roundToLong

/**
 * Reads lines from the given input txt file.
 */
fun readInput(name: String) = Path("src/$name.txt").readLines()

/**
 * Converts string to md5 hash.
 */
fun String.md5() = BigInteger(1, MessageDigest.getInstance("MD5").digest(toByteArray()))
    .toString(16)
    .padStart(32, '0')

fun String.toLongs(delimiter: String = " ") = split(delimiter).filter { it.isNotBlank() }.map { it.trim().toLong() }

fun LongRange.offsetBy(offset: Long) = LongRange(
    start = start + offset,
    endInclusive = endInclusive + offset
)

operator fun LongRange.contains(other: LongRange) = first <= other.first && other.last <= last

fun IntRange.size() = last - first + 1

fun IntRange.expand(times: Int) = (times * size()).let { first - it..last + it }

fun IntRange.normalize(i: Int): Int {
    var iN = i % size()
    if (iN < 0) iN += size()
    check(iN in this) { iN }
    return iN
}

fun gcd(a: Long, b: Long): Long = if (b == 0L) a else gcd(b, a % b)

fun lcm(a: Long, b: Long) = a / gcd(a, b) * b

fun <T> Sequence<T>.takeWhileInclusive(predicate: (T) -> Boolean) = sequence {
    with(iterator()) {
        while (hasNext()) {
            val next = next()
            yield(next)
            if (!predicate(next)) break
        }
    }
}

inline fun <reified E : Enum<E>> E.prev(): E = enumValues<E>().let { it[(ordinal + it.size - 1) % it.size] }

inline fun <reified E : Enum<E>> E.next(): E = enumValues<E>().let { it[(ordinal + 1) % it.size] }

enum class Dir(val dx: Int, val dy: Int, val symbol: Char) {
    UP(0, -1, '▲'),
    RIGHT(1, 0, '▶'),
    DOWN(0, 1, '▼'),
    LEFT(-1, 0, '◀');

    fun isHorizontal() = dy == 0

    fun isVertical() = dx == 0

    fun turnCW(): Dir = next()

    fun turnCCW(): Dir = prev()

    fun opposite(): Dir = next().next()
}

data class Point(val x: Int, val y: Int) {
    fun move(dir: Dir) = move(dir, 1)

    fun move(dir: Dir, n: Int) = Point(x + n * dir.dx, y + n * dir.dy)

    override fun toString() = "($x,$y)"
}

data class GridRange(val xRange: IntRange, val yRange: IntRange) {

    operator fun contains(p: Point) = p.x in xRange && p.y in yRange

    fun pointsAtX(x: Int) = yRange.map { y -> Point(x, y) }

    fun pointsAtY(y: Int) = xRange.map { x -> Point(x, y) }

    fun allPoints() = xRange.flatMap { x -> pointsAtX(x) }

    fun expand(times: Int) = GridRange(xRange = xRange.expand(times), yRange = yRange.expand(times))

    fun normalize(p: Point) = Point(x = xRange.normalize(p.x), y = yRange.normalize(p.y))
}

fun List<String>.toGridRange() = GridRange(xRange = first().indices, yRange = indices)

fun List<String>.toPointMap() = flatMapIndexed { y, line -> line.mapIndexed { x, c -> Point(x, y) to c } }.toMap()

fun <T> List<T>.toGridRange(f: (T) -> Point) = GridRange(
    xRange = minOf { f(it).x }..maxOf { f(it).x },
    yRange = minOf { f(it).y }..maxOf { f(it).y }
)

fun GridRange.toNiceString(mapper: (Point) -> Char) = yRange.joinToString("\n") { y ->
    xRange.map { x -> mapper(Point(x, y)) }.joinToString("")
}

fun <T, R> List<T>.zipWithNextCircular(transform: (a: T, b: T) -> R): List<R> = indices.map { i ->
    transform(this[i], this[(i + 1) % size])
}

fun <T> List<T>.zipWithEveryFollowing(): Sequence<Pair<T, T>> = indices.asSequence().flatMap { i ->
    (i + 1..<size).asSequence().map { j -> this[i] to this[j] }
}

fun <K, V> MutableMap<K, MutableList<V>>.at(key: K) = computeIfAbsent(key) { mutableListOf() }

fun <K, V> MutableMap<K, MutableSet<V>>.at(key: K) = computeIfAbsent(key) { mutableSetOf() }

class GraphInfo<T>(root: T, val next: (T) -> Collection<T>) {
    val cyclicItems: Set<T>
    val allItems: Set<T>
    var layerSizes: List<Int>

    fun hasCycles() = cyclicItems.isNotEmpty()

    init {
        val cyclic = mutableSetOf<T>()
        val all = mutableSetOf<T>()
        val layers = mutableListOf<Int>()

        var items = listOf(root)
        while (items.isNotEmpty()) { // BFS
            layers += items.size
            items = items
                .filter { item -> all.add(item).also { if (!it) cyclic += item } }
                .flatMap(next)
        }

        cyclicItems = cyclic
        allItems = all
        layerSizes = layers
    }
}

data class Cycle(val start: Int, val length: Int) {
    // returns progression containing specified value
    fun progression(value: Int = start): IntProgression {
        check(value >= start) { "$value below $start" }
        val offset = (value - start) % length
        return start + offset..Int.MAX_VALUE step length
    }
}

fun List<Int>.findCycle(): Cycle? {
    val diffs = zipWithNext { a, b -> b - a }
    val cycleLengths = diffs.reversed().takeWhile { it == diffs.last() }
    if (cycleLengths.size < 2) {
        return null
    }
    return Cycle(start = get(size - cycleLengths.size - 1), length = cycleLengths.first())
}

fun lagrange(points: List<Point>): (Int) -> Long {
    fun basis(x: Int, j: Int) = points.indices
        .mapNotNull { i -> if (i != j) (x - points[i].x) / (points[j].x - points[i].x).toDouble() else null }
        .reduce(Double::times)
    return { x -> points.indices.sumOf { j -> points[j].y * basis(x, j) }.roundToLong() }
}
