enum class Pulse { LOW, HIGH }

interface PulseLogic {
    fun init(sources: List<String>): PulseLogic = this

    fun nextPulse(source: String, pulse: Pulse): Pulse?

    fun isReset(): Boolean = true
}

object BroadcasterLogic : PulseLogic {
    override fun nextPulse(source: String, pulse: Pulse) = pulse
}

object SinkLogic : PulseLogic { // e.g. "rx"
    override fun nextPulse(source: String, pulse: Pulse) = null
}

fun main() {
    val day = "Day20"

    // MODEL
    data class Module(
        val name: String,
        val logic: PulseLogic,
        val sources: List<String>,
        val targets: List<String>,
        val sourceInfo: GraphInfo<String>,
        val targetInfo: GraphInfo<String>
    )

    class FlipFlopLogic(var on: Boolean = false) : PulseLogic {
        override fun nextPulse(source: String, pulse: Pulse) = when (pulse) {
            Pulse.LOW -> {
                on = !on
                if (on) Pulse.HIGH else Pulse.LOW
            }

            Pulse.HIGH -> null
        }

        override fun isReset() = !on
    }

    class ConjunctionLogic(val lastSourcePulses: MutableMap<String, Pulse> = mutableMapOf()) : PulseLogic {
        override fun init(sources: List<String>) = apply {
            sources.forEach { source -> lastSourcePulses[source] = Pulse.LOW }
        }

        override fun nextPulse(source: String, pulse: Pulse): Pulse {
            lastSourcePulses[source] = pulse
            return if (lastSourcePulses.values.all { it == Pulse.HIGH }) Pulse.LOW else Pulse.HIGH
        }

        override fun isReset() = lastSourcePulses.values.all { it == Pulse.LOW }
    }

    data class System(val modules: Map<String, Module>) {
        val counter: MutableMap<Pulse, Long> = mutableMapOf(Pulse.LOW to 0, Pulse.HIGH to 0)
    }

    data class Signal(val source: String, val pulse: Pulse, val target: String)

    // PARSE
    fun String.parseSource() = substringBefore(" -> ").removePrefix("%").removePrefix("&")

    fun String.parseTargets() = substringAfter(" -> ").split(", ")

    fun String.parseLogic() = when {
        startsWith('%') -> FlipFlopLogic()
        startsWith('&') -> ConjunctionLogic()
        startsWith("broadcaster ") -> BroadcasterLogic
        else -> error(this)
    }

    fun List<String>.parseSystem(): System {
        val logicMap = mutableMapOf<String, PulseLogic>()
        val sourceMap = mutableMapOf<String, MutableList<String>>()
        val targetMap = mutableMapOf<String, MutableList<String>>()

        for (line in this) {
            val source = line.parseSource()
            for (target in line.parseTargets()) {
                sourceMap.at(target) += source
                targetMap.at(source) += target
            }
            logicMap[source] = line.parseLogic()
        }

        return (sourceMap.keys + targetMap.keys)
            .map { name ->
                Module(
                    name = name,
                    logic = (logicMap[name] ?: SinkLogic).init(sourceMap.at(name)),
                    sources = sourceMap.at(name),
                    targets = targetMap.at(name),
                    sourceInfo = GraphInfo(name, sourceMap::at),
                    targetInfo = GraphInfo(name, targetMap::at)
                )
            }
            .associateBy { it.name }
            .let { System(it) }
    }

    // SOLVE
    fun System.module(name: String) = modules[name]!!

    fun Module.acceptPulse(source: String, pulse: Pulse): List<Signal> {
        val nextPulse = logic.nextPulse(source, pulse) ?: return listOf()
        return targets.map { target -> Signal(name, nextPulse, target) }
    }

    fun System.countNextPulse(pulse: Pulse) = counter.merge(pulse, 1L, Long::plus)

    fun System.pressMainButton(print: Boolean) {
        var signals = listOf(Signal("button", Pulse.LOW, "broadcaster"))
        while (signals.isNotEmpty()) {
            if (print) signals.forEach { s -> println("${s.source} -${s.pulse.name.lowercase()}-> ${s.target}") }
            signals = signals
                .onEach { signal -> countNextPulse(signal.pulse) }
                .flatMap { signal -> module(signal.target).acceptPulse(signal.source, signal.pulse) }
        }
    }

    fun System.pulseTotal(pulse: Pulse) = counter[pulse]!!

    fun part1(input: List<String>, print: Boolean = false): Long {
        val system = input.parseSystem()

        for (pressNo in 1..1000) {
            val printPress = print && pressNo < 5
            if (printPress) println("\nPress $pressNo")
            system.pressMainButton(printPress)
        }

        return system.pulseTotal(Pulse.LOW) * system.pulseTotal(Pulse.HIGH)
    }

    fun Module.isAnalytical() = (logic is SinkLogic || logic is ConjunctionLogic) && !targetInfo.hasCycles()

    fun System.isReset(module: Module) = module.sourceInfo.allItems.all { source -> module(source).logic.isReset() }

    fun System.buildNonTrivialCycleLengths(): Map<String, Int> {
        val nonAnalyticalModules = modules.values.filter { !it.isAnalytical() }.toMutableSet()

        return buildMap {
            var cycleLength = 0
            while (nonAnalyticalModules.isNotEmpty()) {
                pressMainButton(print = false)
                cycleLength++

                nonAnalyticalModules.removeIf { module ->
                    if (isReset(module)) {
                        this[module.name] = cycleLength
                        true
                    } else false
                }
            }
        }
    }

    fun System.countMainButtonPressesForLowPulseAt(endModuleName: String, print: Boolean): Long {
        val nonTrivialCycleLengths = buildNonTrivialCycleLengths()
        if (print) println("Found cycle lengths: " + nonTrivialCycleLengths.values.distinct())

        fun cycleLength(name: String): Long = with(module(name)) {
            when (logic) {
                is SinkLogic -> cycleLength(sources.single())
                is ConjunctionLogic -> sources.map { source -> cycleLength(source) }.reduce(::lcm)
                else -> nonTrivialCycleLengths[name]!!.toLong()
            }
        }

        return cycleLength(endModuleName)
    }

    fun part2(input: List<String>, print: Boolean = false): Long {
        val system = input.parseSystem()

        return system.countMainButtonPressesForLowPulseAt("rx", print)
    }

    // TESTS
    val test1a = part1(readInput("$day/test1a"), print = true)
    32000000L.let { check(test1a == it) { "Test 1a: is $test1a, should be $it" } }

    val test1 = part1(readInput("$day/test"), print = true)
    11687500L.let { check(test1 == it) { "Test 1: is $test1, should be $it" } }

    // RESULTS
    val input = readInput("$day/input")
    val part1 = part1(input)
    794930686L.let { println("Part 1: $part1" + if (part1 == it) "" else " (should be $it?)") }
    val part2 = part2(input, print = true)
    println("Part 2: $part2")
}
