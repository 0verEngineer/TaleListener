import kotlinx.coroutines.delay
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant

suspend fun <T> withMinimumTime(
    minimumTimeMillis: Long,
    block: suspend () -> T,
): T {
    val startTime: Instant = Clock.System.now()
    var result: T = block()
    val elapsedTime = Clock.System.now().minus(startTime).inWholeMilliseconds
    val remainingTime = minimumTimeMillis - elapsedTime
    if (remainingTime > 0) {
        delay(remainingTime)
    }
    return result
}
