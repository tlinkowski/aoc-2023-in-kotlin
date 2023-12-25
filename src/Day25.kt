fun main() {
    val day = "Day25"

    // MODEL
    data class Component(
        val name: String,
        val connectedComponents: Set<String>,
        val connectionInfo: GraphInfo<String>
    )

    data class Machine(val components: Map<String, Component>)

    data class Connection(val a: String, val b: String) {
        init {
            check(a < b)
        }

        override fun toString() = "$a/$b"
    }

    data class Solution(val no: Int, val disconnected: Set<Connection>) {
        init {
            check(disconnected.size == 3)
        }
    }

    // PARSE
    fun toMachine(connectionMap: Map<String, Set<String>>) = connectionMap.keys
        .map { name ->
            Component(
                name = name,
                connectedComponents = connectionMap[name]!!,
                connectionInfo = GraphInfo(name) { connectionMap[it]!! }
            )
        }
        .associateBy { it.name }
        .let { Machine(it) }

    fun List<String>.parseMachine(): Machine {
        val connectionMap = mutableMapOf<String, MutableSet<String>>()

        for (line in this) {
            val (left, rights) = line.split(": ")
            for (right in rights.split(' ')) {
                connectionMap.at(left) += right
                connectionMap.at(right) += left
            }
        }

        return toMachine(connectionMap)
    }

    // SOLVE
    fun connection(a: String, b: String) = if (a < b) Connection(a, b) else Connection(b, a)

    fun Machine.separateGroups(): Set<Set<String>> = components.values
        .mapTo(mutableSetOf()) { it.connectionInfo.allItems }

    fun Machine.disconnect(s: Solution): Machine {
        val connectionMap = mutableMapOf<String, MutableSet<String>>()

        for (connection in components.values) {
            val a = connection.name
            for (b in connection.connectedComponents) {
                if (connection(a, b) !in s.disconnected) {
                    connectionMap.at(a) += b
                    connectionMap.at(b) += a
                }
            }
        }

        return toMachine(connectionMap)
    }

    fun Machine.component(name: String) = components[name]!!

    fun Machine.hasConnection(connection: Connection) = connection.a in component(connection.b).connectedComponents

    fun Machine.candidateSolutions(connectionLimit: Int): List<Solution> {
        val wires: List<String> = components.values
            .sortedBy { it.connectionInfo.layerSizes.size }
            .map { it.name }
        val conns: List<Connection> = wires.indices
            .flatMap { i -> (i + 1..<wires.size).map { j -> listOf(i, j) } }
            .sortedBy { it.sum() }
            .map { (i, j) -> connection(wires[i], wires[j]) }
            .filter { hasConnection(it) }
            .take(connectionLimit)

        val solutions = conns.indices
            .flatMap { i -> (i + 1..<conns.size).flatMap { j -> (j + 1..<conns.size).map { k -> listOf(i, j, k) } } }
            .sortedBy { it.sum() }
            .mapIndexed { iSolution, (i, j, k) -> Solution(iSolution + 1, setOf(conns[i], conns[j], conns[k])) }
        print("Analyzing ${solutions.size} solutions...")
        return solutions
    }

    fun Machine.solve(connectionLimit: Int): Pair<Solution, Machine> = candidateSolutions(connectionLimit)
        .firstNotNullOf { solution ->
            disconnect(solution)
                .takeIf { machine -> machine.separateGroups().size == 2 }
                ?.let { machine -> solution to machine }
        }

    fun part1(input: List<String>, printDetails: Boolean = false): Int {
        print("Building machine from ${input.size} lines...")
        val machine = input.parseMachine()
        println("\b\b\b with ${machine.components.size} components")
        if (printDetails) {
            machine.components.values.forEach { c ->
                println("- ${c.name}: direct connections: ${c.connectedComponents.size}, layers: ${c.connectionInfo.layerSizes}")
            }
        }

        val (solution, fixedMachine) = machine.solve(connectionLimit = 20)
        println("\b\b\b: found $solution")

        val separateGroups = fixedMachine.separateGroups()
        println("Separate groups:")
        separateGroups.forEach { g -> println("- ${g.size}: ${if (g.size <= 10) g else g.take(10) + "..."}") }

        println()
        return separateGroups.map { it.size }.reduce(Int::times)
    }

    // TESTS
    val test1 = part1(readInput("$day/test"), printDetails = true)
    54.let { check(test1 == it) { "Test 1: is $test1, should be $it" } }

    // RESULTS
    val input = readInput("$day/input")
    val part1 = part1(input)
    543036.let { println("Part 1: $part1" + if (part1 == it) "" else " (should be $it?)") }
}
