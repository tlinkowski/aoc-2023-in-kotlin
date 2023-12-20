enum class Pulse { LOW, HIGH }

interface PulseLogic {
    fun nextPulse(source: String, pulse: Pulse): Pulse?

    fun isStartPosition(): Boolean = true
}

object BroadcasterLogic : PulseLogic {
    override fun nextPulse(source: String, pulse: Pulse) = pulse
}

object NoLogic : PulseLogic {
    override fun nextPulse(source: String, pulse: Pulse) = null
}

fun main() {
    val day = "Day20"

    // MODEL
    data class Module(
        val name: String,
        val sources: List<String>,
        val targets: List<String>,
        val logic: PulseLogic
    )

    class FlipFlopLogic(var on: Boolean = false) : PulseLogic {
        override fun nextPulse(source: String, pulse: Pulse) = when (pulse) {
            Pulse.LOW -> {
                on = !on
                if (on) Pulse.HIGH else Pulse.LOW
            }

            Pulse.HIGH -> null
        }

        override fun isStartPosition() = !on
    }

    class ConjunctionLogic(val lastSourcePulses: MutableMap<String, Pulse> = mutableMapOf()) : PulseLogic {
        override fun nextPulse(source: String, pulse: Pulse): Pulse {
            lastSourcePulses[source] = pulse
            return if (lastSourcePulses.values.all { it == Pulse.HIGH }) Pulse.LOW else Pulse.HIGH
        }

        override fun isStartPosition() = lastSourcePulses.values.all { it == Pulse.LOW }
    }

    data class System(val modules: Map<String, Module>)

    // PARSE
    fun String.parseModule(): Module {
        val name = substringBefore(" -> ").removePrefix("%").removePrefix("&")
        val targets = substringAfter(" -> ").split(", ")
        val logic = when {
            startsWith('%') -> FlipFlopLogic()
            startsWith('&') -> ConjunctionLogic()
            startsWith("broadcaster ") -> BroadcasterLogic
            else -> error(this)
        }
        return Module(name, sources = listOf(), targets, logic)
    }

    fun List<String>.parseSystem() = map { it.parseModule() }.let { modules ->
        val sourceNamesByModuleName = modules.flatMap { m -> m.targets.map { t -> t to m.name } }
            .groupBy({ it.first }, { it.second })

        val mainModules = modules
            .map { module ->
                sourceNamesByModuleName[module.name]
                    ?.let { sources -> module.copy(sources = sources) }
                    ?: module
            }
            .associateBy { it.name }

        val noOpModules = sourceNamesByModuleName
            .filterKeys { it !in mainModules }
            .map { (moduleName, sources) ->
                moduleName to Module(
                    name = moduleName,
                    sources = sources,
                    targets = listOf(),
                    logic = NoLogic
                )
            }

        System(mainModules + noOpModules)
    }

    // SOLVE
    fun System.updateConjunctionModules() = also {
        modules.values
            .filter { it.logic is ConjunctionLogic }
            .forEach { m ->
                val lastPulses = (m.logic as ConjunctionLogic).lastSourcePulses
                m.sources.forEach { source -> lastPulses[source] = Pulse.LOW }
            }
    }

    data class Signal(val source: String, val pulse: Pulse, val target: String)

    fun Module.acceptPulse(source: String, pulse: Pulse): List<Signal> {
        val next = logic.nextPulse(source, pulse)
        if (next == null) {
            return listOf()
        }
        return targets.map { target -> Signal(name, next, target) }
    }

    fun System.pressMainButton() {
        var signals = listOf(Signal("button", Pulse.LOW, "broadcaster"))
        while (signals.isNotEmpty()) {
//            println(signals)
            signals = signals.mapNotNull { s ->
                val m = modules[s.target]
                if (m != null) {
                    val next = m.acceptPulse(s.source, s.pulse)
                    next
                } else null
            }.flatten()
        }
    }

    // Conjunction modules (prefix &) remember the type of the most
    // recent pulse received from each of their connected input modules;
    // they initially default to remembering a low pulse for each input.
    // When a pulse is received, the conjunction module first updates its
    // memory for that input. Then, if it remembers high pulses for all inputs,
    // it sends a low pulse; otherwise, it sends a high pulse.

    // There is a single broadcast module (named broadcaster). When it receives a pulse,
    // it sends the same pulse to all of its destination modules.

    // a single low pulse is sent directly to the broadcaster module.

    fun System.displayModulesFromTop(m: Module, tab: String, seen: MutableSet<String>) {
        println("$tab${m.name}: ${m.targets.size} - ${m.logic::class.simpleName}")
        m.targets.forEach { t ->
            if (seen.add(t)) {
                displayModulesFromTop(modules[t]!!, tab + " ", seen)
            } else {
                println("$tab $t...")
            }
        }
    }

    fun System.displayModulesFromBottom(m: Module, tab: String, seen: MutableSet<String>) {
        println("$tab${m.name}: ${m.sources.size} - ${m.logic::class.simpleName}")
        if (tab.length == 4) {
            println("$tab #")
            return
        }
        m.sources.forEach { t ->
            if (seen.add(t)) {
                displayModulesFromBottom(modules[t]!!, tab + " ", seen)
            } else {
                println("$tab $t...")
            }
        }
    }

    fun System.depths(moduleName: String): Map<String, Int> {
        var moduleNames = listOf(moduleName)
        val depths = mutableMapOf<String, Int>()
        depths[moduleName] = 0

        var depth = 1
        while (moduleNames.isNotEmpty()) {
            moduleNames = moduleNames
                .flatMap { name -> modules[name]!!.sources }
                .filter { name ->
                    val d = if (name in depths) Int.MAX_VALUE else depth
                    depths[name] = d
                    d < Int.MAX_VALUE
                }
            depth++
        }

        return depths
    }

    fun System.dependantModuleNames(m: Module): Set<String> {
        val result = mutableSetOf<String>()
        var input = m.sources
        while (input.isNotEmpty()) {
            input = input.filter { result.add(it) }.flatMap { modules[it]!!.sources }
        }
        return result
    }

    fun System.pressMainButtonCount(moduleName: String): Long {
        val depths = depths(moduleName)
        fun calculateCycleNumerically(m: Module) = depths[m.name] != Int.MAX_VALUE
                && (m.logic is NoLogic || m.logic is ConjunctionLogic)

        val cycleLengths = mutableMapOf<String, Int>()
        val moduleNamesLeft = modules.values
            .filter { !calculateCycleNumerically(it) }
            .map { it.name }
            .toMutableSet()

        val dependantModules =
            modules.values.associate { it.name to dependantModuleNames(it).map { n -> modules[n]!! } }

        fun isStartPosition(m: Module) = m.logic.isStartPosition()
                && dependantModules[m.name]!!.all { it.logic.isStartPosition() }

        var i = 1
        while (moduleNamesLeft.isNotEmpty()) {
            pressMainButton()
            val found = mutableSetOf<String>()
            moduleNamesLeft.forEach { name ->
                val m = modules[name]!!
                if (isStartPosition(m)) {
                    println("Found $name: $i")
                    found += name
                    cycleLengths[name] = i
                }
            }
            moduleNamesLeft -= found
            i++
        }

        fun cycleLength(name: String): Long {
            val module = modules[name]!!
            return when (module.logic) {
                is ConjunctionLogic -> module.sources.map { cycleLength(it) }.reduce(::lcm)
                is NoLogic -> cycleLength(module.sources.single())
                else -> cycleLengths[name]!!.toLong()
            }
        }

        return cycleLength(moduleName)
    }

    fun part2(input: List<String>): Long {
        val system = input.parseSystem().updateConjunctionModules()

        return system.pressMainButtonCount("rx")
    }

    // TESTS
//    val test1a = part1(readInput("$day/test1a"))
//    32000000L.let { check(test1a == it) { "Test 1a: is $test1a, should be $it" } }

//    val test1 = part1(readInput("$day/test"))
//    11687500L.let { check(test1 == it) { "Test 1: is $test1, should be $it" } }
//
//    val test2 = part2(readInput("$day/test"))
//    794930686L.let { check(test2 == it) { "Test 2: is $test2, should be $it" } }

    // RESULTS
    val input = readInput("$day/input")
//    val part1 = part1(input)
//    0L.let { println("Part 1: $part1" + if (part1 == it) "" else " (should be $it?)") }
    val part2 = part2(input)
    println("Part 2: $part2")
}
