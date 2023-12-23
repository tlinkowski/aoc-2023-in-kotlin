import java.math.BigInteger
import java.security.MessageDigest
import kotlin.io.path.Path
import kotlin.io.path.readLines

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

fun String.toLongs() = split(" ").filter { it.isNotBlank() }.map { it.trim().toLong() }

fun LongRange.offsetBy(offset: Long) = LongRange(
    start = start + offset,
    endInclusive = endInclusive + offset
)

operator fun LongRange.contains(other: LongRange) = first <= other.first && other.last <= last

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
}

data class GridRange(val xRange: IntRange, val yRange: IntRange) {

    operator fun contains(p: Point) = p.x in xRange && p.y in yRange

    fun pointsAtX(x: Int) = yRange.map { y -> Point(x, y) }

    fun pointsAtY(y: Int) = xRange.map { x -> Point(x, y) }

    fun allPoints() = xRange.flatMap { x -> pointsAtX(x) }
}

fun List<String>.toGridRange() = GridRange(xRange = first().indices, yRange = indices)

fun List<String>.toPointMap() = flatMapIndexed { y, line -> line.mapIndexed { x, c -> Point(x, y) to c } }.toMap()

fun <T> List<T>.toGridRange(mapper: (T) -> Point) = GridRange(
    xRange = minOf { mapper(it).x }..maxOf { mapper(it).x },
    yRange = minOf { mapper(it).y }..maxOf { mapper(it).y }
)

fun GridRange.toNiceString(mapper: (Point) -> Char) = yRange.joinToString("\n") { y ->
    xRange.map { x -> mapper(Point(x, y)) }.joinToString("")
}

fun <T, R> List<T>.zipWithNextCircular(transform: (a: T, b: T) -> R): List<R> = indices.map { i ->
    transform(this[i], this[(i + 1) % size])
}

fun <K, V> MutableMap<K, MutableList<V>>.at(key: K) = computeIfAbsent(key) { mutableListOf() }

fun <K, V> MutableMap<K, MutableSet<V>>.at(key: K) = computeIfAbsent(key) { mutableSetOf() }

class GraphInfo<T>(root: T, val next: (T) -> List<T>) {
    val cyclicItems: Set<T>
    val allItems: Set<T>

    fun hasCycles() = cyclicItems.isNotEmpty()

    init {
        val cyclic = mutableSetOf<T>()
        val all = mutableSetOf<T>()

        var items = listOf(root)
        while (items.isNotEmpty()) { // BFS
            items = items
                .filter { item -> all.add(item).also { if (!it) cyclic += item } }
                .flatMap(next)
        }

        cyclicItems = cyclic
        allItems = all
    }
}

fun IntRange.size() = last - first + 1
