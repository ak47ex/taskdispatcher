import com.suenara.taskdispatcher.api.TaskDispatcher
import com.suenara.taskdispatcher.impl.AsyncMeasurableTaskDispatcher
import com.suenara.taskdispatcher.impl.ForkJoinMeasurableTaskDispatcher
import com.suenara.taskdispatcher.impl.NaiveFjpMeasurableTaskDispatcher
import com.suenara.taskdispatcher.impl.SequentialMeasurableTaskDispatcher
import java.security.spec.KeySpec
import java.util.concurrent.ForkJoinPool
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit
import javax.crypto.SecretKey
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

@ExperimentalStdlibApi
fun main(args: Array<String>) {
    println("Warmup")
    //warmup
    heavyCalculate(35)
    heavyCalculate(100)
    heavyCalculate(11)

    val corePoolSize = Runtime.getRuntime().availableProcessors() * 2 - 1
    val maxPoolSize = Runtime.getRuntime().availableProcessors() * 2 - 1
    val executorService = ThreadPoolExecutor(corePoolSize, maxPoolSize,
        0L, TimeUnit.MILLISECONDS,
        LinkedBlockingQueue()
    )
    executorService.prestartAllCoreThreads()
    val commonPool = ForkJoinPool.commonPool()
    (0..<corePoolSize).flatMap {
        listOf(
            executorService.submit { Thread.sleep(100L) },
            commonPool.submit { Thread.sleep(100L) }
        )
    }.all { it.get(); true }

    println("Run benchmark!")
    listOf(
        { SequentialMeasurableTaskDispatcher() },
        { AsyncMeasurableTaskDispatcher(executorService) },
        { NaiveFjpMeasurableTaskDispatcher() },
        { ForkJoinMeasurableTaskDispatcher() },
    ).forEach {
        benchmark(it)
    }
}

@OptIn(ExperimentalStdlibApi::class)
private fun benchmark(taskDispatcherProvider: () -> TaskDispatcher) {
    var name = ""
    var min = Long.MAX_VALUE
    var max = Long.MIN_VALUE
    val times = Array<Long>(N) { 0 }
    val total = Array<Long>(N) { 0 }
    val timeUnit = TimeUnit.NANOSECONDS

    repeat(N) {
        val dispatcher: TaskDispatcher = taskDispatcherProvider()
        name = dispatcher.javaClass.simpleName

        val start = System.nanoTime()
        val first = dispatcher.dispatch("first") {
            heavyCalculate(150)
        }
        val second = dispatcher.dispatch("second") {
            heavyCalculate(50)
        }
        val third = dispatcher.dispatch("third") {
            heavyCalculate(15)
        }
        val fourth = dispatcher.dispatch("fourth", listOf(third, second)) {
            heavyCalculate(230)
        }
        val fifth = dispatcher.dispatch("fifth", listOf(first, third)) {
            heavyCalculate(11)
        }
        val sixth = dispatcher.dispatch("sixth", listOf(fourth)) {
            heavyCalculate(200)
        }
        val seventh = dispatcher.dispatch("seventh", listOf(second)) {
            heavyCalculate(80)
        }
        val eighth = dispatcher.dispatch("eighth") {
            heavyCalculate(10)
        }

        val ninth = dispatcher.dispatch("ninth", listOf(first, fifth, eighth, seventh)) {
            heavyCalculate(29)
        }
        val tenth = dispatcher.dispatch("ninth", listOf(sixth, ninth)) {
            heavyCalculate(900)
        }
        dispatcher.awaitCompletion()
        val totalDuration = System.nanoTime() - start
        total[it] = totalDuration
        times[it] = dispatcher.durationsSum
        max = maxOf(max, dispatcher.durationsSum)
        min = minOf(min, dispatcher.durationsSum)
    }

    val result = """$name performs:
        |    min = ${timeUnit.toMillis(min)}ms
        |    max = ${timeUnit.toMillis(max)}ms
        |    averageExecution = ${timeUnit.toMillis(times.average().toLong())}ms
        |    averageTotal = ${timeUnit.toMillis(total.average().toLong())}ms
    """.trimMargin()

    println(result)
}


@ExperimentalStdlibApi
private fun heavyCalculate(seed: Int) {
    val password = buildString {
        (seed downTo 0).forEach {
            append((33 + it).toChar())
        }
    }
    generateHash(password, SALT)
}
private const val N = 10
private const val ALGORITHM = "PBKDF2WithHmacSHA512"
private const val ITERATIONS = 120_000
private const val KEY_LENGTH = 256
private const val SECRET = "SomeRandomSecret"
private const val SALT = "3#12-(L,xc¡¡™ˆ˜ππ¬œ∑"
val SECRET_KEY_FACTORY: SecretKeyFactory = SecretKeyFactory.getInstance(ALGORITHM)

@ExperimentalStdlibApi
fun generateHash(password: String, salt: String): String {
    val combinedSalt = "$salt$SECRET".toByteArray()
    val spec: KeySpec = PBEKeySpec(password.toCharArray(), combinedSalt, ITERATIONS, KEY_LENGTH)
    val key: SecretKey = SECRET_KEY_FACTORY.generateSecret(spec)
    val hash: ByteArray = key.encoded

    return hash.toHexString()
}