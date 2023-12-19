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

    data class Combination(val x: IntRange, val m: IntRange, val a: IntRange, val s: IntRange)

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
        .let { (x, m, a, s) -> Part(x, m, a, s) }

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
        val prop = when (cat) {
            'x' -> part.x
            'm' -> part.m
            'a' -> part.a
            's' -> part.s
            else -> error("$cat")
        }
        return when (op) {
            '<' -> prop < value
            '>' -> prop > value
            else -> error("$op")
        }
    }

    data class Result(
        val match: Combination?,
        val mismatch: Combination?
    )

    fun Combination.withRange(cat: Char, propRange: IntRange) = when (cat) {
        'x' -> copy(x = propRange)
        'm' -> copy(m = propRange)
        'a' -> copy(a = propRange)
        's' -> copy(s = propRange)
        else -> error("$cat")
    }

    fun Condition.result(c: Combination): Result {
        val prop = when (cat) {
            'x' -> c.x
            'm' -> c.m
            'a' -> c.a
            's' -> c.s
            else -> error("$cat")
        }
        val splitCombinations = listOf(prop.first..<value, value + 1..prop.last)
            .map { c.withRange(cat, it) }
        return when (op) {
            '<' -> Result(
                match = c.withRange(cat, prop.first..<value),
                mismatch = c.withRange(cat, value..prop.last)
            )
            '>' -> Result(
                match = c.withRange(cat, value + 1..prop.last),
                mismatch = c.withRange(cat, prop.first..value)
            )
            else -> error("$op")
        }
    }

    fun Part.rating() = x + m + a + s

    fun Instruction.matches(part: Part) = cond == null || cond.matches(part)

    fun Instruction.result(c: Combination) = cond?.result(c) ?: Result(match = c, mismatch = null)

    fun Workflow.nextResult(part: Part) = instr.first { it.matches(part) }.target

    fun System.nextResult(target: String, part: Part) = workflows[target]!!.nextResult(part)

    fun System.result(part: Part) = generateSequence("in") { nextResult(it, part) }
        .takeWhileInclusive { !(it.isAccepted() || it.isRejected()) }
        .last()

    val init = 1..4000

    fun initial() = Combination(init, init, init, init)

    fun Combination.isValid() = listOf(x, m, a, s).none { it.isEmpty() }

    fun Result.validate() = Result(
        match = match?.takeIf { it.isValid() },
        mismatch = mismatch?.takeIf { it.isValid() }
    )

    data class Match(val c: Combination, val target: String)

    fun Workflow.nextResults(c: Combination): List<Match> {
        val matches = mutableListOf<Match>()
        var mismatches = listOf(c)
        for (i in instr) {
            val results = mismatches.map { m -> i.result(m).validate() }
            matches += results.mapNotNull { it.match?.let { m -> Match(m, i.target) } }
            mismatches = results.mapNotNull { it.mismatch }
        }
        return matches
    }

    fun System.nextResults(target: String, c: Combination) = workflows[target]!!.nextResults(c)

    fun System.result(c: Combination): List<Match> {
        val seed = listOf(Match(c, "in"))
        return generateSequence(seed) { matches ->
//            println(matches)
            matches
            .filter { m -> !m.target.isRejected() }
            .flatMap { m -> if (m.target.isAccepted()) listOf(m) else nextResults(m.target, m.c) }
        }
            .takeWhileInclusive { matches -> !matches.all { it.target.isAccepted() } }
            .last()
    }

    fun Combination.combinationCount() = x.count().toLong() * m.count() * a.count() * s.count()

    fun part1(input: List<String>): Long {
        val system = input.parseSystem()
        val acceptedParts = system.parts
            .filter { part -> system.result(part).isAccepted() }

        return acceptedParts.sumOf { it.rating().toLong() }
    }

    fun part2(input: List<String>): Long {
        val system = input.parseSystem()
        val initial = initial()

        val acceptedMatches = system.result(initial)
        acceptedMatches.forEach { println(it) }
        return acceptedMatches.sumOf { it.c.combinationCount() }
    }

    // TESTS
    val test1 = part1(readInput("$day/test"))
    19114L.let { check(test1 == it) { "Test 1: is $test1, should be $it" } }

    val test2 = part2(readInput("$day/test"))
    167409079868000L.let { check(test2 == it) { "Test 2: is $test2, should be $it" } }

    // RESULTS
    val input = readInput("$day/input")
    val part1 = part1(input)
    330820L.let { println("Part 1: $part1" + if (part1 == it) "" else " (should be $it?)") }
    val part2 = part2(input)
    println("Part 2: $part2")
}
