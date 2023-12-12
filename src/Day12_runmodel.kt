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
//    fun CondRecord.matchesNext(badRuns: RunModel): Boolean {
//        val checkedRunCount = badRuns.runCount - 1
//        var iSpring = (0..<checkedRunCount).sumOf { i ->
//            badRuns.runLengths[i] + goodRuns.runLengths[i]
//        }
//
//        val badLen = badRuns.runLengths.last()
//        val iBadLast = iSpring + badLen
//        val isMismatchOfBad = (iSpring..<iBadLast).any { springs[it] == SpringType.GOOD }
//                || springs.getOrNull(iBadLast) == SpringType.BAD
//        if (isMismatchOfBad) {
//            return false
//        }
//        iSpring += badLen
//
//        if (checkedRunCount < goodRuns.runCount) {
//            val goodLen = goodRuns.runLengths[checkedRunCount]
//            val iGoodLast = iSpring + goodLen
//            val isMismatchOfGood = (iSpring..<iGoodLast).any { springs[it] == SpringType.BAD }
//                    || springs.getOrNull(iGoodLast) == SpringType.GOOD
//            if (isMismatchOfGood) {
//                return false
//            }
//        }
//
//        return true
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
//    fun CondRecord.maxAllowedNext(badRuns: RunModel): Int {
//        val maxRemainingBadRunLength = springs.size - goodRuns.totalRunLength - badRuns.totalRunLength
//        val missingInnerBadRuns = (goodRuns.runCount - 1 - badRuns.runCount).coerceAtLeast(0)
//
//        val result = maxRemainingBadRunLength - missingInnerBadRuns
//        check(result >= 0) { badRuns }
//        return result
//    }
//
//    fun CondRecord.isInTheMiddle(badRuns: RunModel) =
//        badRuns.runCount in (1..<goodRuns.runCount)
//
//    fun CondRecord.extensionRange(badRuns: RunModel) =
//        (if (isInTheMiddle(badRuns)) 1 else 0)..maxAllowedNext(badRuns)
//
//    fun RunModel.extendWithRun(runLength: Int) = RunModel(runLengths + runLength)
//
//    fun CondRecord.countMatchingBadRuns(badRuns: RunModel): Int {
//        if (badRuns.runCount == goodRuns.runCount) {
//            val lastRunLength = maxAllowedNext(badRuns)
//            val lastBadRuns = badRuns.extendWithRun(lastRunLength)
//            val matches = matchesNext(lastBadRuns)
//            if (matches) {
////                println(nextBadRuns)
//                return 1
//            }
//            return 0
//        }
//
//        val extensionRange = extensionRange(badRuns)
//        val result = extensionRange.asSequence()
//            .map { badRuns.extendWithRun(it) }
//            .filter { matchesNext(it) }
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
//    fun CondRecord.unfold(factor: Int) = CondRecord(
//        springs = generateSequence { listOf(SpringType.UNKNOWN) + springs }
//            .take(factor).flatten().drop(1).toList(),
//        goodRuns = RunModel(generateSequence { goodRuns.runLengths }.take(factor).flatten().toList())
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
