import kotlin.math.roundToLong
import kotlin.math.sign

enum class Axis { X, Y, Z }

fun main() {
    val day = "Day24"

    // MODEL
    data class Point3(val x: Long, val y: Long, val z: Long)

    data class Velocity3(val dx: Long, val dy: Long, val dz: Long)

    data class Hailstone(val point: Point3, val velocity: Velocity3)

    data class Hailstorm(val hailstones: List<Hailstone>)

    // - part 1
    data class XyPoint(val x: Double, val y: Double)

    data class XyLine(val a: Double, val b: Double)

    // - part 2
    data class HailstoneMatch(val rockVelocity: Velocity3, val hailstone: Hailstone, val time: Long)

    // PARSE
    fun String.parsePoint3() = toLongs(", ")
        .let { (x, y, z) -> Point3(x, y, z) }

    fun String.parseVelocity3() = toLongs(", ")
        .let { (x, y, z) -> Velocity3(x, y, z) }

    fun String.parseHailstone() = split(" @ ").let { (p, v) ->
        Hailstone(p.parsePoint3(), v.parseVelocity3())
    }

    fun List<String>.parseHailstorm() = Hailstorm(map { it.parseHailstone() })

    // SOLVE
    operator fun Point3.plus(velocity: Velocity3) = Point3(
        x = x + velocity.dx,
        y = y + velocity.dy,
        z = z + velocity.dz
    )

    // - part 1
    fun xyLineFor(x1: Double, y1: Double, x2: Double, y2: Double): XyLine {
        // y1 = a*x1 + b
        // y2 = a*x2 + b
        // --> y1 - a*x1 = y2 - a*x2
        val a = (y2 - y1) / (x2 - x1)
        val b = y1 - a * x1
        return XyLine(a, b)
    }

    fun xyLineFor(point1: XyPoint, point2: XyPoint): XyLine = xyLineFor(
        x1 = point1.x, y1 = point1.y, x2 = point2.x, y2 = point2.y
    )

    fun xyIntersectionFor(a1: Double, b1: Double, a2: Double, b2: Double): XyPoint? {
        if (a1 == a2) {
            return null // no intersection
        }
        // y = a1*x + b1
        // y = a2*x + b2
        // --> a1*x + b1 = a2*x + b2
        val x = (b2 - b1) / (a1 - a2)
        val y = a1 * x + b1
        return XyPoint(x, y)
    }

    fun xyIntersectionFor(line1: XyLine, line2: XyLine): XyPoint? = xyIntersectionFor(
        a1 = line1.a, b1 = line1.b, a2 = line2.a, b2 = line2.b
    )

    fun Point3.toXyPoint() = XyPoint(x.toDouble(), y.toDouble())

    fun Hailstone.toXyLine() = xyLineFor(point.toXyPoint(), (point + velocity).toXyPoint())

    fun Long.hasSameSign(o: Double) = sign == o.sign.toInt()

    fun Hailstone.isGoingToward(xyPoint: XyPoint) = velocity.dx.hasSameSign(xyPoint.x - point.x)
            && velocity.dy.hasSameSign(xyPoint.y - point.y)

    fun xyCollision(hail1: Hailstone, hail2: Hailstone): XyPoint? =
        xyIntersectionFor(hail1.toXyLine(), hail2.toXyLine())
            ?.takeIf { point -> hail1.isGoingToward(point) && hail2.isGoingToward(point) }

    fun Hailstorm.xyCollisions(): Sequence<XyPoint> = hailstones.zipWithEveryFollowing()
        .mapNotNull { xyCollision(it.first, it.second) }

    operator fun LongRange.contains(o: Double) = o.roundToLong() in this

    // - part 2
    operator fun Velocity3.times(factor: Long) = Velocity3(
        dx = dx * factor,
        dy = dy * factor,
        dz = dz * factor
    )

    operator fun Point3.minus(velocity: Velocity3) = plus(velocity * -1)

    operator fun Point3.get(axis: Axis) = when (axis) {
        Axis.X -> x
        Axis.Y -> y
        Axis.Z -> z
    }

    operator fun Velocity3.get(axis: Axis) = when (axis) {
        Axis.X -> dx
        Axis.Y -> dy
        Axis.Z -> dz
    }

    class RockVelocityFinder {

        val rockVelocityRange = -500L..500L

        fun canCollideAtIntegerTime(velocityDiff: Long, hailDistance: Long) =
            velocityDiff != 0L && hailDistance % velocityDiff == 0L

        fun findAllowedRockVelocitiesAtAxis(axis: Axis, hail1: Hailstone, hail2: Hailstone): Set<Long>? {
            val hailVelocity = hail1.velocity[axis]
            if (hailVelocity != hail2.velocity[axis]) {
                return null
            }

            val hailDistance = hail1.point[axis] - hail2.point[axis]
            return rockVelocityRange
                .filter { rockVelocity -> canCollideAtIntegerTime(rockVelocity - hailVelocity, hailDistance) }
                .toSet()
        }

        fun Hailstorm.computePotentialRockVelocitiesAtAxis(axis: Axis): Set<Long> = hailstones
            .zipWithEveryFollowing()
            .mapNotNull { (hail1, hail2) -> findAllowedRockVelocitiesAtAxis(axis, hail1, hail2) }
            .reduce(Set<Long>::intersect)

        fun computePotentialRockVelocities(hailstorm: Hailstorm): List<Velocity3> {
            val velocities = Axis.entries.associateWith { axis -> hailstorm.computePotentialRockVelocitiesAtAxis(axis) }

            return velocities[Axis.X]!!.flatMap { dx ->
                velocities[Axis.Y]!!.flatMap { dy ->
                    velocities[Axis.Z]!!.map { dz ->
                        Velocity3(dx, dy, dz)
                    }
                }
            }
        }
    }

    class RockOriginFinder {

        fun Hailstone.pointAt(time: Long) = point + velocity * time

        fun findTimeDifferenceAtAxis(
            axis: Axis,
            rockVelocity: Velocity3,
            hail1: Hailstone,
            hail2: Hailstone
        ): Long? {
            if (hail1.velocity[axis] != hail2.velocity[axis]) {
                return null
            }

            val hailVelocity = hail1.velocity[axis]
            val distance = hail2.point[axis] - hail1.point[axis]
            val velocityDiff = rockVelocity.dx - hailVelocity
            check(distance % velocityDiff == 0L)
            return distance / velocityDiff
        }

        // time2 = time1 + timeDiff
        // hail1.pointAt(time1) + rockV * timeDiff = hail2.pointAt(time2)
        // --> hail1.p + hail1.v * time1 + rockV * timeDiff = hail2.p + hail2.v * (time1 + timeDiff)
        // --> time1 = (hail1.p - hail2.p + timeDiff * (rockV - hail2.v)) / (hail2.v - hail1.v)
        private fun calculateTime1AtAxis(
            axis: Axis, hail1: Hailstone, hail2: Hailstone, timeDifference: Long, rockVelocity: Velocity3
        ): Long = (hail1.point[axis] - hail2.point[axis] + (rockVelocity[axis] - hail2.velocity[axis]) * timeDifference) /
                (hail2.velocity[axis] - hail1.velocity[axis])

        fun findHailstoneMatch(rockVelocity: Velocity3, hail1: Hailstone, hail2: Hailstone): HailstoneMatch? {
            val timeDifference = findTimeDifferenceAtAxis(Axis.X, rockVelocity, hail1, hail2)
                ?: return null

            val time1 = calculateTime1AtAxis(Axis.Y, hail1, hail2, timeDifference, rockVelocity)
            val time2 = time1 + timeDifference

            val hailAt1 = hail1.pointAt(time1)
            val hailAt2 = hail2.pointAt(time2)
            val rockAt2 = hailAt1 + rockVelocity * timeDifference

            return if (hailAt2 == rockAt2) HailstoneMatch(rockVelocity, hail1, time1) else null
        }

        fun HailstoneMatch.originPoint() = hailstone.pointAt(time) - rockVelocity * time

        fun Hailstorm.findOriginPoint(rockVelocity: Velocity3): Point3? = hailstones.zipWithEveryFollowing()
            .mapNotNull { (a, b) -> findHailstoneMatch(rockVelocity, a, b) }
            .drop(1)
            .firstOrNull() // to ensure it's a valid velocity, it must match at least 2 hailstones
            ?.originPoint()

        fun Hailstorm.findRockHailstone(rockVelocity: Velocity3): Hailstone? = findOriginPoint(rockVelocity)
            ?.let { point -> Hailstone(point, rockVelocity) }

        fun computeRockHailstone(hailstorm: Hailstorm, rockVelocities: List<Velocity3>): Hailstone = rockVelocities
            .firstNotNullOf { rockVelocity -> hailstorm.findRockHailstone(rockVelocity) }
    }

    fun part1(input: List<String>, testRange: LongRange): Int {
        val hailstorm = input.parseHailstorm()
        val xyCollisionsInRange = hailstorm.xyCollisions()
            .filter { point -> point.x in testRange && point.y in testRange }
            .toList()
        return xyCollisionsInRange.size
    }

    fun part2(input: List<String>): Long {
        val hailstorm = input.parseHailstorm()
        val rockVelocities = RockVelocityFinder().computePotentialRockVelocities(hailstorm)
        val rockHailstone = RockOriginFinder().computeRockHailstone(hailstorm, rockVelocities)

        return with(rockHailstone.point) { x + y + z }
    }

    // TESTS
    val test1 = part1(readInput("$day/test"), testRange = 7L..27L)
    2.let { check(test1 == it) { "Test 1: is $test1, should be $it" } }

    val test2 = part2(readInput("$day/test"))
    47L.let { check(test2 == it) { "Test 2: is $test2, should be $it" } }

    // RESULTS
    val input = readInput("$day/input")
    val part1 = part1(input, testRange = 200000000000000..400000000000000)
    15107.let { println("Part 1: $part1" + if (part1 == it) "" else " (should be $it?)") }
    val part2 = part2(input)
    println("Part 2: $part2")
}
