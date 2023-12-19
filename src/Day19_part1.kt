fun main() {
    val day = "Day19"

    // MODEL
    data class Part(val x: Int, val m: Int, val a: Int, val s: Int)

    data class Condition(
        val cat: Char,
        val op: Char,
        val value: Int,
    )

    data class Instruction(
        val cond: Condition?,
        val target: String
    )

    data class Workflow(val name: String, val instr: List<Instruction>)

    data class System(val workflows: Map<String, Workflow>, val parts: List<Part>)

    // PARSE
    fun String.parseCondition() = Condition(
        cat = this[0],
        op = this[1],
        value = substring(2).toInt()
    )

    fun String.parseInstruction() = Instruction(
        cond = substringBefore(':', "").takeIf { it.isNotEmpty() }?.parseCondition(),
        target = substringAfter(':')
    )

    fun String.parseWorkflow() = Workflow(
        name = substringBefore('{'),
        instr = substringAfter('{').substringBefore('}').split(',')
            .map { it.parseInstruction() }
    )

    fun String.parsePart() = drop(1).dropLast(1).split(',')
        .map { it.substringAfter('=').toInt() }
        .let { (x, m, a, s) -> Part(x, m, a, s)}

    fun List<String>.parseSystem() = joinToString("\n").split("\n\n").let { (w, p) ->
        val workflows = w.split('\n')
            .filter { it.isNotBlank() }
            .map { it.parseWorkflow() }
        val parts = p.split('\n')
            .filter { it.isNotBlank() }
            .map { it.parsePart() }
        System(workflows.associateBy { it.name }, parts)
    }

    // SOLVE
    fun String.isAccepted() = this == "A"
    fun String.isRejected() = this == "R"

    fun Condition.matches(part: Part): Boolean {
        val prop = when(cat) {
            'x' -> part.x
            'm' -> part.m
            'a' -> part.a
            's' -> part.s
            else -> error("$cat")
        }
        return when(op) {
            '<' -> prop < value
            '>' -> prop > value
            else -> error("$op")
        }
    }

    fun Part.rating() = x + m + a + s

    fun Instruction.matches(part: Part) = cond == null || cond.matches(part)

    fun System.nextResult(target: String, part: Part) = with(workflows[target]!!) {
        instr.first { it.matches(part) }.target
    }

    fun System.result(part: Part) = generateSequence("in") { nextResult(it, part) }
        .takeWhileInclusive { !(it.isAccepted() || it.isRejected()) }
        .last()

    fun part1(input: List<String>): Long {
        val system = input.parseSystem()
        val acceptedParts = system.parts
            .filter { part -> system.result(part).isAccepted() }

        return acceptedParts.sumOf { it.rating().toLong() }
    }

    fun part2(input: List<String>): Long {
        return 0
    }

    // TESTS
    val test1 = part1(readInput("$day/test"))
    19114L.let { check(test1 == it) { "Test 1: is $test1, should be $it" } }

    val test2 = part2(readInput("$day/test"))
    0L.let { check(test2 == it) { "Test 2: is $test2, should be $it" } }

    // RESULTS
    val input = readInput("$day/input")
    val part1 = part1(input)
    330820L.let { println("Part 1: $part1" + if (part1 == it) "" else " (should be $it?)") }
    val part2 = part2(input)
    println("Part 2: $part2")
}
