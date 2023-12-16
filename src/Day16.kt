fun main() {
    val day = "Day16"

    // MODEL
    data class Point(val x: Int, val y: Int)

    data class Contraption(
        val xRange: IntRange,
        val yRange: IntRange,
        val map: Map<Point, Char>
    )

    data class Beam(val p: Point, val dir: Dir)

    // PARSE
    fun List<String>.parseContraption() = Contraption(
        xRange = first().indices,
        yRange = indices,
        map = flatMapIndexed { y, line ->
            line.mapIndexedNotNull { x, c -> if (c != '.') Point(x, y) to c else null }
        }.toMap()
    )

    // SOLVE
    operator fun Contraption.contains(p: Point) = p.x in xRange && p.y in yRange

    operator fun Contraption.contains(b: Beam) = b.p in this

    fun Point.move(d: Dir) = Point(x + d.dx, y + d.dy)

    fun Beam.move() = Beam(p.move(dir), dir)

    fun Beam.changeDir(nd: Dir) = Beam(p.move(nd), nd)

    fun Contraption.move(b: Beam) = when (map[b.p]) {
        '/' -> when (b.dir) {
            Dir.LEFT -> Dir.DOWN
            Dir.RIGHT -> Dir.UP
            Dir.DOWN -> Dir.LEFT
            Dir.UP -> Dir.RIGHT
        }.let { listOf(b.changeDir(it)) }
        '\\' -> when (b.dir) {
            Dir.LEFT -> Dir.UP
            Dir.RIGHT -> Dir.DOWN
            Dir.DOWN -> Dir.RIGHT
            Dir.UP -> Dir.LEFT
        }.let { listOf(b.changeDir(it)) }
        '|' -> when (b.dir) {
            Dir.UP, Dir.DOWN -> listOf(b.move())
            Dir.LEFT, Dir.RIGHT -> listOf(
                b.changeDir(Dir.UP),
                b.changeDir(Dir.DOWN)
            )
        }
        '-' -> when (b.dir) {
            Dir.LEFT, Dir.RIGHT -> listOf(b.move())
            Dir.UP, Dir.DOWN -> listOf(
                b.changeDir(Dir.LEFT),
                b.changeDir(Dir.RIGHT)
            )
        }
        else -> listOf(b.move())
    }.filter { it in this }

    fun print(c: Contraption, b: List<Beam>) {
        val map = c.map + b.associate {
            it.p to it.dir.name[0]
        }
        c.yRange.forEach { y ->
            c.xRange.forEach { x -> print(map.getOrDefault(Point(x, y), ' ')) }
            println()
        }
        println()
    }

    fun energeizedPoints(startBeam: Beam, contraption: Contraption): Int {
        val energizedPoints = HashSet<Point>()

        var beams = listOf(startBeam)
        val seenBeams = mutableSetOf<Beam>()

        while (beams.isNotEmpty()) {
    //            print(contraption, beams)
    //            println("Beams: ${beams.size}")
            val newBeams = beams.flatMap { contraption.move(it) }
                .filter { seenBeams.add(it) }
            newBeams.forEach { energizedPoints += it.p }
            beams = newBeams
        }

        return energizedPoints.size
    }

    fun part1(input: List<String>): Int {
        val contraption = input.parseContraption()
        val startBeam = Beam(Point(-1, 0), Dir.RIGHT)

        return energeizedPoints(startBeam, contraption)
    }

    fun part2(input: List<String>): Int {
        val contraption = input.parseContraption()

        val startBeams = contraption.yRange.flatMap { y ->
            listOf(
                Beam(Point(-1, y), Dir.RIGHT),
                Beam(Point(contraption.xRange.last + 1, y), Dir.LEFT)
            )
        } + contraption.xRange.flatMap { x ->
            listOf(
                Beam(Point(x, -1), Dir.DOWN),
                Beam(Point(x, contraption.yRange.last + 1), Dir.UP)
            )
        }
        return startBeams.maxOf { sb -> energeizedPoints(sb, contraption) }

    }

    // TESTS
    val test1 = part1(readInput("$day/test"))
    46.let { check(test1 == it) { "Test 1: is $test1, should be $it" } }

    val test2 = part2(readInput("$day/test"))
    51.let { check(test2 == it) { "Test 2: is $test2, should be $it" } }

    // RESULTS
    val input = readInput("$day/input")
    val part1 = part1(input)
    0.let { println("Part 1: $part1" + if (part1 == it) "" else " (should be $it?)") }
    val part2 = part2(input)
    println("Part 2: $part2")
}
