import java.lang.Character.isDigit
import kotlin.math.abs

fun main() {
    val day = "Day03"

    fun Point.isAdjacent(other: Point) = abs(x - other.x) <= 1 && abs(y - other.y) <= 1

    data class Number(val value: Int, val points: Set<Point>)

    fun String.parseNumbers(y: Int): List<Number> = indices
        .filter { x -> isDigit(this[x]) && (x == 0 || !isDigit(this[x - 1])) }
        .map { xStart -> (xStart..lastIndex).takeWhile { x -> isDigit(this[x]) } }
        .map { xList -> (xList.first()..xList.last()) }
        .map { xRange ->
            Number(
                value = Integer.parseInt(substring(xRange)),
                points = xRange.map { x -> Point(x, y) }.toSet()
            )
        }

    data class Symbol(val value: Char, val point: Point)

    fun String.parseSymbols(y: Int): Set<Symbol> =
        mapIndexedNotNull { x, c -> if (isDigit(c) || c == '.') null else Symbol(c, Point(x, y)) }
            .toSet()

    data class Schematic(val numbers: List<Number>, val symbols: Set<Symbol>) {
        operator fun plus(other: Schematic) = Schematic(
            numbers + other.numbers, symbols + other.symbols
        )

        fun isPartNumber(number: Number) = number.points.any { symbols.map(Symbol::point).any(it::isAdjacent) }

        fun gearRatio(symbol: Symbol): Int? = numbers
            .takeIf { symbol.value == '*' }
            ?.filter { number -> number.points.any(symbol.point::isAdjacent) }
            ?.takeIf { adjacentNumbers -> adjacentNumbers.size == 2 }
            ?.map { number -> number.value }
            ?.reduce(Int::times)
    }

    fun String.parseSchematic(y: Int) = Schematic(parseNumbers(y), parseSymbols(y))

    fun List<String>.parseSchematic() = mapIndexed { y, line -> line.parseSchematic(y) }
        .reduce(Schematic::plus)

    fun part1(input: List<String>): Int {
        val schematic = input.parseSchematic()
        return schematic.numbers
            .filter(schematic::isPartNumber)
            .sumOf { it.value }
    }

    fun part2(input: List<String>): Int {
        val schematic = input.parseSchematic()
        return schematic.symbols
            .mapNotNull(schematic::gearRatio)
            .sum()
    }

    // TESTS
    val part1 = part1(readInput("$day/test"))
    check(part1 == 4361) { "Part 1: actual=$part1" }

    val part2 = part2(readInput("$day/test"))
    check(part2 == 467835) { "Part 2: actual=$part2" }

    // RESULTS
    val input = readInput("$day/input")
    println("Part 1: " + part1(input))
    println("Part 2: " + part2(input))
}
