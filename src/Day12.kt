import java.util.*

enum class SpringType(val symbol: Char) {
    GOOD('#'),
    BAD('.'),
    UNKNOWN('?')
}

fun main() {
    val day = "Day12"

    // MODEL
    data class CondRecord(
        val springs: List<SpringType>,
        val goodRuns: List<Int>
    )

    data class BadRunModel(
        val size: Int, // number of "gaps" (runs of symbol ".")
        val sum: Int // number of "." symbols
    )

    // PARSE
    fun Char.toSpringType() = SpringType.entries.first { this == it.symbol }

    fun String.parseRecord() = split(" ")
        .let { (springs, runs) ->
            CondRecord(
                springs.map { it.toSpringType() },
                runs.split(",").map { it.toInt() }
            )
        }

    // SOLVE
    class Finder {

        private val results = HashMap<BadRunModel, Long>()

        fun CondRecord.matchesNext(iSpring: Int, badLen: Int, goodLen: Int?): Boolean {
            val iBadLast = iSpring + badLen
            val isMismatchOfBad = (iSpring..<iBadLast).any { springs[it] == SpringType.GOOD }
            if (isMismatchOfBad) {
                return false
            }

            if (goodLen != null) {
                val iGoodLast = iBadLast + goodLen
                val isMismatchOfGood = (iBadLast..<iGoodLast).any { springs[it] == SpringType.BAD }
                        || springs.getOrNull(iGoodLast) == SpringType.GOOD
                if (isMismatchOfGood) {
                    return false
                }
            }

            return true
        }

        fun CondRecord.matchesNext(matchedRuns: BadRunModel, badLen: Int): Boolean {
            val iSpring = matchedRuns.sum + (0..<matchedRuns.size).sumOf { goodRuns[it] }
            val goodLen = goodRuns.getOrNull(matchedRuns.size)

            return matchesNext(iSpring, badLen, goodLen)
        }

        fun CondRecord.maxAllowedNext(badRuns: BadRunModel): Int {
            val maxRemainingBadRunLength = springs.size - goodRuns.sum() - badRuns.sum
            val missingInnerBadRuns = (goodRuns.size - 1 - badRuns.size).coerceAtLeast(0)

            val result = maxRemainingBadRunLength - missingInnerBadRuns
            check(result >= 0) { badRuns }
            return result
        }

        fun CondRecord.isInTheMiddle(badRuns: BadRunModel) =
            badRuns.size in (1..<goodRuns.size)

        fun CondRecord.extensionRange(badRuns: BadRunModel) =
            (if (isInTheMiddle(badRuns)) 1 else 0)..maxAllowedNext(badRuns)

        operator fun BadRunModel.plus(runLength: Int) = BadRunModel(
            size = size + 1,
            sum = sum + runLength
        )

        fun CondRecord.countMatchingBadRuns(badRuns: BadRunModel): Long {
            if (badRuns.size == goodRuns.size) {
                val lastRunLength = maxAllowedNext(badRuns)
//                val finalBadRuns = badRuns + lastRunLength
                return if (matchesNext(badRuns, lastRunLength)) 1 else 0
            }

            var result = 0L
            extensionRange(badRuns).forEach { runLength ->
                if (matchesNext(badRuns, runLength)) {
                    val nextBadRuns = badRuns + runLength
                    var subresult = results[nextBadRuns]
                    if (subresult == null) {
                        subresult = countMatchingBadRuns(nextBadRuns)
                        results[nextBadRuns] = subresult
                    }
                    result += subresult
                }
            }

//        println("$badRuns: $result - " + badRuns.runCount + "/" + (goodRuns.runCount + 1))
            return result
        }

        fun countAllMatchingBadRuns(r: CondRecord): Long {
            return r.countMatchingBadRuns(BadRunModel(0, 0))
        }
    }

    fun CondRecord.arrangements(): Long {
        return Finder().countAllMatchingBadRuns(this)
    }

    fun CondRecord.unfold(factor: Int) = CondRecord(
        springs = generateSequence { listOf(SpringType.UNKNOWN) + springs }
            .take(factor).flatten().drop(1).toList(),
        goodRuns = generateSequence { goodRuns }.take(factor).flatten().toList()
    )

    fun part1(input: List<String>): Long {
        val records = input.map { it.parseRecord() }
        return records.sumOf { it.arrangements() }
    }

    fun part2(input: List<String>): Long {
        val records = input.map { it.parseRecord().unfold(5) }
        return records.sumOf { it.arrangements().also(::println) }
    }

    // TESTS
    val part1 = part1(readInput("$day/test"))
    check(part1 == 21L) { "Part 1: actual=$part1" }

    val part2 = part2(readInput("$day/test"))
    check(part2 == 525152L) { "Part 2: actual=$part2" }
    println("Here we go!")

    // RESULTS
    val input = readInput("$day/input")
    val rp1 = part1(input)
    check(rp1 == 7916L) { rp1 }
    println("Part 1: " + rp1)
    println("Part 2: " + part2(input))

    // 26442227262 way too low
    // 22866290980758 too low
    // 22883340011284 too low
    // 22934723060793 too low
    // 22964858892816 too low
}
