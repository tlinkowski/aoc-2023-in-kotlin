import kotlin.math.roundToLong
import kotlin.math.sqrt

fun main() {
    val day = "Day24"

    // MODEL
    data class Point3(val x: Long, val y: Long, val z: Long)

    data class Velocity(val dx: Long, val dy: Long, val dz: Long)

    data class Hailstone(val p: Point3, val v: Velocity)

    data class Hailstorm(val hailstones: List<Hailstone>)

    // PARSE
    fun String.parsePoint3d() = split(", ")
        .map { it.trim().toLong() }
        .let { (x, y, z) -> Point3(x, y, z) }

    fun String.parseVelocity() = split(", ")
        .map { it.trim().toLong() }
        .let { (x, y, z) -> Velocity(x, y, z) }

    fun String.parseHailstone() = split(" @ ").let { (p, v) ->
        Hailstone(p.parsePoint3d(), v.parseVelocity())
    }

    fun List<String>.parseHailstorm() = Hailstorm(map { it.parseHailstone() })

    // SOLVE
    operator fun Point3.plus(v: Velocity) = Point3(
        x + v.dx,
        y + v.dy,
        z + v.dz
    )

    data class HailstoneAspect(val p: Long, val v: Long)

    fun Hailstone.xAspect() = HailstoneAspect(p.x, v.dx)
    fun Hailstone.yAspect() = HailstoneAspect(p.y, v.dy)
    fun Hailstone.zAspect() = HailstoneAspect(p.z, v.dz)

    fun <T> List<T>.pairs() =
        indices.asSequence().flatMap { i -> (i + 1..<size).asSequence().map { j -> this[i] to this[j] } }

    fun findAllowedRockVelocities(a: HailstoneAspect, b: HailstoneAspect): Set<Long> {
        val hailV = a.v
        val distDiff = a.p - b.p
        return (-1000L..1000L).asSequence()
            .filter { rv -> rv != hailV }
            // for same speed: DistanceDifference % (RockVelocity-HailVelocity) = 0
            .filter { rv -> distDiff % (rv - hailV) == 0L }
            .toSet()
    }

    fun findRockVelocityAspects(aspects: List<HailstoneAspect>): Set<Long> {
        val rockVelocities = (-1000L..1000L).toMutableSet()

        aspects.pairs()
            .filter { (a, b) -> a.v == b.v }
            .map { (a, b) -> findAllowedRockVelocities(a, b) }
            .forEach { rockVelocities.retainAll(it) }

        return rockVelocities
    }

    operator fun Velocity.times(m: Long) = Velocity(dx * m, dy * m, dz * m)

    fun Hailstone.pointAt(t: Long) = p + v * t

    fun Velocity.opposite() = Velocity(-dx, -dy, -dz)

    operator fun Point3.minus(o: Point3) = Velocity(
        dx = x - o.x,
        dy = y - o.y,
        dz = z - o.z
    )

    fun Velocity.length() = sqrt(dx.toDouble() * dx + dy.toDouble() * dy + dz.toDouble() * dz)

    operator fun Velocity.plus(o: Velocity) = Velocity(
        dx + o.dx,
        dy + o.dy,
        dz + o.dz
    )

    operator fun Velocity.minus(o: Velocity) = Velocity(
        dx - o.dx,
        dy - o.dy,
        dz - o.dz
    )

    operator fun Point3.minus(v: Velocity) = Point3(x - v.dx, y - v.dy, z - v.dz)

    fun Hailstorm.potentialRockVelocities(): List<Velocity> {
        val xVelocities = findRockVelocityAspects(hailstones.map { it.xAspect() })
        val yVelocities = findRockVelocityAspects(hailstones.map { it.yAspect() })
        val zVelocities = findRockVelocityAspects(hailstones.map { it.zAspect() })
        return xVelocities.flatMap { dx ->
            yVelocities.flatMap { dy ->
                zVelocities.map { dz -> Velocity(dx, dy, dz) }
            }
        }
    }

    data class Match(val rv: Velocity, val a: Hailstone, val b: Hailstone, val ta: Long, val tb: Long) {
        fun startPoint() = a.pointAt(ta) - rv * ta
    }

    fun Hailstorm.startPoint(rv: Velocity): Point3? {
        val matches = hailstones.pairs()
            .filter { (a, b) -> a.v.dx == b.v.dx }
            // for same speed: DistanceDifference % (RockVelocity-HailVelocity) = 0
            .mapNotNull { (a, b) ->
                val hailV = a.v.dx
                val distDiff = b.p.x - a.p.x
                check(distDiff % (rv.dx - hailV) == 0L)
                val tDiff = distDiff / (rv.dx - hailV)
                // t1 = (a.p + rv*tDiff - b.p - b.v*tDiff) / (b.v - a.v)
                val nom = a.p.y + rv.dy * tDiff.toDouble() - b.p.y - b.v.dy * tDiff.toDouble()
                val denom = b.v.dy - a.v.dy
                val t1 = (nom / denom).roundToLong()
//                check(t1 == nom.dy / denom.dy)
//                check(t1 == nom.dz / denom.dz)

                val ta = t1
                val tb = t1 + tDiff

                val pa = a.pointAt(ta)
                val pb = b.pointAt(tb)
                val paMoved = pa + rv * tDiff
//                println(pa - rv * tDiff - pb)
                if (paMoved == pb) {
                    return@mapNotNull Match(rv, a, b, ta, tb)
                }

                val taP = t1 + tDiff
                val tbP = t1

                val paP = a.pointAt(taP)
                val pbP = b.pointAt(tbP)
                if (paP == pbP + rv * tDiff) {
                    println("Do not remove!")
                    return@mapNotNull Match(rv, a, b, taP, tbP)
                }
                null
            }
//        matches.forEach { println(it) }
        return matches.firstOrNull()?.startPoint()

//        val prevPoints = mutableListOf<Set<Point3>>()
//        prevPoints.add(hailstones.map { it.pointAt(0) }.toSet())
//        for (t in 1..100L) {
//            val points = hailstones.map { it.pointAt(t) }.toSet()
//            val filtered = prevPoints
//                .flatMap { s -> s.map { it + rv } }
//                .filter { it in points }
//            if (filtered.isNotEmpty()) println(filtered.size)
//            prevPoints.add(points)
//        }
    }

    fun part2(input: List<String>): Long {
        val hailstorm = input.parseHailstorm()
        val potentialVelocities = hailstorm.potentialRockVelocities()
        val p = potentialVelocities.firstNotNullOf {
            hailstorm.startPoint(it)
        }

        return p.x + p.y + p.z
    }

    // TESTS
//    val test1 = part1(readInput("$day/test"), testRange = 7L..27L)
//    2.let { check(test1 == it) { "Test 1: is $test1, should be $it" } }

//    val test2 = part2(readInput("$day/test"))
//    47L.let { check(test2 == it) { "Test 2: is $test2, should be $it" } }

    // RESULTS
    val input = readInput("$day/input") // 24193 is wrong
//    val part1 = part1(input, testRange = 200000000000000..400000000000000)
//    0.let { println("Part 1: $part1" + if (part1 == it) "" else " (should be $it?)") }
    val part2 = part2(input)
    println("Part 2: $part2")
}
