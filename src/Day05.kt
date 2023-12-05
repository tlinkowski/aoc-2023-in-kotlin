fun main() {
    val day = "Day05"

    data class Mapper(
        val ranges: Map<LongRange, Long>
    ) {
        fun mapId(id: Long) = ranges
            .firstNotNullOfOrNull { (range, offset) -> if (range.contains(id)) id + offset else null }
            ?: id
    }

    data class Almanac(
        val seeds: List<Long>,
        val mappers: List<Mapper>
    ) {
        fun locationId(seedId: Long) = mappers
            .fold(seedId) { id, mapper -> mapper.mapId(id) }

        fun lowestLocationId() = seeds.minOf { locationId(it) }
    }

    data class Almanac2(
        val seedRanges: List<LongRange>,
        val mappers: List<Mapper>
    ) {
        fun locationId(seedId: Long) = mappers
            .fold(seedId) { id, mapper -> mapper.mapId(id) }

        fun lowestLocationId(): Long = seedRanges.parallelStream()
            .peek { println("Finding min location for seeds $it") }
            .mapToLong { range -> range.minOf { locationId(it) } }
            .peek { println("Found min location: $it") }
            .min().orElseThrow()
    }

    fun parseMapper(part: String) = Mapper(
        part.split("\n").drop(1)
            .map { line -> line.split(" ") }
            .associate { lineParts ->
                val destRangeStart = lineParts[0].toLong()
                val sourceRangeStart = lineParts[1].toLong()
                val length = lineParts[2].toLong()
                (sourceRangeStart..<sourceRangeStart + length) to destRangeStart - sourceRangeStart
            }
    )

    fun parseAlmanac(input: List<String>): Almanac {
        val text = input.joinToString("\n")
        val parts = text.split("\n\n")
        val seeds = parts[0].substringAfter(": ").split(" ")
            .map { it.toLong() }
        val mappers = parts.drop(1)
            .map { parseMapper(it) }
        return Almanac(seeds, mappers)
    }

    fun parseAlmanac2(input: List<String>): Almanac2 {
        val text = input.joinToString("\n")
        val parts = text.split("\n\n")
        val seedRanges = parts[0].substringAfter(": ").split(" ")
            .map { it.toLong() }
            .chunked(2)
            .map { (it[0]..<it[0] + it[1]) }
        val mappers = parts.drop(1)
            .map { parseMapper(it) }
        return Almanac2(seedRanges, mappers)
    }

    fun part1(input: List<String>): Int {
        val almanac = parseAlmanac(input)
        return almanac.lowestLocationId().toInt()
    }

    fun part2(input: List<String>): Long {
        val almanac = parseAlmanac2(input)
        println("${almanac.seedRanges.size} seed ranges")
        return almanac.lowestLocationId()
    }

    // TESTS
    val part1 = part1(readInput("$day/test"))
    check(part1 == 35) { "Part 1: actual=$part1" }

    val part2 = part2(readInput("$day/test"))
    check(part2 == 46L) { "Part 2: actual=$part2" }

    // RESULTS
    val input = readInput("$day/input")
    println("Part 1: " + part1(input))
    println("Part 2: " + part2(input))
}
