fun main() {
    val day = "Day19"

    // MODEL
    data class Part(val x: Int, val m: Int, val a: Int, val s: Int)

    data class Condition(val fieldName: Char, val operation: Char, val refValue: Int)

    data class Instruction(val condition: Condition?, val target: String)

    data class Workflow(val name: String, val instructions: List<Instruction>)

    data class System(val workflows: Map<String, Workflow>, val parts: List<Part>)

    // - part 2
    data class Combination(val x: IntRange, val m: IntRange, val a: IntRange, val s: IntRange)

    data class CombinationSplit(val match: Combination?, val mismatch: Combination?)

    data class InstructionMatch(val combination: Combination, val target: String)

    // PARSE
    fun String.parseCondition() = Condition(
        fieldName = this[0],
        operation = this[1],
        refValue = substring(2).toInt()
    )

    fun String.parseInstruction() = Instruction(
        condition = substringBefore(':', "").ifEmpty { null }?.parseCondition(),
        target = substringAfter(':')
    )

    fun String.parseWorkflow() = Workflow(
        name = substringBefore('{'),
        instructions = substringAfter('{').removeSuffix("}").split(',')
            .map { it.parseInstruction() }
    )

    fun String.parsePart() = removePrefix("{").removeSuffix("}").split(',')
        .map { it.substringAfter('=').toInt() }
        .let { (x, m, a, s) -> Part(x, m, a, s) }

    fun List<String>.parseSystem() = joinToString("\n").split("\n\n").let { (workflows, parts) ->
        System(
            workflows = workflows.split('\n')
                .map { it.parseWorkflow() }
                .associateBy { it.name },
            parts = parts.split('\n')
                .map { it.parsePart() }
        )
    }

    // SOLVE
    val initialTarget = "in"

    fun System.workflow(target: String) = workflows[target]!!

    fun String.isAccepted() = this == "A"

    fun String.isRejected() = this == "R"

    fun String.isTerminal() = isAccepted() || isRejected()

    // - part 1
    fun Part.fieldValue(fieldName: Char) = when (fieldName) {
        'x' -> x
        'm' -> m
        'a' -> a
        's' -> s
        else -> error(fieldName)
    }

    fun Condition.matches(part: Part): Boolean {
        val fieldValue = part.fieldValue(fieldName)
        return when (operation) {
            '<' -> fieldValue < refValue
            '>' -> fieldValue > refValue
            else -> error(operation)
        }
    }

    fun Instruction.matches(part: Part) = condition == null || condition.matches(part)

    fun Workflow.nextTarget(part: Part) = instructions.first { it.matches(part) }.target

    fun System.endTarget(part: Part) = generateSequence(initialTarget) { target -> workflow(target).nextTarget(part) }
        .first { target -> target.isTerminal() }

    fun Part.rating() = x + m + a + s

    fun part1(input: List<String>): Int {
        val system = input.parseSystem()
        val acceptedParts = system.parts.filter { part -> system.endTarget(part).isAccepted() }

        return acceptedParts.sumOf { it.rating() }
    }

    // - part 2
    fun Combination.fieldValueRange(fieldName: Char) = when (fieldName) {
        'x' -> x
        'm' -> m
        'a' -> a
        's' -> s
        else -> error(fieldName)
    }

    fun Combination.withValueRange(fieldName: Char, valueRange: IntRange) = when (fieldName) {
        'x' -> copy(x = valueRange)
        'm' -> copy(m = valueRange)
        'a' -> copy(a = valueRange)
        's' -> copy(s = valueRange)
        else -> error(fieldName)
    }

    fun Condition.split(combination: Combination): CombinationSplit {
        fun partialCombination(valueRange: IntRange) = combination.withValueRange(fieldName, valueRange)

        val valueRange = combination.fieldValueRange(fieldName)
        return when (operation) {
            '<' -> CombinationSplit(
                match = partialCombination(valueRange.first..<refValue),
                mismatch = partialCombination(refValue..valueRange.last)
            )

            '>' -> CombinationSplit(
                mismatch = partialCombination(valueRange.first..refValue),
                match = partialCombination(refValue + 1..valueRange.last)
            )

            else -> error(operation)
        }
    }

    fun Combination.isValid() = listOf(x, m, a, s).none { it.isEmpty() }

    fun Combination.validate() = takeIf { isValid() }

    fun CombinationSplit.validate() = CombinationSplit(match = match?.validate(), mismatch = mismatch?.validate())

    fun Instruction.split(combination: Combination) = condition?.split(combination)
        ?: CombinationSplit(match = combination, mismatch = null)

    fun Workflow.nextMatches(combination: Combination): List<InstructionMatch> {
        var mismatchCombinations = listOf(combination)
        val matches = mutableListOf<InstructionMatch>()

        for (instruction in instructions) {
            val combinationSplits = mismatchCombinations
                .map { nextCombination -> instruction.split(nextCombination).validate() }
            mismatchCombinations = combinationSplits.mapNotNull { it.mismatch }
            matches += combinationSplits.mapNotNull { it.match }
                .map { nextCombination -> InstructionMatch(nextCombination, instruction.target) }
        }

        check(mismatchCombinations.isEmpty())
        return matches
    }

    fun InstructionMatch.nextMatches(system: System) = when {
        target.isTerminal() -> listOf(this)
        else -> system.workflow(target).nextMatches(combination)
    }

    fun List<InstructionMatch>.nextMatches(system: System) = flatMap { match -> match.nextMatches(system) }

    fun List<InstructionMatch>.allTerminal() = all { match -> match.target.isTerminal() }

    fun initialMatch() = (1..4000)
        .let { fullRange -> Combination(fullRange, fullRange, fullRange, fullRange) }
        .let { initialCombination -> InstructionMatch(initialCombination, initialTarget) }

    fun System.endMatches() = generateSequence(listOf(initialMatch())) { matches -> matches.nextMatches(this) }
        .first { matches -> matches.allTerminal() }

    fun Combination.count() = x.size().toLong() * m.size() * a.size() * s.size()

    fun part2(input: List<String>): Long {
        val system = input.parseSystem()

        val acceptedCombinations = system.endMatches()
            .mapNotNull { if (it.target.isAccepted()) it.combination else null }
        return acceptedCombinations.sumOf { it.count() }
    }

    // TESTS
    val test1 = part1(readInput("$day/test"))
    19114.let { check(test1 == it) { "Test 1: is $test1, should be $it" } }

    val test2 = part2(readInput("$day/test"))
    167409079868000L.let { check(test2 == it) { "Test 2: is $test2, should be $it" } }

    // RESULTS
    val input = readInput("$day/input")
    val part1 = part1(input)
    330820.let { println("Part 1: $part1" + if (part1 == it) "" else " (should be $it?)") }
    val part2 = part2(input)
    println("Part 2: $part2")
}
