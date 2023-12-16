enum class Pipe(val symbol: Char, val dirs: Set<Dir>) {
    START('┼', Dir.entries.toSet()),
    HORIZONTAL('─', setOf(Dir.LEFT, Dir.RIGHT)),
    VERTICAL('│', setOf(Dir.UP, Dir.DOWN)),
    DOWN_RIGHT('┌', setOf(Dir.DOWN, Dir.RIGHT)),
    DOWN_LEFT('┐', setOf(Dir.DOWN, Dir.LEFT)),
    UP_LEFT('┘', setOf(Dir.UP, Dir.LEFT)),
    UP_RIGHT('└', setOf(Dir.UP, Dir.RIGHT))
}

fun main() {
    val day = "Day10"

    // MODEL
    data class PipePoint(val point: Point, val pipe: Pipe)

    data class PipeMap(val pipePoints: Map<Point, PipePoint>)

    data class PipePointMove(val sourceDir: Dir, val target: PipePoint)

    // PARSE
    fun List<String>.normalize() = map { line ->
        line
            .replace('.', ' ')
            .replace('S', '┼')
            .replace('-', '─')
            .replace('|', '│')
            .replace('F', '┌')
            .replace('L', '└')
            .replace('7', '┐')
            .replace('J', '┘')
    }

    fun Char.toPipe() = Pipe.entries.firstOrNull { it.symbol == this }

    fun List<String>.parsePipeMap() = mapIndexedNotNull { y, line ->
        line.mapIndexedNotNull { x, c -> c.toPipe()?.let { pipe -> PipePoint(Point(x, y), pipe) } }
    }
        .flatten()
        .associateBy { it.point }
        .let { PipeMap(it) }

    // SOLVE
    fun PipeMap.tryMove(source: PipePoint, dir: Dir): PipePointMove? = pipePoints[source.point.move(dir)]
        ?.let { target -> PipePointMove(dir, target) }

    fun PipeMap.move(source: PipePoint, dir: Dir): PipePointMove = tryMove(source, dir)!!

    fun PipePointMove.connectsBack() = sourceDir.opposite() in target.pipe.dirs

    fun PipeMap.firstNextMove(start: PipePoint): PipePointMove = Dir.entries
        .mapNotNull { dir -> tryMove(start, dir) }
        .first { move -> move.connectsBack() }

    fun PipePointMove.nextDir() = (target.pipe.dirs - sourceDir.opposite()).single()

    fun PipeMap.nextMove(prevMove: PipePointMove): PipePointMove = move(source = prevMove.target, prevMove.nextDir())

    fun PipeMap.detectPipeLoop(): List<PipePointMove> = pipePoints.values
        .first { it.pipe == Pipe.START }
        .let { start ->
            generateSequence(firstNextMove(start)) { nextMove(it) }
                .takeWhileInclusive { it.target != start }
                .toList()
        }

    fun pipeOf(dirs: Set<Dir>) = Pipe.entries.first { it.dirs == dirs }

    fun PipePointMove.withPipe(pipe: Pipe) = copy(target = target.copy(pipe = pipe))

    fun List<PipePointMove>.normalizedStartMove() = last()
        .also { start -> check(start.target.pipe == Pipe.START) { start } }
        .let { start ->
            val startDir1 = start.sourceDir.opposite()
            val startDir2 = first().sourceDir
            start.withPipe(pipeOf(setOf(startDir1, startDir2)))
        }

    fun List<PipePointMove>.replaceStart() = dropLast(1) + normalizedStartMove()

    fun List<PipePoint>.xRange() = minOf { it.point.x }..maxOf { it.point.x }

    fun List<PipePoint>.yRange() = minOf { it.point.y }..maxOf { it.point.y }

    fun List<PipePoint>.allPoints() = xRange().flatMap { x -> yRange().map { y -> Point(x, y) } }

    fun toNiceString(loop: List<PipePoint>, interior: Set<Point>): String {
        val map = loop.associateBy { it.point }

        fun symbol(p: Point) = if (p in interior) '▒' else map[p]?.pipe?.symbol ?: ' '

        return loop.yRange().joinToString("\n") { y ->
            loop.xRange().map { x -> symbol(Point(x, y)) }.joinToString("")
        }
    }

    fun Point.isInside(loop: List<PipePoint>): Boolean {
        if (loop.any { move -> this == move.point }) {
            return false
        }

        val sameYLowerX = loop.filter { it.point.y == y && it.point.x < x }

        val simpleVerticalIntersections = sameYLowerX
            .count { it.pipe == Pipe.VERTICAL }

        val edgeVerticalIntersections = sameYLowerX.asSequence()
            .filter { it.pipe != Pipe.HORIZONTAL }
            .sortedBy { it.point.x }
            .map { it.pipe.symbol }
            .zipWithNext { a, b -> "$a$b" }
            .count { it in listOf("┌┘", "└┐") }

        return (simpleVerticalIntersections + edgeVerticalIntersections) % 2 == 1
    }

    fun part1(input: List<String>): Long {
        val normInput = input.normalize()
        normInput.onEach(::println)

        val pipeMap = normInput.parsePipeMap()
        val loop = pipeMap.detectPipeLoop()

        return (loop.size / 2).toLong()
    }

    fun part2(input: List<String>): Long {
        val normInput = input.normalize()

        val pipeMap = normInput.parsePipeMap()
        val loop = pipeMap.detectPipeLoop()
            .replaceStart()
            .map { it.target }

        val interior = loop.allPoints()
            .filter { it.isInside(loop) }
            .toSet()

        println(toNiceString(loop, interior))

        return interior.size.toLong()
    }

    // TESTS
    val part1 = part1(readInput("$day/test1"))
    check(part1 == 8L) { "Part 1: actual=$part1" }

    val part2 = part2(readInput("$day/test2"))
    check(part2 == 10L) { "Part 2: actual=$part2" }

    val part2a = part2(readInput("$day/test2a"))
    check(part2a == 8L) { "Part 2a: actual=$part2a" }

    val part2b = part2(readInput("$day/test2b"))
    check(part2b == 4L) { "Part 2b: actual=$part2b" }

    // RESULTS
    val input = readInput("$day/input")
    println("Part 1: " + part1(input))
    println("Part 2: " + part2(input))
}
