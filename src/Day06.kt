fun main() {
    val day = "Day06"

    // 1 ms pressing -> 1 mm/ms acceleration
    data class Strategy(
        val raceTime: Long,
        val pressingTime: Long
    ) {
        val acceleration = pressingTime

        val distance = (raceTime - pressingTime) * acceleration
    }

    data class Record(
        val raceTime: Long,
        val recordDistance: Long
    ) {

        fun winningStrategyCount() = 1 + findHighBeatingStrategy().pressingTime - findLowBeatingStrategy().pressingTime

        private fun findLowBeatingStrategy(): Strategy = (1..<raceTime)
            .asSequence()
            .map { pressingTime -> Strategy(raceTime, pressingTime) }
            .first { it.distance > recordDistance }

        private fun findHighBeatingStrategy(): Strategy = (1..<raceTime)
            .reversed()
            .asSequence()
            .map { pressingTime -> Strategy(raceTime, pressingTime) }
            .first { it.distance > recordDistance }
    }

    fun parseRecords(input: List<String>) = input.let { (timeStr, distanceStr) ->
        val times = timeStr.substringAfter(":").toLongs()
        val distances = distanceStr.substringAfter(":").toLongs()
        times.zip(distances) { time, distance -> Record(time, distance) }
    }

    fun String.toJointLong() = replace(" ", "").toLong()

    fun parseRecord2(input: List<String>) = input.let { (timeStr, distanceStr) ->
        val time = timeStr.substringAfter(":").toJointLong()
        val distance = distanceStr.substringAfter(":").toJointLong()
        Record(time, distance)
    }

    fun part1(input: List<String>): Long {
        val records = parseRecords(input)
        return records
            .map { it.winningStrategyCount() }
            .reduce(Long::times)
    }

    fun part2(input: List<String>): Long {
        val record = parseRecord2(input)
        return record.winningStrategyCount()
    }

    // TESTS
    val part1 = part1(readInput("$day/test"))
    check(part1 == 288L) { "Part 1: actual=$part1" }

    val part2 = part2(readInput("$day/test"))
    check(part2 == 71503L) { "Part 2: actual=$part2" }

    // RESULTS
    val input = readInput("$day/input")
    println("Part 1: " + part1(input))
    println("Part 2: " + part2(input))
}
