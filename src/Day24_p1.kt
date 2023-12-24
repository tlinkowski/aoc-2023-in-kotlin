import kotlin.math.roundToLong
import kotlin.math.sign

fun main() {
    val day = "Day24"

    // MODEL
    data class Point3d(val x: Long, val y: Long, val z: Long)

    data class Point2d(val x: Double, val y: Double)

    data class Velocity(val dx: Int, val dy: Int, val dz: Int)

    data class XyLine(val a: Double, val b: Double)

    fun Pair<Point3d, Point3d>.toXyLine(): XyLine {
        val a = (second.y - first.y).toDouble() / (second.x - first.x)
        val b = first.y - a * first.x
        return XyLine(a, b)
    }

    fun Point3d.move(v: Velocity) = Point3d(
        x + v.dx,
        y + v.dy,
        z + v.dz
    )

    data class Hailstone(val p: Point3d, val v: Velocity) {
        val xyLine = (p to p.move(v)).toXyLine()
    }

    data class Hailstorm(val hailstones: List<Hailstone>)

    // PARSE
    fun String.parsePoint3d() = split(", ")
        .map { it.trim().toLong() }
        .let { (x, y, z) -> Point3d(x, y, z) }

    fun String.parseVelocity() = split(", ")
        .map { it.trim().toInt() }
        .let { (x, y, z) -> Velocity(x, y, z) }

    fun String.parseHailstone() = split(" @ ").let { (p, v) ->
        Hailstone(p.parsePoint3d(), v.parseVelocity())
    }

    fun List<String>.parseHailstorm() = Hailstorm(map { it.parseHailstone() })

    // SOLVE
    fun Pair<XyLine, XyLine>.intersection(): Point2d? {
        if (first.a == second.a) {
            return null
        }
        val x = (second.b - first.b) / (first.a - second.a)
        val y = first.a * x + first.b
        return Point2d(x, y)
    }

    fun Hailstone.isLater(p2: Point2d) =
        (p2.x - p.x).sign.toInt() == v.dx.sign && (p2.y - p.y).sign.toInt() == v.dy.sign

    fun Pair<Hailstone, Hailstone>.intersectionLater() = (first.xyLine to second.xyLine).intersection()
        ?.takeIf { p -> first.isLater(p) && second.isLater(p) }

    fun Hailstorm.xyCollisions(testRange: LongRange): List<Point2d> {
        return hailstones.indices
            .flatMap { i ->
                (i + 1..<hailstones.size)
                    .mapNotNull { j -> (hailstones[i] to hailstones[j]).intersectionLater() }
                    .filter { p -> p.x.roundToLong() in testRange && p.y.roundToLong() in testRange }
            }
    }

    fun part1(input: List<String>, testRange: LongRange): Int {
        val hailstorm = input.parseHailstorm()

        val xyCollisions = hailstorm.xyCollisions(testRange)
        xyCollisions.forEach { println(it) }
        return xyCollisions.size
    }

    fun part2(input: List<String>): Long {
        return 0
    }

    // TESTS
    val test1 = part1(readInput("$day/test"), testRange = 7L..27L)
    2.let { check(test1 == it) { "Test 1: is $test1, should be $it" } }

    val test2 = part2(readInput("$day/test"))
    0L.let { check(test2 == it) { "Test 2: is $test2, should be $it" } }

    // RESULTS
    val input = readInput("$day/input") // 24193 is wrong
    val part1 = part1(input, testRange = 200000000000000..400000000000000)
    0.let { println("Part 1: $part1" + if (part1 == it) "" else " (should be $it?)") }
    val part2 = part2(input)
    println("Part 2: $part2")
}
