//import java.util.Collections.nCopies
//
//enum class SpringType(val symbol: Char) {
//    GOOD('#'),
//    BAD('.'),
//    UNKNOWN('?')
//}
//
//enum class RunType { GOOD, BAD }
//
//fun main() {
//    val day = "Day12"
//
//    // MODEL
//    data class RunModel(val runLengths: List<Int>) {
//        val runCount
//            get() = runLengths.size
//
//        val totalRunLength = runLengths.sum()
//    }
//
//    data class Run(val length: Int, val type: RunType)
//
//    data class CondRecord(
//        val springs: List<SpringType>,
//        val goodRuns: RunModel
//    )
//
//    // PARSE
//    fun Char.toSpringType() = SpringType.entries.first { this == it.symbol }
//
//    fun String.parseRecord() = split(" ")
//        .let { (springs, runs) ->
//            CondRecord(
//                springs.map { it.toSpringType() },
//                RunModel(runs.split(",").map { it.toInt() })
//            )
//        }
//
//    // SOLVE
//
//    fun RunType.matches(o: SpringType) = when(this) {
//        RunType.GOOD -> o != SpringType.BAD
//        RunType.BAD -> o != SpringType.GOOD
//    }
//
//
//    fun CondRecord.sizing(badRuns: RunModel) =
//        springs.size - (badRuns.totalRunLength + goodRuns.totalRunLength)
//
//    fun CondRecord.initialBadRunModel(): RunModel {
//        val innerBadRunCount = goodRuns.runCount - 1
//        return RunModel(
//            listOf(springs.size - goodRuns.totalRunLength - innerBadRunCount)
//                    + nCopies(innerBadRunCount, 1)
//                    + listOf(0)
//        )
//    }
//
//    fun CondRecord.matches(badRuns: RunModel): Boolean {
//        var nextIsBad = true
//        var iBadRun = 0
//        var iGoodRun = 0
//        var iSpring = 0
//        while (iBadRun in badRuns.runLengths.indices
//            && iGoodRun in goodRuns.runLengths.indices) {
//            if (nextIsBad) {
//                val badLen = badRuns.runLengths[iBadRun]
//                val iBadLast = iSpring + badLen
//                val isMismatch = (iSpring..<iBadLast).any { springs[it] == SpringType.GOOD }
//                        || springs.getOrNull(iBadLast) == SpringType.BAD
//                if (isMismatch) {
//                    return false
//                }
//                iSpring += badLen
//            } else {
//                val goodLen = goodRuns.runLengths[iGoodRun]
//                val iGoodLast = iSpring + goodLen
//                val isMismatch = (iSpring..<iGoodLast).any { springs[it] == SpringType.BAD }
//                        || springs.getOrNull(iGoodLast) == SpringType.GOOD
//                if (isMismatch) {
//                    return false
//                }
//                iSpring += goodLen
//                iBadRun++
//                iGoodRun++
//            }
//            nextIsBad = !nextIsBad
//        }
////        check(iSpring == springs.size) { "$iSpring, $badRuns" }
////        check(iBadRun == badRuns.runCount - 1) { "$iBadRun, $badRuns" }
//        return badRuns.runLengths.isNotEmpty()
//    }
//
//    fun CondRecord.findMatchingBadRunCount(badRuns: RunModel): Int {
//        var nextIsBad = true
//        var iBadRun = 0
//        var iGoodRun = 0
//        var iSpring = 0
//        while (iBadRun in badRuns.runLengths.indices) {
//            if (nextIsBad) {
//                val badLen = badRuns.runLengths[iBadRun]
//                val iBadLast = iSpring + badLen
//                val isMismatch = (iSpring..<iBadLast).any { springs[it] == SpringType.GOOD }
//                        || springs.getOrNull(iBadLast) == SpringType.BAD
//                if (isMismatch) {
//                    return iBadRun
//                }
//                iSpring += badLen
//            } else {
//                val goodLen = goodRuns.runLengths[iGoodRun]
//                val iGoodLast = iSpring + goodLen
//                val isMismatch = (iSpring..<iGoodLast).any { springs[it] == SpringType.BAD }
//                        || springs.getOrNull(iGoodLast) == SpringType.GOOD
//                if (isMismatch) {
//                    return iBadRun
//                }
//                iSpring += goodLen
//                iBadRun++
//                iGoodRun++
//            }
//            nextIsBad = !nextIsBad
//        }
////        check(iSpring == springs.size) { "$iSpring, $badRuns" }
//        check(iBadRun == badRuns.runCount - 1) { "$iBadRun, $badRuns" }
//        return badRuns.runCount
//    }
//
//    fun RunModel.incrementAtIndex(i: Int): RunModel {
//        val newLengths = runLengths.toMutableList()
//        newLengths[i]++
//        return RunModel(newLengths)
//    }
//
//    fun CondRecord.toNiceString(badRuns: RunModel): String {
//        val sb = StringBuilder()
//
//        return sb.toString()
//    }
//
//    fun CondRecord.maxLeft(badRuns: RunModel) =
//        springs.size - (badRuns.totalRunLength + goodRuns.totalRunLength)
//
//    fun CondRecord.isInTheMiddle(badRuns: RunModel) =
//        badRuns.runCount in (1..<goodRuns.runCount)
//
//    fun CondRecord.extensionRange(badRuns: RunModel) =
//        (if (isInTheMiddle(badRuns)) 1 else 0)..maxLeft(badRuns)
//
//    fun RunModel.extendWithRun(runLength: Int) = RunModel(runLengths + runLength)
//
//    fun CondRecord.countMatchingBadRuns(badRuns: RunModel): Int {
//        if (badRuns.runCount == goodRuns.runCount) {
////            println("$badRuns: 1 - " + badRuns.runCount + "/" + (goodRuns.runCount + 1))
//            val matches = matches(badRuns.extendWithRun(maxLeft(badRuns)))
//            return if (matches) 1 else 0
//        }
//
//        val extensionRange = extensionRange(badRuns)
//        val result = extensionRange.asSequence()
//            .map { badRuns.extendWithRun(it) }
//            .filter { matches(it) }
//            .sumOf { countMatchingBadRuns(it) }
////        println("$badRuns: $result - " + badRuns.runCount + "/" + (goodRuns.runCount + 1))
//        return result
//    }
//
//    fun CondRecord.countAllMatchingBadRuns(): Int {
//        return countMatchingBadRuns(RunModel(emptyList()))
//    }
//
//    fun CondRecord.arrangements(): Long {
//        return countAllMatchingBadRuns().toLong()
//    }
//
//    fun CondRecord.unfold() = CondRecord(
//        springs = generateSequence { listOf(SpringType.UNKNOWN) + springs }
//            .take(5).flatten().drop(1).toList(),
//        goodRuns = RunModel(generateSequence { goodRuns.runLengths }.take(5).flatten().toList())
//    )
//
//    fun part1(input: List<String>): Long {
//        val records = input.map { it.parseRecord() }
//        return records.sumOf { it.arrangements() }
//    }
//
//    fun part2(input: List<String>): Long {
//        val records = input.map { it.parseRecord() }.map { it.unfold() }
//        return records.parallelStream()
//            .mapToLong { it.arrangements().also(::println) }
//            .sum()
//    }
//
//    // TESTS
////    val part1 = part1(readInput("$day/test"))
////    check(part1 == 21L) { "Part 1: actual=$part1" }
//
//    val part2 = part2(readInput("$day/test"))
//    check(part2 == 525152L) { "Part 2: actual=$part2" }
//    println("Here we go!")
//
//    // RESULTS
//    val input = readInput("$day/input")
//    println("Part 1: " + part1(input))
//    println("Part 2: " + part2(input))
//}
