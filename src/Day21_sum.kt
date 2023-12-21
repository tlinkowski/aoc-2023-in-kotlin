fun main() {
    val day = "Day21"

    // MODEL
    // remaining 64 steps
// He gives you an up-to-date map (your puzzle input) of his
// starting position (S), garden plots (.), and rocks (#)
    data class Garden(val gridRange: GridRange, val start: Point, val rocks: Set<Point>)

    // PARSE
    fun List<String>.parseGarden(): Garden {
        val pointMap = toPointMap()
        val start = pointMap.firstNotNullOf { (p, c) -> if (c == 'S') p else null }
        val rocks = pointMap.filterValues { it == '#' }.keys
        return Garden(toGridRange(), start, rocks)
    }

    fun GridRange.adapted(p: Point) = Point(
        p.x % xRange.size(),
        p.y % yRange.size()
    )

    fun GridRange.expanded(times: Int) = GridRange(
        xRange = xRange.first - times * xRange.size()..xRange.last + times * xRange.size(),
        yRange = yRange.first - times * yRange.size()..yRange.last + times * yRange.size(),
    )

    // SOLVE
    data class DirPath(val map: Map<Dir, Int>)

    fun GridRange.move(dir: Dir) = GridRange(
        xRange = (xRange.size() * dir.dx).let { xRange.first + it..xRange.last + it },
        yRange = (yRange.size() * dir.dy).let { yRange.first + it..yRange.last + it }
    )

    fun GridRange.move(xTimes: Int, yTimes: Int) = GridRange(
        xRange = (xRange.size() * xTimes).let { xRange.first + it..xRange.last + it },
        yRange = (yRange.size() * yTimes).let { yRange.first + it..yRange.last + it }
    )

    data class Uni(
        val base: GridRange,
        val times: Int = 0,
        val spanning: GridRange = base,
        val all: Set<GridRange> = setOf(base)
    ) {
        fun expand(): Uni {
            val newTimes = times + 1
            return Uni(
                base = base,
                times = newTimes,
                spanning = base.expanded(newTimes),
                all = (-newTimes..newTimes).flatMap { xTimes ->
                    (-newTimes..newTimes).map { yTimes -> base.move(xTimes, yTimes) }
                }.toSet()
            )
        }
    }

    fun Garden.reachablePlots(steps: Int): Int {
        val stepCycles = mutableListOf<Int>()
//        val r1 = mutableListOf<Int>()
//        val r2 = mutableListOf<Int>()
//        val rCycles = mutableListOf<Int>()

        var reachablePoints = setOf(start)
        var uni = Uni(gridRange)
        for (step in 1..1000) {

//            if (reachablePoints.any { it !in gr }) {
//                stepCycles += step
//                r1 += reachablePoints.count { it in gridRange }
//                r2 += reachablePoints.count { it !in gridRange }
//                val fCycle = stepCycles[stepCycles.size - 1] - stepCycles.getOrElse(stepCycles.size - 2) { 0 }
//                val r1Cycle = r1[r1.size - 1] - r1.getOrElse(r1.size - 2) { 0 }
//                val r2Cycle = r2[r2.size - 1] - r2.getOrElse(r2.size - 2) { 0 }
////                rCycles += rCycle
//                // split r2 by grid and group
////                val rCycle2 = rCycles[rCycles.size - 1] - rCycles.getOrElse(r.size - 2) { 0 }
//                println("x${stepCycles.size}------ $step (cycle: $fCycle) -> ${r1.last()} (cycle: $r1Cycle), r2: ${r2.last()} (cycle: $r2Cycle)")
//            }

            val nextReachablePoints = reachablePoints
                .flatMap { p ->
                    Dir.entries
                        .map { d -> p.move(d) }
                        .filter { pp -> gridRange.adapted(pp) !in rocks }
                }
                .toSet()


            if (nextReachablePoints.any { it !in uni.spanning }) {
                uni = uni.expand()
                stepCycles += (step - 1)
                val stepCycle = stepCycles.last() - stepCycles.getOrElse(stepCycles.size - 2) { 0 }
                val uniCountsDescription = uni.all.map { gr -> reachablePoints.count { it in gr } }
                    .groupBy { it }
                    .entries
                    .sortedBy { it.key }
                    .joinToString(",") { "${it.key}x${it.value.size}" }
                println("x${stepCycles.size - 1}------ step $step (cycle: $stepCycle) -> grids: ${uni.all.size} - $uniCountsDescription")
            }

            reachablePoints = nextReachablePoints

//            println("\nStep $step")
//            println(gridRange.toNiceString {
//                when (gridRange.adapted(it)) {
//                    in reachablePoints -> 'O'
//                    in rocks -> '#'
//                    else -> '.'
//                }
//            })
        }

        var cycleLength = stepCycles[stepCycles.size - 1] - stepCycles[stepCycles.size - 2]


        return reachablePoints.size
    }

    fun part1(input: List<String>, steps: Int): Int {
        val garden = input.parseGarden()

        return garden.reachablePlots(steps)
    }

    fun part2(input: List<String>, steps: Int): Long {

        // 0 =>   3703 = 3703*1
        // 1 =>  32957 = 921*1 + 924*1 + 926*1 + 946*1 + 5456*1 + 5473*1 + 5480*1 + 5497*1 +                                               7334 *1
        // 2 =>  91379 = 921*2 + 924*2 + 926*2 + 946*2 + 5456*1 + 5473*1 + 5480*1 + 5497*1 + 6342*1 + 6359*1 + 6364*1 + 6388*1 + 7250 *1 + 7334 *4
        // 3 => 178969 = 921*3 + 924*3 + 926*3 + 946*3 + 5456*1 + 5473*1 + 5480*1 + 5497*1 + 6342*2 + 6359*2 + 6364*2 + 6388*2 + 7250 *4 + 7334 *9
        // 4 => 295727 = 921*4 + 924*4 + 926*4 + 946*4 + 5456*1 + 5473*1 + 5480*1 + 5497*1 + 6342*3 + 6359*3 + 6364*3 + 6388*3 + 7250 *9 + 7334*16
        // 5 => 441653 = 921*5 + 924*5 + 926*5 + 946*5 + 5456*1 + 5473*1 + 5480*1 + 5497*1 + 6342*4 + 6359*4 + 6364*4 + 6388*4 + 7250*16 + 7334*25
        // 6 => 616747 = 921*6 + 924*6 + 926*6 + 946*6 + 5456*1 + 5473*1 + 5480*1 + 5497*1 + 6342*5 + 6359*5 + 6364*5 + 6388*5 + 7250*25 + 7334*36
        // 7 => 821009 = 921*7 + 924*7 + 926*7 + 946*7 + 5456*1 + 5473*1 + 5480*1 + 5497*1 + 6342*6 + 6359*6 + 6364*6 + 6388*6 + 7250*36 + 7334*49
        fun sum(t: Int) =
            921L * t +
                    924L * t +
                    926L * t +
                    946L * t +
                    5456L +
                    5473L +
                    5480L +
                    5497L +
                    6342L * (t - 1) +
                    6359L * (t - 1) +
                    6364L * (t - 1) +
                    6388L * (t - 1) +
                    7250L * (t - 1) * (t - 1) +
                    7334L * t * t

        fun sumString(t: Int) = "${sum(t)} = " +
                "921 * $t + "

        2.let { check(sum(it) == 91379L) { sumString(it) } }
        3.let { check(sum(it) == 178969L) { sumString(it) } }
        4.let { check(sum(it) == 295727L) { sumString(it) } }
        5.let { check(sum(it) == 441653L) { sumString(it) } }
        6.let { check(sum(it) == 616747L) { sumString(it) } }
        7.let { check(sum(it) == 821009L) { sumString(it) } }

        val rest = (26501365 - 65) % 131
        val checkT = (26501365 - 65) / 131
        println(checkT)
        println(rest)
        // 675949204318352 wrong
        // 675949264300127 too high
        // 675954411822609 too high
        // 675955886994205 too high
        // 675955946976318 too high
        // 7266816063 is wrong
        println(sumString(checkT))
        return sum(checkT)

//        val garden = input.parseGarden()
//
//        return garden.reachablePlots(steps).toLong()
    }

    // TESTS
//    val testInput = readInput("$day/test")
//    val test1 = part1(testInput, steps = 6)
//    16.let { check(test1 == it) { "Test 1: is $test1, should be $it" } }
//
//    val test2 = part2(testInput, steps = 1000)
//    50L.let { check(test2 == it) { "Test 2: is $test2, should be $it" } }
//
//    val test2a = part2(testInput, steps = 50)
//    1594L.let { check(test2a == it) { "Test 2a: is $test2a, should be $it" } }
//
//    val test2b = part2(testInput, steps = 100)
//    6536L.let { check(test2a == it) { "Test 2b: is $test2b, should be $it" } }

    // RESULTS
    val input = readInput("$day/input")

    //    val part1 = part1(input, 64)
//    3617.let { println("Part 1: $part1" + if (part1 == it) "" else " (should be $it?)") }
    val part2 = part2(input, 26501365)
    println("Part 2: $part2")
}
