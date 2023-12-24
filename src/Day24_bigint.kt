import java.math.BigInteger
import kotlin.math.roundToLong

fun main() {
    val day = "Day24"

    // MODEL
    data class Point3(val x: BigInteger, val y: BigInteger, val z: BigInteger)

    data class Velocity(val dx: BigInteger, val dy: BigInteger, val dz: BigInteger)

    data class Hailstone(val p: Point3, val v: Velocity)

    data class Hailstorm(val hailstones: List<Hailstone>)

    // PARSE
    fun String.parsePoint3d() = split(", ")
        .map { it.trim().toBigInteger() }
        .let { (x, y, z) -> Point3(x, y, z) }

    fun String.parseVelocity() = split(", ")
        .map { it.trim().toBigInteger() }
        .let { (x, y, z) -> Velocity(x, y, z) }

    fun String.parseHailstone() = split(" @ ").let { (p, v) ->
        Hailstone(p.parsePoint3d(), v.parseVelocity())
    }

    fun List<String>.parseHailstorm() = Hailstorm(map { it.parseHailstone() })

    // SOLVE
    fun Velocity.crossProduct(o: Velocity) = Velocity(
        dx = dy * o.dz - dz * o.dy,
        dy = -dx * o.dz + dz * o.dx,
        dz = dx * o.dy - dy * o.dx
    )

    operator fun Point3.plus(v: Velocity) = Point3(
        x + v.dx,
        y + v.dy,
        z + v.dz
    )

    data class Segment(
        val h1: Hailstone, val t1: BigInteger,
        val h2: Hailstone, val t2: BigInteger
    )

    operator fun Velocity.times(m: BigInteger) = Velocity(dx * m, dy * m, dz * m)

    fun Hailstone.pointAt(t: BigInteger) = p + v * t

    fun Segment.p1() = h1.pointAt(t1)

    fun Segment.p2() = h2.pointAt(t2)

    fun Velocity.dotProduct(o: Velocity) = dx * o.dx + dy * o.dy + dz * o.dz

    fun Velocity.opposite() = Velocity(-dx, -dy, -dz)

//    fun Velocity.isZero() = dx && dy == 0.0 && dz == 0.0

//    fun Velocity.normalize() =
//        if (isZero()) this else listOf(dx, dy, dz).reduce(::gcd).let { Velocity(dx / it, dy / it, dz / it) }

    operator fun Point3.minus(o: Point3) = Velocity(
        dx = x - o.x,
        dy = y - o.y,
        dz = z - o.z
    )

    fun Hailstone.shortestSegment(o: Hailstone): Segment? {
        val r1 = p
        val r2 = o.p
        val e1 = v
        val e2 = o.v
        val n = e1.crossProduct(e2)
        val nDot = n.dotProduct(n)
        val rDiff = r2 - r1
        if (nDot.signum() == 0) {
            return null
        }

        val t1 = e2.crossProduct(n).dotProduct(rDiff).divide(nDot)
        val t2 = e1.crossProduct(n).dotProduct(rDiff).divide(nDot)

        return Segment(this, t1, o, t2)
    }

//    fun part1(input: List<String>, testRange: LongRange): Int {
//        val hailstorm = input.parseHailstorm()
//
//        val xyCollisions = hailstorm.xyCollisions(testRange)
//        xyCollisions.forEach { println(it) }
//        return xyCollisions.size
//    }

    fun Hailstorm.hailstonePairs() = hailstones.indices
        .flatMap { i -> (i + 1..<hailstones.size).map { j -> hailstones[i] to hailstones[j] } }

//    fun Hailstorm.eachCross(): Set<Velocity> {
//        val uniqueVectors = hailstones
//            .map { it.v.normalize() }
//            .distinct()
//
//        return uniqueVectors.indices
//            .flatMap { i ->
//                (i + 1..<uniqueVectors.size)
//                    .map { j -> uniqueVectors[i] to uniqueVectors[j] }
//                    .map { (a, b) ->
//                        a.crossProduct(b).normalize().also {
//                            println("$a x $b => $it")
//                        }
//                    }
//            }
//            .toSet()
//    }

    fun Velocity.lengthSqrd() = dx * dx + dy * dy + dz * dz

    fun Segment.velocity() = p2() - p1()

    operator fun Point3.minus(v: Velocity) = Point3(x - v.dx, y - v.dy, z - v.dz)

    fun Segment.hailstone() = Hailstone(p1(), velocity())

    fun Hailstone.distance(o: Hailstone) = shortestSegment(o)
        ?.velocity()?.lengthSqrd()
        ?: BigInteger.valueOf(Long.MAX_VALUE)

    fun Hailstorm.startPoint(s: Segment): Point3? {
        val h = s.hailstone()
        val match = hailstones.all { h.distance(it).signum() == 0 }
        if (match) {
            println(s.velocity())
            val p = s.p1() - h.v
            println(p)
            return p
        }
        return null
    }

    fun Hailstorm.candidateSegments() = hailstonePairs()
        .mapNotNull { (a, b) -> a.shortestSegment(b) }
        .sortedBy { s -> s.velocity().lengthSqrd() }

    fun part2(input: List<String>): Long {
        val hailstorm = input.parseHailstorm()

        val p = hailstorm.candidateSegments()
            .onEach { println("C: ${it.velocity()}")}
            .firstNotNullOf { hailstorm.startPoint(it) }

        return p.x.toLong() + p.y.toLong() + p.z.toLong()
    }

    // TESTS
//    val test1 = part1(readInput("$day/test"), testRange = 7L..27L)
//    2.let { check(test1 == it) { "Test 1: is $test1, should be $it" } }

    val test2 = part2(readInput("$day/test"))
    47L.let { check(test2 == it) { "Test 2: is $test2, should be $it" } }

    // RESULTS
    val input = readInput("$day/input") // 24193 is wrong
//    val part1 = part1(input, testRange = 200000000000000..400000000000000)
//    0.let { println("Part 1: $part1" + if (part1 == it) "" else " (should be $it?)") }
    val part2 = part2(input)
    println("Part 2: $part2")
}
