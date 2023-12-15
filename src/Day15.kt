fun main() {
    val day = "Day15"

    // MODEL
    data class Step(val label: String, val operation: Char, val focalLength: Int?)

    data class Lens(val label: String, val focalLength: Int)

    data class Box(val no: Int, val lenses: MutableList<Lens> = ArrayList())

    // PARSE
    fun String.parseSteps() = split(",")
        .map { if('=' in it) Step(it.substringBefore('='), '=', it.substringAfter('=').toInt())
            else Step(it.substringBefore('-'), '-', null) }

    // SOLVE
    fun hash(s: String) = s
        .fold(0) {acc, c -> ((acc + c.code) * 17) % 256 }

    fun Step.boxNo() = hash(label)

    fun Step.lens() = focalLength?.let { Lens(label, it) }

    fun Step.shouldRemove() = operation == '-'

    fun Box.remove(label: String) {
        lenses.removeIf { it.label == label }
    }

    fun Box.focusingPower() = lenses.mapIndexed {
        iLens, lens -> (no + 1) * (iLens + 1) * lens.focalLength.toLong()
    }.sum()

    // If the operation character is a dash (-), go to the relevant box
    // and REMOVE the lens with the given label if it is present in the box.
    // Then, move any remaining lenses as far forward in the box as they can
    // go without changing their order, filling any space made by removing the
    // indicated lens. (If no lens in that box has the given label, nothing
    // happens.)

    fun part1(input: List<String>): Long {
        return input[0].split(",").sumOf { hash(it).toLong() }
    }

    fun part2(input: List<String>): Long {
        val steps = input[0].parseSteps()
        val boxes = (0..255).map { i -> Box(i)}
        for (step in steps) {
            val iBox = step.boxNo()
            val box = boxes[iBox]
            if (step.shouldRemove()) {
                box.remove(step.label)
            } else {
                val lens = step.lens()!!
                val iLens = box.lenses.indexOfFirst { it.label == lens.label }
                if (iLens >= 0) {
                    box.lenses[iLens] = lens
                } else {
                    box.lenses += lens
                }
            }
        }
        return boxes.sumOf { it.focusingPower() }
    }

    // TESTS
    val test1 = part1(readInput("$day/test"))
    1320L.let { check(test1 == it) { "Test 1: is $test1, should be $it" } }

    val test2 = part2(readInput("$day/test"))
    145L.let { check(test2 == it) { "Test 2: is $test2, should be $it" } }

    // RESULTS
    val input = readInput("$day/input")
    val part1 = part1(input)
    509152L.let { println("Part 1: $part1" + if (part1 == it) "" else " (should be $it?)") }
    val part2 = part2(input)
    println("Part 2: $part2")
}
