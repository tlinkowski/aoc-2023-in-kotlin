import java.math.BigInteger
import java.security.MessageDigest
import kotlin.io.path.Path
import kotlin.io.path.readLines
import kotlin.math.min

/**
 * Reads lines from the given input txt file.
 */
fun readInput(name: String) = Path("src/$name.txt").readLines()

/**
 * Converts string to md5 hash.
 */
fun String.md5() = BigInteger(1, MessageDigest.getInstance("MD5").digest(toByteArray()))
    .toString(16)
    .padStart(32, '0')

fun String.toLongs() = split(" ").filter { it.isNotBlank() }.map { it.trim().toLong() }

fun LongRange.offsetBy(offset: Long) = LongRange(
    start = start + offset,
    endInclusive = endInclusive + offset
)

operator fun LongRange.contains(other: LongRange) = first <= other.first && other.last <= last

fun gcd(a: Long, b: Long) = (1..min(a, b)).reversed()
    .first { a % it == 0L && b % it == 0L }

fun lcm(a: Long, b: Long) = a / gcd(a, b) * b

fun <T> Sequence<T>.takeWhileInclusive(predicate: (T) -> Boolean) = sequence {
    with(iterator()) {
        while (hasNext()) {
            val next = next()
            yield(next)
            if (!predicate(next)) break
        }
    }
}

inline fun <reified E : Enum<E>> E.prev(): E = E::class.java.enumConstants.let { it[(ordinal + it.size - 1) % it.size] }

inline fun <reified E : Enum<E>> E.next(): E = E::class.java.enumConstants.let { it[(ordinal + 1) % it.size] }
