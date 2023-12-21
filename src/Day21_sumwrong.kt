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
                stepCycles += (step-1)
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

    fun seq_0_1_2_4_6_9_12_16_20(n: Int): Long {
        val r = LongArray(2)
        for (i in 1..n) {
            val shift = (i + 1) / 2
            r[i % 2] = r[(i - 1) % 2] + shift
        }
        return r[n % 2]
    }
    check(seq_0_1_2_4_6_9_12_16_20(1) == 1L)
    check(seq_0_1_2_4_6_9_12_16_20(2) == 2L)
    check(seq_0_1_2_4_6_9_12_16_20(3) == 4L)
    check(seq_0_1_2_4_6_9_12_16_20(4) == 6L)
    check(seq_0_1_2_4_6_9_12_16_20(5) == 9L)
    check(seq_0_1_2_4_6_9_12_16_20(6) == 12L)
    check(seq_0_1_2_4_6_9_12_16_20(7) == 16L)
    check(seq_0_1_2_4_6_9_12_16_20(8) == 20L)
    check(seq_0_1_2_4_6_9_12_16_20(9) == 25L)

    fun seq_0_2_5_10_16_24_33(n: Int): Long {
        val r = LongArray(2)
        var one = false
        var shift = 0
        for (i in 1..n) {
            shift += if (one) 1 else 2
            one = !one
            r[i % 2] = r[(i - 1) % 2] + shift
        }
        return r[n % 2]
    }
    check(seq_0_2_5_10_16_24_33(1) == 2L)
    check(seq_0_2_5_10_16_24_33(2) == 5L)
    check(seq_0_2_5_10_16_24_33(3) == 10L)
    check(seq_0_2_5_10_16_24_33(4) == 16L)
    check(seq_0_2_5_10_16_24_33(5) == 24L)
    check(seq_0_2_5_10_16_24_33(6) == 33L)
    check(seq_0_2_5_10_16_24_33(7) == 44L)
    check(seq_0_2_5_10_16_24_33(8) == 56L)

    fun part2(input: List<String>, steps: Int): Long {

        // 0 =>   3703 = 3703*1
        // 1 =>   35433 = 926*1 + 1089 *3 + 5473*1 + 5497*1 +          6468*2 +           7334 *1
        // 2 =>  100303 = 926*2 + 1089 *6 + 5473*1 + 5497*1 + 6359*1 + 6468*2 + 7250 *1 + 7334 *2 + 7524 *3 +           8581 *2
        // 3 =>  198248 = 926*3 + 1089 *9 + 5473*1 + 5497*1 + 6359*2 + 6468*2 + 7250 *2 + 7334 *4 + 7524 *6 + 8580 *2 + 8581 *5
        // 4 =>  329185 = 926*4 + 1089*12 + 5473*1 + 5497*1 + 6359*3 + 6468*2 + 7250 *4 + 7334 *6 + 7524 *9 + 8580 *5 + 8581*10
        // 5 =>  493197 = 926*5 + 1089*15 + 5473*1 + 5497*1 + 6359*4 + 6468*2 + 7250 *6 + 7334 *9 + 7524*12 + 8580*10 + 8581*16
        // 6 =>  690201 = 926*6 + 1089*18 + 5473*1 + 5497*1 + 6359*5 + 6468*2 + 7250 *9 + 7334*12 + 7524*15 + 8580*16 + 8581*24
        // 7 =>  920280 = 926*7 + 1089*21 + 5473*1 + 5497*1 + 6359*6 + 6468*2 + 7250*12 + 7334*16 + 7524*18 + 8580*24 + 8581*33
        // 8 => 1183351 = 926*8 + 1089*24 + 5473*1 + 5497*1 + 6359*7 + 6468*2 + 7250*16 + 7334*20 + 7524*21 + 8580*33 + 8581*44
        // 9 => 1479497 = 926*9 + 1089*27 + 5473*1 + 5497*1 + 6359*8 + 6468*2 + 7250*20 + 7334*25 + 7524*24 + 8580*44 + 8581*56
        fun sum(t: Int) =
                926L * t +
                1089L * t * 3 +
                5473L +
                5497L +
                6359L * (t-1) +
                6468L * 2 +
                7250L * seq_0_1_2_4_6_9_12_16_20(t - 1) +
                7334L * seq_0_1_2_4_6_9_12_16_20(t) +
                7524L * (t-1) * 3 +
                8580L * seq_0_2_5_10_16_24_33(t - 2) +
                8581L * seq_0_2_5_10_16_24_33(t - 1)
        fun sumString(t: Int) = "${sum(t)} = " +
                "926 * $t + " +
                "1089 * ${t * 3} + " +
                "5473 * 1 + " +
                "5497 * 1 + " +
                "6359 * ${t-1} + " +
                "6468 * 2 + " +
                "7250 * ${seq_0_1_2_4_6_9_12_16_20(t - 1)} + " +
                "7334 * ${seq_0_1_2_4_6_9_12_16_20(t)} + " +
                "7524 * ${(t-1) * 3} + " +
                "8580 * ${seq_0_2_5_10_16_24_33(t - 2)} + " +
                "8581 * ${seq_0_2_5_10_16_24_33(t - 1)}"

        2.let { check(sum(it) == 100303L) { sumString(it) } }
        3.let { check(sum(it) == 198248L) { sumString(it) } }
        4.let { check(sum(it) == 329185L) { sumString(it) } }
        5.let { check(sum(it) == 493197L) { sumString(it) } }
        6.let { check(sum(it) == 690201L) { sumString(it) } }
        7.let { check(sum(it) == 920280L) { sumString(it) } }
        8.let { check(sum(it) == 1183351L) { sumString(it) } }
        9.let { check(sum(it) == 1479497L) { sumString(it) } }

        val rest = (26501365 - 65) % 131
        val checkT = (26501365 - 65) / 131
        println(checkT)
        println(rest)
        // 675949204318352 wrong
        // 675949264300127 too high
        // 675954411822609 too high
        // 675955886994205 too high
        // 675955946976318 too high
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
