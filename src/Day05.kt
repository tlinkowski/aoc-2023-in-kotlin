fun main() {
    val day = "Day05"

    // MODEL
    class Mapper(val mapperRanges: Map<LongRange, Long>)

    class Almanac(val seedIds: List<Long>, val mappers: List<Mapper>)

    class Almanac2(val seedIdRanges: List<LongRange>, val mappers: List<Mapper>)

    // PARSE
    fun String.parseMapper(): Mapper = split("\n")
        .drop(1)
        .map { line -> line.toLongs() }
        .associate { (destStart, sourceStart, length) ->
            (sourceStart..<sourceStart + length) to destStart - sourceStart
        }.let(::Mapper)

    fun parseAlmanac(input: List<String>): Almanac {
        val parts = input.joinToString("\n").split("\n\n")
        val seedIds = parts[0].substringAfter(": ").toLongs()
        val mappers = parts.drop(1).map { it.parseMapper() }
        return Almanac(seedIds, mappers)
    }

    fun parseAlmanac2(input: List<String>): Almanac2 {
        val parts = input.joinToString("\n").split("\n\n")
        val seedIdRanges = parts[0].substringAfter(": ").toLongs()
            .chunked(2) { (start, length) -> start..<start + length }
        val mappers = parts.drop(1).map { it.parseMapper() }
        return Almanac2(seedIdRanges, mappers)
    }

    // SOLVE
    fun Mapper.offsetForId(id: Long): Long = mapperRanges
        .filterKeys { mapperRange -> id in mapperRange }
        .values
        .singleOrNull() ?: 0

    fun Mapper.offsetForIdSubrange(idSubrange: LongRange): Long = mapperRanges
        .filterKeys { mapperRange -> idSubrange in mapperRange }
        .values
        .singleOrNull() ?: 0

    fun LongRange.boundaries() = listOf(first, last + 1)

    fun LongRange.splitOnRanges(ranges: Collection<LongRange>): List<LongRange> = ranges.asSequence()
        .flatMap { it.boundaries() }
        .filter { i -> i in this }
        .plus(boundaries())
        .distinct()
        .sorted()
        .zipWithNext { i, j -> i..<j }
        .filter { !it.isEmpty() }
        .toList()

    fun Mapper.mapId(id: Long): Long = id + offsetForId(id)

    fun Mapper.mapIdRange(idRange: LongRange): List<LongRange> {
        return idRange.splitOnRanges(mapperRanges.keys)
            .map { idSubrange -> idSubrange.offsetBy(offsetForIdSubrange(idSubrange)) }
    }

    fun Almanac.locationId(seedId: Long): Long = mappers
        .fold(seedId) { id, mapper -> mapper.mapId(id) }

    fun Almanac.lowestLocationId(): Long = seedIds.minOf { locationId(it) }

    fun Almanac2.lowestLocationId(): Long = mappers
        .fold(seedIdRanges) { idRanges, mapper -> idRanges.flatMap(mapper::mapIdRange) }
        .minOf { it.first }

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
