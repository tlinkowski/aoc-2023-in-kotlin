fun main() {
    val day = "Day16"

    // MODEL
    class Contraption(
        val gridRange: GridRange,
        val pointMap: Map<Point, Char>
    )

    data class Beam(val point: Point, val dir: Dir)

    // PARSE
    fun List<String>.parseContraption() = Contraption(
        gridRange = toGridRange(),
        pointMap = toPointMap().filterValues { it != '.' }
    )

    // SOLVE
    operator fun Contraption.contains(beam: Beam) = beam.point in gridRange

    fun Beam.nextBeam(nextDir: Dir) = Beam(point.move(nextDir), nextDir)

    fun Dir.nextDirs(symbol: Char?): List<Dir> {
        fun cw() = listOf(turnCW())
        fun ccw() = listOf(turnCCW())
        fun split() = cw() + ccw()
        fun keep() = listOf(this)

        val vertical = isVertical()
        return when (symbol) {
            '/' -> if (vertical) cw() else ccw()
            '\\' -> if (vertical) ccw() else cw()
            '-' -> if (vertical) split() else keep()
            '|' -> if (vertical) keep() else split()
            else -> keep()
        }
    }

    fun Contraption.nextBeams(beam: Beam): List<Beam> = beam.dir
        .nextDirs(pointMap[beam.point])
        .map { dir -> beam.nextBeam(dir) }
        .filter { it in this }

    fun Contraption.toString(beams: List<Beam>): String {
        val combinedMap = pointMap + beams.associate { it.point to it.dir.symbol }
        return gridRange.yRange.joinToString("\n") { y ->
            gridRange.xRange.map { x -> combinedMap.getOrDefault(Point(x, y), '.') }.joinToString("")
        }
    }

    fun Contraption.findAllBeams(startBeam: Beam, print: Boolean): Set<Beam> = buildSet {
        var currentBeams = listOf(startBeam)
        do { // BFS
            if (print) println(toString(currentBeams) + "\n")
            currentBeams = currentBeams
                .filter { beam -> add(beam) }
                .flatMap { beam -> nextBeams(beam) }
        } while (currentBeams.isNotEmpty())
    }

    fun Contraption.energizedPoints(startBeam: Beam, print: Boolean = false): Set<Point> =
        findAllBeams(startBeam, print).asSequence().map { it.point }.toSet()

    fun GridRange.startBeams() = listOf(
        pointsAtX(x = 0).map { p -> Beam(p, Dir.RIGHT) },
        pointsAtY(y = 0).map { p -> Beam(p, Dir.DOWN) },
        pointsAtX(x = xRange.last).map { p -> Beam(p, Dir.LEFT) },
        pointsAtY(y = yRange.last).map { p -> Beam(p, Dir.UP) }
    ).flatten()

    fun part1(input: List<String>, print: Boolean = false): Int {
        val contraption = input.parseContraption()
        val startBeam = Beam(Point(0, 0), Dir.RIGHT)

        return contraption.energizedPoints(startBeam, print).size
    }

    fun part2(input: List<String>): Int {
        val contraption = input.parseContraption()

        return contraption.gridRange.startBeams()
            .maxOf { startBeam -> contraption.energizedPoints(startBeam).size }
    }

    // TESTS
    val test1 = part1(readInput("$day/test"), print = true)
    46.let { check(test1 == it) { "Test 1: is $test1, should be $it" } }

    val test2 = part2(readInput("$day/test"))
    51.let { check(test2 == it) { "Test 2: is $test2, should be $it" } }

    // RESULTS
    val input = readInput("$day/input")
    val part1 = part1(input)
    6361.let { println("Part 1: $part1" + if (part1 == it) "" else " (should be $it?)") }
    val part2 = part2(input)
    println("Part 2: $part2")
}
