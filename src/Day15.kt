fun main() {
    val day = "Day15"

    // MODEL
    class Step(val label: String, val operation: Char, val focalLength: Int?)

    class Lens(val label: String, val focalLength: Int)

    class MutableBox(val iBox: Int, val lenses: MutableList<Lens> = ArrayList())

    // PARSE
    fun List<String>.parseParts() = first().split(',')

    fun String.parseStep() = if ('=' in this)
        Step(substringBefore('='), '=', last().digitToInt())
    else
        Step(substringBefore('-'), '-', null)

    // SOLVE
    fun hash(string: String) = string.fold(0) { acc, c -> (acc + c.code) * 17 % 256 }

    fun Step.boxIndex() = hash(label)

    fun Step.lens() = Lens(label, focalLength!!)

    fun MutableBox.removeLens(label: String) = lenses.removeIf { it.label == label }

    fun MutableBox.putLens(lens: Lens) {
        val iLens = lenses.indexOfFirst { it.label == lens.label }
        if (iLens >= 0) lenses[iLens] = lens else lenses += lens
    }

    fun Step.applyTo(box: MutableBox) {
        when (operation) {
            '-' -> box.removeLens(label)
            '=' -> box.putLens(lens())
            else -> throw RuntimeException()
        }
    }

    fun MutableBox.focusingPower() = lenses.mapIndexed { iLens, lens ->
        (iBox + 1) * (iLens + 1) * lens.focalLength
    }.sum()

    fun part1(input: List<String>): Int {
        return input.parseParts().sumOf { hash(it) }
    }

    fun part2(input: List<String>): Int {
        val steps = input.parseParts().map { it.parseStep() }
        val boxes = (0..255).map { i -> MutableBox(i) }

        for (step in steps) {
            val box = boxes[step.boxIndex()]
            step.applyTo(box)
        }

        return boxes.sumOf { it.focusingPower() }
    }

    // TESTS
    val test1 = part1(readInput("$day/test"))
    1320.let { check(test1 == it) { "Test 1: is $test1, should be $it" } }

    val test2 = part2(readInput("$day/test"))
    145.let { check(test2 == it) { "Test 2: is $test2, should be $it" } }

    // RESULTS
    val input = readInput("$day/input")
    val part1 = part1(input)
    509152.let { println("Part 1: $part1" + if (part1 == it) "" else " (should be $it?)") }
    val part2 = part2(input)
    println("Part 2: $part2")
}
