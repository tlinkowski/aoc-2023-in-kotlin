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
        if (cur.c in listOf('-', '└', '┌') && move(cur, 1, 0)?.c in listOf('-', '┘', '┐', 'S') && prev != move(cur, 1, 0)) {
            return move(cur, 1, 0)!!
        }
        if (cur.c in listOf('-', '┘', '┐') && move(cur, -1, 0)?.c in listOf('-', '└', '┌', 'S') && prev != move(cur, -1, 0)) {
            return move(cur, -1, 0)!!
        }
        if (cur.c in listOf('|', '┐', '┌') && move(cur, 0, 1)?.c in listOf('|', '┘', '└', 'S') && prev != move(cur, 0, 1)) {
            return move(cur, 0, 1)!!
        }
        if (cur.c in listOf('|', '┘', '└') && move(cur, 0, -1)?.c in listOf('|', '┐', '┌', 'S') && prev != move(cur, 0, -1)) {
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

    fun part2(input: List<String>): Long {
        return 0
    }

    // TESTS
    val part1 = part1(readInput("$day/test"))
    check(part1 == 8L) { "Part 1: actual=$part1" }

    val part2 = part2(readInput("$day/test"))
    check(part2 == 0L) { "Part 2: actual=$part2" }

    // RESULTS
    val input = readInput("$day/input")
    println("Part 1: " + part1(input))
    println("Part 2: " + part2(input))
}
