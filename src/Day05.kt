fun main() {
    val day = "Day05"

    data class Mapper(
        val mapperRanges: Map<LongRange, Long>
    ) {
        fun mapId(id: Long) = id + offsetForId(id)

        fun mapRange(inputRange: LongRange): List<LongRange> {
            return splitInputRangeIntoPartsMatchingMapperRanges(inputRange)
                .map { part -> part.offsetBy(offsetForPart(part)) }
        }

        private fun offsetForId(id: Long) = mapperRanges
            .filterKeys { it.contains(id) }
            .values
            .singleOrNull() ?: 0

        private fun offsetForPart(inputRangePart: LongRange) = mapperRanges
            .filterKeys { it.contains(inputRangePart) }
            .values
            .singleOrNull() ?: 0

        private fun splitInputRangeIntoPartsMatchingMapperRanges(inputRange: LongRange) = mapperRanges.keys
            .fold(listOf(inputRange)) { parts, mapperRange ->
                parts.flatMap { part -> part.splitUsing(mapperRange) }
            }

        private fun LongRange.splitUsing(other: LongRange): List<LongRange> {
            val intersection = intersect(other)
            if (intersection.isEmpty()) {
                return listOf(this)
            }

            return listOf(
                LongRange(start = start, endInclusive = intersection.start - 1),
                intersection,
                LongRange(start = intersection.endInclusive + 1, endInclusive = endInclusive)
            ).filter { !it.isEmpty() }
        }
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
        fun lowestLocationId(): Long {
            return mappers
                .fold(seedRanges) { ranges, mapper -> ranges.flatMap(mapper::mapRange) }
                .minOf { it.first }
        }
    }

    fun parseMapper(part: String) = Mapper(
        part.split("\n").drop(1)
            .map { line -> line.split(" ").map { it.toLong() } }
            .associate { (destStart, sourceStart, length) ->
                (sourceStart..<sourceStart + length) to destStart - sourceStart
            }
    )

    fun parseAlmanac(input: List<String>): Almanac {
        val text = input.joinToString("\n")
        val parts = text.split("\n\n")
        val seeds = parts[0].substringAfter(": ").toLongs()
        val mappers = parts.drop(1)
            .map { parseMapper(it) }
        return Almanac(seeds, mappers)
    }

    fun parseAlmanac2(input: List<String>): Almanac2 {
        val text = input.joinToString("\n")
        val parts = text.split("\n\n")
        val seedRanges = parts[0].substringAfter(": ").toLongs()
            .chunked(2)
            .map { (start, length) -> start..<start + length }
        val mappers = parts.drop(1)
            .map { parseMapper(it) }
        return Almanac2(seedRanges, mappers)
    }

    fun part1(input: List<String>): Long {
        val almanac = parseAlmanac(input)
        return almanac.lowestLocationId()
    }

    fun part2(input: List<String>): Long {
        val almanac = parseAlmanac2(input)
        return almanac.lowestLocationId()
    }

    // TESTS
    val part1 = part1(readInput("$day/test"))
    check(part1 == 35L) { "Part 1: actual=$part1" }

    val part2 = part2(readInput("$day/test"))
    check(part2 == 46L) { "Part 2: actual=$part2" }

    // RESULTS
    val input = readInput("$day/input")
    println("Part 1: " + part1(input))
    println("Part 2: " + part2(input))
}
