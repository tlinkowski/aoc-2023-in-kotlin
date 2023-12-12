//import java.util.*
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
//    data class CondRecord(
//        val springs: List<SpringType>,
//        val goodRuns: List<Int>
//    )
//
//    data class BadRunModel(val prev: BadRunModel?, val lastRunLength: Int) {
//
//    }
//
//    // PARSE
//    fun Char.toSpringType() = SpringType.entries.first { this == it.symbol }
//
//    fun String.parseRecord() = split(" ")
//        .let { (springs, runs) ->
//            CondRecord(
//                springs.map { it.toSpringType() },
//                runs.split(",").map { it.toInt() }
//            )
//        }
//
//    // SOLVE
//    fun CondRecord.matchesAt(iSpring: Int, badLen: Int, matchedRunSize: Int): Boolean {
//        val iBadLast = iSpring + badLen
//        val isMismatchOfBad = (iSpring..<iBadLast).any { springs[it] == SpringType.GOOD }
////                || springs.getOrNull(iBadLast) == SpringType.BAD
//        if (isMismatchOfBad) {
//            return false
//        }
//
//        if (matchedRunSize < goodRuns.size) {
//            val goodLen = goodRuns[matchedRunSize]
//            val iGoodLast = iBadLast + goodLen
//            val isMismatchOfGood = (iBadLast..<iGoodLast).any { springs[it] == SpringType.BAD }
//                    || springs.getOrNull(iGoodLast) == SpringType.GOOD
//            if (isMismatchOfGood) {
//                return false
//            }
//        }
//
//        return true
//    }
//
//    fun CondRecord.matchesNext(badRuns: List<Int>): Boolean {
//        val matchedRunSize = badRuns.size - 1
//        val iSpring = (0..<matchedRunSize).sumOf { i -> badRuns[i] + goodRuns[i] }
//        val badLen = badRuns.last()
//
//        return matchesAt(iSpring, badLen, matchedRunSize)
//    }
////
////    fun CondRecord.matches(badRuns: RunModel): Boolean {
////        var nextIsBad = true
////        var iBadRun = 0
////        var iGoodRun = 0
////        var iSpring = 0
////        while (if (nextIsBad) iBadRun in badRuns.runLengths.indices
////            else iGoodRun in goodRuns.runLengths.indices) {
////            if (nextIsBad) {
////                val badLen = badRuns.runLengths[iBadRun]
////                val iBadLast = iSpring + badLen
////                val isMismatch = (iSpring..<iBadLast).any { springs[it] == SpringType.GOOD }
////                        || springs.getOrNull(iBadLast) == SpringType.BAD
////                if (isMismatch) {
////                    return false
////                }
////                iSpring += badLen
////            } else {
////                val goodLen = goodRuns.runLengths[iGoodRun]
////                val iGoodLast = iSpring + goodLen
////                val isMismatch = (iSpring..<iGoodLast).any { springs[it] == SpringType.BAD }
////                        || springs.getOrNull(iGoodLast) == SpringType.GOOD
////                if (isMismatch) {
////                    return false
////                }
////                iSpring += goodLen
////                iBadRun++
////                iGoodRun++
////            }
////            nextIsBad = !nextIsBad
////        }
//////        check(iSpring == springs.size) { "$iSpring, $badRuns" }
//////        check(iBadRun == badRuns.runCount) { "$iBadRun, $badRuns" }
////        return badRuns.runLengths.isNotEmpty()
////    }
//
//    fun CondRecord.maxAllowedNext(badRuns: List<Int>): Int {
//        val maxRemainingBadRunLength = springs.size - goodRuns.sum() - badRuns.sum()
//        val missingInnerBadRuns = (goodRuns.size - 1 - badRuns.size).coerceAtLeast(0)
//
//        val result = maxRemainingBadRunLength - missingInnerBadRuns
//        check(result >= 0) { badRuns }
//        return result
//    }
//
//    fun CondRecord.isInTheMiddle(badRuns: List<Int>) =
//        badRuns.size in (1..<goodRuns.size)
//
//    fun CondRecord.extensionRange(badRuns: List<Int>) =
//        (if (isInTheMiddle(badRuns)) 1 else 0)..maxAllowedNext(badRuns)
//
//    fun CondRecord.countMatchingBadRuns(badRuns: List<Int>): Int {
//        if (badRuns.size == goodRuns.size) {
//            val lastRunLength = maxAllowedNext(badRuns)
//            val finalBadRuns = badRuns + lastRunLength
//            val matches = matchesNext(finalBadRuns)
//            if (matches) {
//                println(finalBadRuns)
//                return 1
//            }
//            return 0
//        }
//
//        val extensionRange = extensionRange(badRuns)
//        val result = extensionRange.asSequence()
//            .map { badRuns + it }
//            .filter { matchesNext(it) }
//            .sumOf { countMatchingBadRuns(it) }
////        println("$badRuns: $result - " + badRuns.runCount + "/" + (goodRuns.runCount + 1))
//        return result
//    }
//
//    fun CondRecord.countAllMatchingBadRuns(): Int {
//        return countMatchingBadRuns(LinkedList())
//    }
//
//    fun CondRecord.arrangements(): Long {
//        return countAllMatchingBadRuns().toLong()
//    }
//
//    fun CondRecord.unfold(factor: Int) = CondRecord(
//        springs = generateSequence { listOf(SpringType.UNKNOWN) + springs }
//            .take(factor).flatten().drop(1).toList(),
//        goodRuns = generateSequence { goodRuns }.take(factor).flatten().toList()
//    )
//
//    fun part1(input: List<String>): Long {
//        val records = input.map { it.parseRecord() }
//        return records.sumOf { it.arrangements() }
//    }
//
//    fun CondRecord.resultNew(): Long {
//        val once = arrangements()
//        val twice = unfold(2).arrangements()
//        val twoFactor = twice / once.toDouble()
//        val thrice = unfold(3).arrangements()
//        val threeFactor = thrice / twice.toDouble()
//        if (twoFactor == threeFactor) {
//            return (thrice * twoFactor * twoFactor).toLong()
//        }
//
//        println("long path")
//        return unfold(5).arrangements()
//    }
//
//    fun part2(input: List<String>): Long {
//        val records = input.map { it.parseRecord() }
//        return records
//            .sumOf { it.resultNew().also(::println) }
//
////        return records.parallelStream()
////            .mapToLong { it.arrangements().also(::println) }
////            .sum()
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
//    val rp1 = part1(input)
//    check(rp1 == 7916L) { rp1 }
//    println("Part 1: " + rp1)
//    println("Part 2: " + part2(input))
//
//    // 22866290980758 too low
//    // 22883340011284 too low
//    // 22934723060793 too low
//    // 22964858892816 too low
//}
