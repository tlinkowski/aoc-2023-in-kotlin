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

infix fun LongRange.intersect(other: LongRange) = LongRange(
    start = kotlin.math.max(start, other.start),
    endInclusive = kotlin.math.min(endInclusive, other.endInclusive)
)

fun LongRange.offsetBy(offset: Long) = LongRange(
    start = start + offset,
    endInclusive = endInclusive + offset
)

fun LongRange.contains(other: LongRange) = start <= other.start && other.endInclusive <= endInclusive

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
