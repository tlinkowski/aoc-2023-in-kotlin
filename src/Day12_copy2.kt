//import java.util.Collections.nCopies
//import kotlin.math.min
//
//enum class SpringType(val symbol: Char) {
//    GOOD('#'),
//    BAD('.'),
//    UNKNOWN('?')
//}
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
////    data class DoubleRunModel( )
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
//    fun CondRecord.sizing(badRuns: RunModel) =
//        springs.size - (badRuns.totalRunLength + goodRuns.totalRunLength)
//
//    fun CondRecord.startingBadRunModel(): RunModel = RunModel(
//        listOf(0) + nCopies(goodRuns.runCount - 1, 1) + listOf(0)
//    )
//
//    fun CondRecord.findMatchingBadRunCount(badRuns: RunModel): Int {
////        check(sizing(badRuns) >= 0) { sizing(badRuns) }
//
//        var nextIsBad = true
//        var iBadRun = -1
//        var iGoodRun = 0
//        var iSpring = 0
//        while (iBadRun < badRuns.runCount - 1) {
////        while (iSpring < springs.size) {
//            if (nextIsBad) {
//                val badLen = badRuns.runLengths[++iBadRun]
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
//                iGoodRun++
//            }
//            nextIsBad = !nextIsBad
//        }
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
//    class Finder(private val r: CondRecord) {
//        private val matches = HashSet<RunModel>()
//
//        fun result(badRuns: RunModel): Int {
//            expandBadRuns(badRuns)
//            return matches.size
//        }
//
//        fun expandBadRuns(badRuns: RunModel) {
//            val sizing = r.sizing(badRuns)
//            check (sizing >= 0) { sizing }
//            val matchingBadRunCount = r.findMatchingBadRunCount(badRuns)
//            println("Expanding $badRuns -> $matchingBadRunCount/${badRuns.runCount}")
//            if (sizing == 0 && matchingBadRunCount == badRuns.runCount) {
//                matches += badRuns
//                return
//            }
//
//            if (sizing > 0) {
//                (0..matchingBadRunCount.coerceAtMost(badRuns.runCount - 1)).reversed()
//                    .forEach { i -> expandBadRuns(badRuns.incrementAtIndex(i)) }
//            }
//        }
//    }
//
//    fun CondRecord.arrangements(): Long {
//        println("Solving $this")
//        return Finder(this).result(startingBadRunModel()).toLong().also(::println)
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
//        return records.sumOf { it.arrangements() }
//    }
//
//    // TESTS
//    val part1 = part1(readInput("$day/test"))
//    check(part1 == 21L) { "Part 1: actual=$part1" }
//
//    val part2 = part2(readInput("$day/test"))
//    check(part2 == 525152L) { "Part 2: actual=$part2" }
//    println("Here we go!")
//
//    // RESULTS
//    val input = readInput("$day/input")
////    println("Part 1: " + part1(input))
//    println("Part 2: " + part2(input))
//}
