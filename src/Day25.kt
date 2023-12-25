import java.util.*

fun main() {
    val day = "Day25"

    // MODEL
    data class ConnectionDesc(
        val name: String,
        val connections: Set<String>,
        val connectionInfo: GraphInfo<String>
    )

    data class Machine(val connectionDescs: Map<String, ConnectionDesc>)

    data class Connection(val a: String, val b: String) {
        init {
            check(a < b)
        }
    }

    data class CandidateSolution(val disconnected: Set<Connection>) {
        init {
            check(disconnected.size == 3)
        }
    }

    // PARSE
    fun Map<String, Set<String>>.toMachine() = keys
        .map { name ->
            val connectionInfo = GraphInfo(name) { this[it]!! }
            ConnectionDesc(
                name = name,
                connections = this[name]!!,
                connectionInfo = connectionInfo
            )
        }
        .associateBy { it.name }
        .let { Machine(it) }

    fun List<String>.parseMachine(): Machine {
        val connectionMap = mutableMapOf<String, MutableSet<String>>()

        for (line in this) {
            val (name, connections) = line.split(": ")
            for (connected in connections.split(' ')) {
                connectionMap.at(name) += connected
                connectionMap.at(connected) += name
            }
        }

        return connectionMap.toMachine()
    }

    // SOLVE
    fun connection(a: String, b: String) = if (a < b) Connection(a, b) else Connection(b, a)

    fun Machine.groupSizes(): List<Int> {
        val groups = mutableListOf<MutableSet<String>>()
        for (c in connectionDescs.values) {
            var foundGroup = false
            for (g in groups) {
                if (!Collections.disjoint(c.connectionInfo.allItems, g)) {
                    g += c.connectionInfo.allItems
                    foundGroup = true
                    break
                }
            }
            if (!foundGroup) {
                groups += c.connectionInfo.allItems.toMutableSet()
            }
        }
        return groups.map { it.size }
    }

    fun Machine.disconnect(s: CandidateSolution): Machine {
        val connectionMap = mutableMapOf<String, MutableSet<String>>()

        for (connection in connectionDescs.values) {
            val name = connection.name
            for (connected in connection.connections) {
                if (connection(name, connected) !in s.disconnected) {
                    connectionMap.at(name) += connected
                    connectionMap.at(connected) += name
                }
            }
        }

        return connectionMap.toMachine()
    }

    fun Machine.hasConnection(c: Connection) = c.a in connectionDescs[c.b]!!.connections

    fun Machine.candidateSolutions(): Sequence<CandidateSolution> {
        val wires = connectionDescs.values
            .sortedBy { it.connectionInfo.layeredCounts.size }
            .map { it.name }
        val conns = wires.indices
            .flatMap { i -> (i + 1..<wires.size).map { j -> listOf(i, j) } }
            .sortedBy { it.sum() }
            .map { (i, j) -> connection(wires[i], wires[j]) }
            .filter { hasConnection(it) }
            .take(100)

        val solutionIndexes = conns.indices
            .flatMap { i -> (i + 1..<conns.size).flatMap { j -> (j + 1..<conns.size).map { k -> listOf(i, j, k) } } }
            .sortedBy { it.sum() }
        println("Potential solution count: ${solutionIndexes.size}")
        return solutionIndexes.asSequence()
            .map { (i, j, k) -> CandidateSolution(setOf(conns[i], conns[j], conns[k])) }
    }

    fun Machine.solution(): List<Int> = candidateSolutions()
        .firstNotNullOf { s -> disconnect(s).groupSizes().takeIf { it.size == 2 } }

    // hfx/pzl, bvb/cmg, nvd/jqt

    fun Machine.describe() {
        println()
        println("Machine with ${connectionDescs.size} connections")
        connectionDescs.values.forEach { m ->
            println("${m.name}: direct: ${m.connections.size}, layers: ${m.connectionInfo.layeredCounts}")
        }
        println("Group sizes: ${groupSizes()}")

        val shortestDepth = connectionDescs.values.minOf { it.connectionInfo.layeredCounts.size }
        val shortestDepths = connectionDescs.values.filter { it.connectionInfo.layeredCounts.size == shortestDepth }
        println("Shortest")
        shortestDepths.forEach { println(it) }
    }

    fun part1(input: List<String>): Int {
        val machine = input.parseMachine()
        machine.describe()

//        val modified = machine.disconnect(setOf(setOf("hfx", "pzl"), setOf("bvb", "cmg"), setOf("nvd", "jqt")))
//        modified.describe()

        val solution = machine.solution()

        return solution.reduce(Int::times)
    }

    fun part2(input: List<String>): Long {
        return 0
    }

    // TESTS
    val test1 = part1(readInput("$day/test"))
    54.let { check(test1 == it) { "Test 1: is $test1, should be $it" } }

    val test2 = part2(readInput("$day/test"))
    0L.let { check(test2 == it) { "Test 2: is $test2, should be $it" } }

    // RESULTS
    val input = readInput("$day/input")
    val part1 = part1(input) // 775850 is wrong
    0.let { println("Part 1: $part1" + if (part1 == it) "" else " (should be $it?)") }
    val part2 = part2(input)
    println("Part 2: $part2")
}
