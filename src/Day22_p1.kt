//enum class Axis { X, Y, Z }
//
//fun main() {
//    val day = "Day22"
//
//    // 1,0,1~1,2,1   <- A
//    //0,0,2~2,0,2   <- B
//    //0,2,3~2,2,3   <- C
//    //0,0,4~0,2,4   <- D
//    //2,0,5~2,2,5   <- E
//    //0,1,6~2,1,6   <- F
//    //1,1,8~1,1,9   <- G
//
//    // x
//    //012
//    //.G. 9
//    //.G. 8
//    //... 7
//    //FFF 6
//    //..E 5 z
//    //D.. 4
//    //CCC 3
//    //BBB 2
//    //.A. 1
//    //--- 0
//
//    // y
//    //012
//    //.G. 9
//    //.G. 8
//    //... 7
//    //.F. 6
//    //EEE 5 z
//    //DDD 4
//    //..C 3
//    //B.. 2
//    //AAA 1
//    //--- 0
//
//    // then:
//    // x
//    //012
//    //.G. 6
//    //.G. 5
//    //FFF 4
//    //D.E 3 z
//    //??? 2
//    //.A. 1
//    //--- 0
//
//    // y
//    //012
//    //.G. 6
//    //.G. 5
//    //.F. 4
//    //??? 3 z
//    //B.C 2
//    //AAA 1
//    //--- 0
//
//    // which bricks are safe to disintegrate. A brick can be safely disintegrated if, after removing it, no other
//    // bricks would fall further directly downward.
//
//    //Brick A cannot be disintegrated safely; if it were disintegrated, bricks B and C would both fall.
//    //Brick B can be disintegrated; the bricks above it (D and E) would still be supported by brick C.
//    //Brick C can be disintegrated; the bricks above it (D and E) would still be supported by brick B.
//    //Brick D can be disintegrated; the brick above it (F) would still be supported by brick E.
//    //Brick E can be disintegrated; the brick above it (F) would still be supported by brick D.
//    //Brick F cannot be disintegrated; the brick above it (G) would fall.
//    //Brick G can be disintegrated; it does not support any other bricks.
//
//    // MODEL
//    data class Point3(val x: Int, val y: Int, val z: Int) {
//        init {
//            check(z >= 1)
//        }
//
//        fun v(axis: Axis) = when (axis) {
//            Axis.X -> x
//            Axis.Y -> y
//            Axis.Z -> z
//        }
//
//        override fun toString() = "$x,$y,$z"
//    }
//
//    fun toRange(a: Int, b: Int) = (a..b).takeIf { !it.isEmpty() } ?: b..a
//
//    data class Brick(val a: Point3, val b: Point3) {
//        val xRange = toRange(a.x, b.x)
//        val yRange = toRange(a.y, b.y)
//        val zRange = toRange(a.z, b.z)
//
//        fun range(axis: Axis) = when (axis) {
//            Axis.X -> xRange
//            Axis.Y -> yRange
//            Axis.Z -> zRange
//        }
//
//        val axis = Axis.entries.firstOrNull { range(it).size() > 1 }
//        val isSingle = axis == null
//
//        override fun toString() = "$a~$b"
//    }
//
//    data class Snapshot(val bricks: List<Brick>)
//
//    // PARSE
//    fun String.parsePoint3() = split(',')
//        .map { it.toInt() }
//        .let { (x, y, z) -> Point3(x, y, z) }
//
//    fun String.parseBrick() = split('~').let { (a, b) ->
//        Brick(a.parsePoint3(), b.parsePoint3())
//    }
//
//    fun List<String>.parseSnapshot() = Snapshot(map { it.parseBrick() })
//
//    // SOLVE
//    fun Point3.point2() = Point(x, y)
//
//    operator fun Brick.contains(p: Point3) =
//        p.x in xRange && p.y in yRange && p.z in zRange
//
//    fun Brick.points3() = when (axis) {
//        Axis.X -> xRange.map { x -> a.copy(x = x) }
//        Axis.Y -> yRange.map { y -> a.copy(y = y) }
//        Axis.Z -> zRange.map { z -> a.copy(z = z) }
//        else -> {
//            check(a == b)
//            listOf(a)
//        }
//    }
//
//    fun Brick.points2() = points3().map { it.point2() }.toSet()
//
//    fun Brick.fallBy(dz: Int) = Brick(
//        a.copy(z = a.z - dz),
//        b.copy(z = b.z - dz)
//    )
//
//    class BrickFaller(s: Snapshot) {
//        val newBricks = s.bricks.toMutableSet()
//
//        val byPoint2 = s.bricks
//            .flatMap { b -> b.points2().map { it to b } }
//            .groupBy({ it.first }, { it.second })
//            .mapValues { (_, v) -> v.sortedBy { it.zRange.first }.toMutableList() } //
//            .toMutableMap()
//
//        fun remove(b: Brick) {
//            newBricks -= b
//            b.points2().forEach { p2 -> byPoint2.at(p2) -= b }
//        }
//
//        fun add(b: Brick) {
//            newBricks += b
//            b.points2().forEach { p2 ->
//                val br = byPoint2.at(p2)
//                val ip = br.binarySearchBy(b.zRange.first) { it.zRange.first }
////                println("Before insert: $br")
////                br.forEach { bb -> println("$bb: ${bb.zRange}")}
////                println(ip)
//                check(ip < 0)
//                br.add(-ip - 1, b)
////                println("After insert: $br")
//            }
//        }
//
//        fun fallDz(b: Brick): Int? {
////            byPoint2.forEach {
////                it.value.zipWithNext().forEach { (a, c) -> check(a.zRange.last < c.zRange.first) { it } }
////            }
//
//            val freeZ = b.points2().maxOf { p2 ->
//                val bb = byPoint2.at(p2)
//                check(b in bb)
//                bb
//                    .takeWhile { it != b }
//                    .lastOrNull()
//                    ?.zRange?.last?.let { it + 1 } ?: 1
//            }
////            println("$b -> freeZ: $freeZ")
//            return (b.zRange.first - freeZ).takeIf { it > 0 }
//        }
//
//        fun fallNextBrick(): Boolean {
//            val result = newBricks.firstNotNullOfOrNull { b -> fallDz(b)?.let { b to it } }
//                ?: return false
//
//            val (oldBrick, dz) = result
//            remove(oldBrick)
//            val newBrick = oldBrick.fallBy(dz)
////            println("Falling $oldBrick by $dz as $newBrick")
//            add(newBrick)
//            return true
//        }
//    }
//
//    fun Snapshot.makeFall(): Snapshot {
//        val faller = BrickFaller(this)
//        while (faller.fallNextBrick()) {
//        }
////        faller.newBricks.forEach { b -> println(b) }
//        return Snapshot(faller.newBricks.toList())
//    }
//
//    fun Snapshot.disintegratable(): List<Brick> {
//        return bricks.filter { b ->
////            println("Checking if $b can be disintegrated")
//            val faller = BrickFaller(this)
//            faller.remove(b)
//            !faller.fallNextBrick()
//        }
//    }
//
//    fun part1(input: List<String>): Int {
//        val snapshot = input.parseSnapshot()
//        println("Falling in progress...")
//        val fallenSnapshot = snapshot.makeFall()
//        println("Disintegrating in progress...")
//        val extractable = fallenSnapshot.disintegratable()
//        println(extractable)
//        return extractable.size
//    }
//
//    fun part2(input: List<String>): Long {
//        return 0
//    }
//
//    // TESTS
//    val test1 = part1(readInput("$day/test"))
//    5.let { check(test1 == it) { "Test 1: is $test1, should be $it" } }
//
//    val test2 = part2(readInput("$day/test"))
//    0L.let { check(test2 == it) { "Test 2: is $test2, should be $it" } }
//
//    // RESULTS
//    val input = readInput("$day/input")
//    val part1 = part1(input)
//    0.let { println("Part 1: $part1" + if (part1 == it) "" else " (should be $it?)") }
//    val part2 = part2(input)
//    println("Part 2: $part2")
//}
