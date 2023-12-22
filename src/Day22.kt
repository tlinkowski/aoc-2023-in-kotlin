import java.util.*
import kotlin.math.max
import kotlin.math.min

fun main() {
    val day = "Day22"

    // MODEL
    data class Point3d(val x: Int, val y: Int, val z: Int) {
        init {
            check(z >= 1) { this }
        }

        override fun toString() = "$x,$y,$z"
    }

    data class Brick(val id: Int, val a: Point3d, val b: Point3d) {
        private val xRange = range { it.x }
        private val yRange = range { it.y }
        private val zRange = range { it.z }

        val xyPoints = xRange.flatMap { x -> yRange.map { y -> Point(x, y) } }

        val minZ
            get() = zRange.first
        val maxZ
            get() = zRange.last

        private fun range(f: (Point3d) -> Int) = min(f(a), f(b))..max(f(a), f(b))

        override fun toString() = "[$id] $a~$b"
    }

    data class Snapshot(val bricks: Set<Brick>)

    data class BrickFall(val oldBrick: Brick, val fallAmount: Int)

    // PARSE
    fun String.parsePoint3d() = split(',')
        .map { it.toInt() }
        .let { (x, y, z) -> Point3d(x, y, z) }

    fun String.parseBrick(id: Int) = split('~').let { (a, b) ->
        Brick(id, a.parsePoint3d(), b.parsePoint3d())
    }

    fun List<String>.parseSnapshot() = Snapshot(mapIndexed { id, line -> line.parseBrick(id) }.toSet())

    // SOLVE
    fun Brick.fallBy(fallAmount: Int) = copy(
        a = a.copy(z = a.z - fallAmount),
        b = b.copy(z = b.z - fallAmount)
    )

    fun BrickFall.newBrick() = oldBrick.fallBy(fallAmount)

    class BrickTetris(snapshot: Snapshot) {

        private val bricksByXY: MutableMap<Point, SortedSet<Brick>> = snapshot.bricks
            .flatMap { brick -> brick.xyPoints.map { xy -> xy to brick } }
            .groupBy({ (xy, _) -> xy }, { (_, brick) -> brick })
            .mapValues { (_, bricks) -> bricks.toSortedSet(compareBy { it.minZ }) }
            .toMutableMap()

        private val bricksByMinZ: SortedMap<Int, MutableList<Brick>> = snapshot.bricks
            .groupBy { it.minZ }
            .mapValues { (_, bricks) -> bricks.toMutableList() }
            .toSortedMap()

        var lastCheckedMinZ = 1

        fun bricks() = bricksByMinZ.values.asSequence().flatten()

        private fun bricksAt(xy: Point) = bricksByXY[xy]!!

        private fun bricksAt(minZ: Int) = bricksByMinZ.at(minZ)

        fun remove(brick: Brick) {
            brick.xyPoints.forEach { xy -> bricksAt(xy) -= brick }
            bricksAt(brick.minZ) -= brick
        }

        private fun add(brick: Brick) {
            brick.xyPoints.forEach { xy -> bricksAt(xy) += brick }
            bricksAt(brick.minZ) += brick
        }

        fun makeAllBricksFall(print: Boolean = false) {
            var counter = 0
            while (tryMakeNextBrickFall()) {
                counter++
            }
            if (print) println("Made $counter bricks fall")
        }

        private fun tryMakeNextBrickFall(): Boolean {
            val brickFall = findNextBrickToFall() ?: return false

            remove(brickFall.oldBrick)
            add(brickFall.newBrick())

            lastCheckedMinZ = brickFall.oldBrick.minZ
            return true
        }

        private fun findNextBrickToFall(): BrickFall? = bricksByMinZ
            .tailMap(lastCheckedMinZ) // do not check below last checked minZ
            .values
            .asSequence()
            .flatten()
            .firstNotNullOfOrNull { brick -> findFallAmount(brick)?.let { fall -> BrickFall(brick, fall) } }

        private fun findFallAmount(brick: Brick): Int? {
            val zAfterFall = brick.xyPoints.maxOf { xy -> minZAfterFallAt(brick, xy) }
            return (brick.minZ - zAfterFall).takeIf { it > 0 }
        }

        private fun minZAfterFallAt(brick: Brick, xy: Point): Int {
            val bricksBelow = bricksAt(xy).headSet(brick)
            if (bricksBelow.isEmpty()) {
                return 1
            }
            return bricksBelow.last().maxZ + 1
        }
    }

    fun Snapshot.makeBricksFall(): Snapshot {
        val tetris = BrickTetris(this)
        tetris.makeAllBricksFall(print = true)
        return Snapshot(tetris.bricks().toSet())
    }

    // returns number of bricks that would fall if given brick were removed
    fun Snapshot.countAffectedBricksAfterRemovalOf(brick: Brick): Int {
        val tetris = BrickTetris(this)
        tetris.remove(brick)
        tetris.makeAllBricksFall()
        return tetris.bricks().count { it !in bricks }
    }

    fun Snapshot.findBrickRemovalCounts(): Map<Brick, Int> = bricks.associateWith { brick ->
        countAffectedBricksAfterRemovalOf(brick)
    }

    fun parts(input: List<String>): List<Int> {
        val brickRemovalCounts = input
            .parseSnapshot()
            .makeBricksFall()
            .findBrickRemovalCounts()

        val part1 = brickRemovalCounts.values.count { it == 0 }
        val part2 = brickRemovalCounts.values.sum()
        return listOf(part1, part2)
    }

    // TESTS
    val (test1, test2) = parts(readInput("$day/test"))
    5.let { check(test1 == it) { "Test 1: is $test1, should be $it" } }
    7.let { check(test2 == it) { "Test 2: is $test2, should be $it" } }

    // RESULTS
    val input = readInput("$day/input")
    val (part1, part2) = parts(input)
    398.let { println("Part 1: $part1" + if (part1 == it) "" else " (should be $it?)") }
    println("Part 2: $part2")
}
