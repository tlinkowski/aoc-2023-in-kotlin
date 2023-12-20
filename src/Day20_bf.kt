//enum class Pulse { LOW, HIGH }
//
//interface Module {
//    val name: String
//    val targets: List<String>
//}
//
//fun main() {
//    val day = "Day20"
//
//    // MODEL
//    data class FlipFlopModule(
//        override val name: String, override val targets: List<String>, var on: Boolean = false
//    ) : Module
//
//    data class ConjunctionModule(
//        override val name: String, override val targets: List<String>,
//        val lastPulses: MutableMap<String, Pulse> = mutableMapOf()
//    ) : Module
//
//    data class BroadcasterModule(override val name: String, override val targets: List<String>) : Module
//
//    data class System(val modules: Map<String, Module>)
//
//    // PARSE
//    fun String.parseModule(): Module {
//        val name = substringBefore(" -> ").removePrefix("%").removePrefix("&")
//        val targets = substringAfter(" -> ").split(", ")
//        return when {
//            startsWith('%') -> FlipFlopModule(name, targets)
//            startsWith('&') -> ConjunctionModule(name, targets)
//            startsWith("broadcaster ") -> BroadcasterModule(name, targets)
//            else -> error(this)
//        }
//    }
//
//    fun List<String>.parseSystem() = System(
//        map { it.parseModule() }.associateBy { it.name }
//    )
//
//    // SOLVE
//    fun System.updateConjunctionModules() = also {
//        modules.values.forEach { m ->
//            m.targets.forEach { t ->
//                modules[t]
//                    ?.let { it as? ConjunctionModule }
//                    ?.let { it.lastPulses[m.name] = Pulse.LOW }
//            }
//        }
//    }
//
//// Flip-flop modules (prefix %) are either on or off; they are initially off.
//    // if a flip-flop module receives a low pulse, it flips between on and off
//    // If it was off, it turns on and sends a high pulse. If it was on, it turns
//    // off and sends a low pulse.
//
//    fun FlipFlopModule.nextPulse(pulse: Pulse) = when (pulse) {
//        Pulse.LOW -> {
//            on = !on
//            if (on) Pulse.HIGH else Pulse.LOW
//        }
//
//        Pulse.HIGH -> null
//    }
//
//    fun ConjunctionModule.nextPulse(source: String, pulse: Pulse): Pulse {
//        lastPulses[source] = pulse
//        return if (lastPulses.values.all { it == Pulse.HIGH}) Pulse.LOW else Pulse.HIGH
//    }
//
//    fun Module.nextPulse(source: String, pulse: Pulse): Pulse? = when (this) {
//        is FlipFlopModule -> nextPulse(pulse)
//        is ConjunctionModule -> nextPulse(source, pulse)
//        is BroadcasterModule -> pulse
//        else -> error(this)
//    }
//
//    data class Signal(val source: String, val pulse: Pulse, val target: String)
//
//    fun Module.acceptPulse(source: String, pulse: Pulse): List<Signal> {
//        val next = nextPulse(source, pulse)
//        if (next == null) {
//            return listOf()
//        }
//        return targets
//            .map { target -> Signal(name, next, target) }
////            .mapNotNull { t -> nextPulse(pulse, name)?.let { p -> Signal(t, p) } }
////            .onEach { s -> println("$name -> $s") }
//    }
//
//    fun System.pressMainButton(): Boolean {
//        var signals = listOf(Signal("button", Pulse.LOW, "broadcaster"))
//        while (signals.isNotEmpty()) {
////            signals.forEach { s ->
////                println("${s.source} -${s.pulse}-> ${s.target}")
////            }
//            signals = signals.mapNotNull { s ->
//                val m = modules[s.target]
//                if (m != null) {
//                    val next = m.acceptPulse(s.source, s.pulse)
////                    next.forEach { ss -> println("${ss.source} -${ss.pulse}-> ${ss.target}") }
//                    next
//                } else null
//            }.flatten()
//            if (signals.any { it.target == "rx" && it.pulse == Pulse.LOW }) {
//                return true
//            }
//        }
//
//        return false
//    }
//
//    // Conjunction modules (prefix &) remember the type of the most
//    // recent pulse received from each of their connected input modules;
//    // they initially default to remembering a low pulse for each input.
//    // When a pulse is received, the conjunction module first updates its
//    // memory for that input. Then, if it remembers high pulses for all inputs,
//    // it sends a low pulse; otherwise, it sends a high pulse.
//
//    // There is a single broadcast module (named broadcaster). When it receives a pulse,
//    // it sends the same pulse to all of its destination modules.
//
//    // a single low pulse is sent directly to the broadcaster module.
//
//    fun part2(input: List<String>): Long {
//        val system = input.parseSystem().updateConjunctionModules()
//
//        var i = 0L
//        while (!system.pressMainButton()) {
//            i++
//            if (i % 100000 == 0L) println("Step $i")
//        }
//
//        return i
//    }
//
//    // TESTS
////    val test1a = part1(readInput("$day/test1a"))
////    32000000L.let { check(test1a == it) { "Test 1a: is $test1a, should be $it" } }
//
////    val test1 = part1(readInput("$day/test"))
////    11687500L.let { check(test1 == it) { "Test 1: is $test1, should be $it" } }
////
////    val test2 = part2(readInput("$day/test"))
////    794930686L.let { check(test2 == it) { "Test 2: is $test2, should be $it" } }
//
//    // RESULTS
//    val input = readInput("$day/input")
////    val part1 = part1(input)
////    0L.let { println("Part 1: $part1" + if (part1 == it) "" else " (should be $it?)") }
//    val part2 = part2(input)
//    println("Part 2: $part2")
//}
