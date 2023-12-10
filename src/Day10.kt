fun main() {
    val day = "Day10"

    // MODEL
    data class Point(val x: Int, val y: Int) {
        fun move(dx: Int, dy: Int) = Point(x + dx, y + dy)

        fun nextPoints() = listOf(
            move(-1, 0),
            move(1, 0),
            move(0, -1),
            move(0, 1)
        )
    }

    data class Pipe(val point: Point, val c: Char)

    data class PipeMap(val pipes: Map<Point, Pipe>)

    // PARSE
    fun List<String>.parsePipeMap() = mapIndexedNotNull { y, line ->
        line
            .replace('F', '┌')
            .replace('L', '└')
            .replace('7', '┐')
            .replace('J', '┘')
            .mapIndexedNotNull { x, c ->
                if (c == '.') null else Pipe(Point(x, y), c)
            }
    }
        .flatten()
        .associateBy { it.point }
        .let { PipeMap(it) }

    // SOLVE
    fun PipeMap.move(p: Pipe, dx: Int, dy: Int) = pipes[p.point.move(dx, dy)]

    fun PipeMap.firstNext(p: Pipe): Pipe { // OK!
        if (move(p, 1, 0)?.c in listOf('-', '┘', '┐')) {
            return move(p, 1, 0)!!
        }
        if (move(p, -1, 0)?.c in listOf('-', '└', '┌')) {
            return move(p, -1, 0)!!
        }
        if (move(p, 0, 1)?.c in listOf('|', '┘', '└')) {
            return move(p, 0, 1)!!
        }
        if (move(p, 0, -1)?.c in listOf('|', '┐', '┌')) {
            return move(p, 0, -1)!!
        }
        throw IllegalArgumentException(p.toString())
    }

    fun PipeMap.next(prev: Pipe, cur: Pipe): Pipe {
        if (cur.c in listOf('-', '└', '┌') && move(cur, 1, 0)?.c in listOf('-', '┘', '┐', 'S')
            && prev != move(cur, 1, 0)
        ) {
            return move(cur, 1, 0)!!
        }
        if (cur.c in listOf('-', '┘', '┐') && move(cur, -1, 0)?.c in listOf('-', '└', '┌', 'S')
            && prev != move(cur, -1, 0)
        ) {
            return move(cur, -1, 0)!!
        }
        if (cur.c in listOf('|', '┐', '┌') && move(cur, 0, 1)?.c in listOf('|', '┘', '└', 'S')
            && prev != move(cur, 0, 1)
        ) {
            return move(cur, 0, 1)!!
        }
        if (cur.c in listOf('|', '┘', '└') && move(cur, 0, -1)?.c in listOf('|', '┐', '┌', 'S')
            && prev != move(cur, 0, -1)
        ) {
            return move(cur, 0, -1)!!
        }
        throw IllegalArgumentException(cur.toString())
    }

//    fun Pipe.isPotentialNext(cur: Pipe): Boolean {
//        if (c == 'S') {
//            return true
//        }
//        if (cur.c == '-' && c in listOf('-', '┘', '┐')) {
//            return point == cur.point.move(1, 0) || point == cur.point.move(-1, 0)
//        }
//
////        if (point == cur.point.move(1, 0))
////
////        return c in listOf('-', '┘', '┐') && point == cur.point.move(1, 0)
////                || c in listOf('-', '└', '┌') && point == cur.point.move(-1, 0)
////                || c in listOf('|', '┐', '┌') && point == cur.point.move(0, -1)
////                || c in listOf('|', '┘', '└') && point == cur.point.move(0, 1)
//    }

//    fun PipeMap.next(prev: Pipe, cur: Pipe): Pipe {
//        val nextPoints = cur.point.nextPoints()
//            .mapNotNull { pipes[it] }
//            .filter { it != prev }
//        return nextPoints
//            .firstOrNull { it.isPotentialNext(cur) }
//            ?: throw IllegalStateException(nextPoints.toString())
//    }

    fun part1(input: List<String>): Long {
        val pipeMap = input.parsePipeMap()
        val start = pipeMap.pipes.values
            .first { it.c == 'S' }
        var prev = start
        var cur = pipeMap.firstNext(start)
        var length = 0
        do {
            println(cur)
            val nextCur = pipeMap.next(prev, cur)
            prev = cur
            cur = nextCur
            length++
        } while (cur != start)
        return (1 + length / 2).toLong()
    }

//    fun Pipe.split() = when (c) {
//        '┐', '┌', '└', '┘' -> listOf(Pipe(point, '-'), Pipe(point, '|'))
//        else -> listOf(this)
//    }

    fun List<Char>.chunkedReduce(transform: (a: Char, b: Char) -> Char?): List<Char> {
        var i = 0
        val result = ArrayList<Char>()
        while (i < size) {
            if (i == size - 1) {
                result.add(this[i])
            } else {
                val t = transform(this[i], this[i + 1])
                if (t != null) {
                    result.add(t)
                    i++ // skip i + 1
                } else {
                    result.add(this[i])
                }
            }
            i++
        }
        return result
    }

    fun List<Pipe>.isInside(p: Point): Boolean {
        if (any { it.point == p }) {
            return false
        }
        val pipes = this // flatMap { it.split() }

        val sameXLowerY = pipes
            .filter { it.point.x == p.x && it.point.y < p.y }
            .filter { it.c != '|' }
            .sortedBy { it.point.y }
            .map { it.c }
            .chunkedReduce { a, b -> if (a == '┐' && b == '└' || a == '┌' && b == '┘') '-' else null }
        val sameXHigherY = pipes
            .filter { it.point.x == p.x && it.point.y > p.y }
            .filter { it.c != '|' }
            .sortedBy { it.point.y }
            .map { it.c }
            .chunkedReduce { a, b -> if (a == '┐' && b == '└' || a == '┌' && b == '┘') '-' else null }
        val sameYLowerX = pipes
            .filter { it.point.y == p.y && it.point.x < p.x }
            .filter { it.c != '-' }
            .sortedBy { it.point.x }
            .map { it.c }
            .chunkedReduce { a, b -> if (a == '└' && b == '┐' || a == '┌' && b == '┘') '|' else null }
        val sameYHigherX = pipes
            .filter { it.point.y == p.y && it.point.x > p.x }
            .filter { it.c != '-' }
            .sortedBy { it.point.x }
            .map { it.c }
            .chunkedReduce { a, b -> if (a == '└' && b == '┐' || a == '┌' && b == '┘') '|' else null }
        return sameXLowerY.size % 2 == 1
                && sameXHigherY.size % 2 == 1
                && sameYLowerX.size % 2 == 1
                && sameYHigherX.size % 2 == 1
    }

//    data class PipeDir(val pipe: Pipe, val dir: Int)

    fun List<Pipe>.firstPointInside() = first { isInside(it.point) }.point

    class PointFinder(val loop: Set<Point>) {
        val seen = HashSet<Point>()
        val minX = loop.minOf { it.x }
        val minY = loop.minOf { it.y }
        val maxX = loop.maxOf { it.x }
        val maxY = loop.maxOf { it.y }

        fun floodFill(p: Point): Set<Point> {
            if (p in loop || !p.isValid()) {
                return setOf()
            }
            val points = allPointsInside(p)
            return if (points.any { it.isEdge() }) setOf() else points
        }

        fun Point.isValid() = x >= minX && y >= minY && x <= maxX && y <= maxY

        fun Point.isEdge() = x == minX || y == minY || x == maxX || y == maxY

        fun allPointsInside(p: Point): Set<Point> = setOf(p) + p.nextPoints()
            .filter { it.isValid() }
            .filter(seen::add)
            .filter { it !in loop }
            .flatMap { allPointsInside(it) }
    }

    fun part2(input: List<String>): Long {
        val pipeMap = input.parsePipeMap()
        val start = pipeMap.pipes.values
            .first { it.c == 'S' }
        var prev = start
        val pipeLoop = ArrayList<Pipe>()

        var cur = pipeMap.firstNext(start)
        do {
            pipeLoop.add(cur)
            val nextCur = pipeMap.next(prev, cur)
            prev = cur
            cur = nextCur
        } while (cur != start)
        pipeLoop.add(start) // TODO: replace S

        val loopSet = pipeLoop.map { it.point }.toSet()

        val pointsInside = input.flatMapIndexed { y, line ->
            line.indices.map { x -> Point(x, y) }
        }
            .filter { it !in loopSet }
            .filter { pipeLoop.isInside(it) }

        // 376 too low
        // 739 too high

        return pointsInside.size.toLong()

//        val allPoints = input.flatMapIndexed { y, line ->
//            line.indices.map { x -> Point(x, y) }
//        }
//
//        val areas = allPoints
//            .map { finder.floodFill(it) }
//            .distinct()
//            .onEach { println(it) }
//
//        // 376 too low
//
//        return areas.last().size.toLong()


//        val seedPoints = input.flatMapIndexed { y, line ->
//            line.indices.map { x -> Point(x, y) }
//        }
//            .filter { it !in loopSet }
//            .filter { pipeLoop.isInside(it) }
//
//        val areas = seedPoints
//            .map { PointFinder(loopSet).floodFill(it) }
//            .distinct()
//            .onEach { println(it) }
//
//        // 376 too low
//        // 739 too high
//
//        return areas.sumOf { it.size }.toLong()
    }

    // TESTS
//    val part1 = part1(readInput("$day/test1"))
//    check(part1 == 8L) { "Part 1: actual=$part1" }

    val part2 = part2(readInput("$day/test2"))
    check(part2 == 10L) { "Part 2: actual=$part2" }

    val part2a = part2(readInput("$day/test2a"))
    check(part2a == 8L) { "Part 2a: actual=$part2a" }

    val part2b = part2(readInput("$day/test2b"))
    check(part2b == 4L) { "Part 2b: actual=$part2b" }

    // RESULTS
    val input = readInput("$day/input")
//    println("Part 1: " + part1(input))
    println("Part 2: " + part2(input))
}
