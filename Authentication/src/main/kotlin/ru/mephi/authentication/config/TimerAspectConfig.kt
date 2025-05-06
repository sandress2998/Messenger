package ru.mephi.authentication.config

import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.Timer
import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.springframework.stereotype.Component
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import ru.mephi.authentication.annotation.TimeHttpRequest
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit


@Aspect
@Component
class TimerAspectConfig(
    private val meterRegistry: MeterRegistry
) {
    companion object {
        private val httpRequestTimers = ConcurrentHashMap<String, Timer>()
    }

    fun setHttpRequestTimer(uri: String) {
        httpRequestTimers[uri] = getTimer(
            "http.request.time",
            "Time taken to process HTTP requests",
            "layer", "controller",
            "uri", uri
        )
    }

    private fun getTimer(vararg tags: String): Timer =
        Timer.builder("http.request.time")
            .description("Time taken to process HTTP requests")
            .tags(*tags)
            .register(meterRegistry)

    @Around("@annotation(timeHttpRequest)")
    fun measureHttpRequest(
        joinPoint: ProceedingJoinPoint,
        timeHttpRequest: TimeHttpRequest
    ): Any? {
        val timer = httpRequestTimers[timeHttpRequest.uri] ?: return joinPoint.proceed()

        return measureTime(joinPoint, timer)
    }

    private fun measureTime(joinPoint: ProceedingJoinPoint, timer: Timer): Any? {
        return when (val result = joinPoint.proceed()) {
            is Mono<*> -> {
                val startTime = System.nanoTime()
                result
                    .doOnSubscribe {
                        // Дополнительные метрики при необходимости
                    }
                    .doOnSuccess {
                        timer.record(System.nanoTime() - startTime, TimeUnit.NANOSECONDS)
                    }
                    .doOnError {
                        timer.record(System.nanoTime() - startTime, TimeUnit.NANOSECONDS)
                    }
            }

            is Flux<*> -> {
                val startTime = System.nanoTime()
                result
                    .doOnSubscribe {
                        // Дополнительные метрики при необходимости
                    }
                    .doOnComplete {
                        timer.record(System.nanoTime() - startTime, TimeUnit.NANOSECONDS)
                    }
                    .doOnError {
                        timer.record(System.nanoTime() - startTime, TimeUnit.NANOSECONDS)
                    }
            }

            else -> timer.recordCallable { result }
        }
    }
}



/* Это некорректно для WebFlux, но корректно для Spring MVC
@Aspect
@Component
class TimerAspectConfig(
    private val applicationProperties: ApplicationProperties,
    private val meterRegistry: MeterRegistry
) {
    companion object {
        private val httpRequestTimers = ConcurrentHashMap<String, Timer>()

        private val databaseSQLTimers = ConcurrentHashMap<String, Timer>()

        private val databaseNoSQLTimers = ConcurrentHashMap<String, Timer>()

        private val businessTimers = ConcurrentHashMap<String, Timer>()
    }

    fun setHttpRequestTimer(uri: String) {
        httpRequestTimers[uri] = getTimer(
            "http.request.time",
            "Time taken to process HTTP requests",
            "layer", "controller",
            "uri", uri,
            "application_name", applicationProperties.applicationName ?: "Unknown"
        )
    }

    fun setNoSQLDatabaseTimer(functionName: String){
        businessTimers[functionName] = getTimer(
            "db.query.time",
            "Time taken to execute database queries",
            "type", "nosql",
            "operation", functionName,
            "application_name", applicationProperties.applicationName ?: "Unknown"
        )
    }

    fun setSQLDatabaseTimer(functionName: String){
        businessTimers[functionName] = getTimer(
            "db.query.time",
            "Time taken to execute database queries",
            "type", "sql",
            "operation", functionName,
            "application_name", applicationProperties.applicationName ?: "Unknown"
        )
    }

    fun setBusinessTimer(functionName: String) {
        businessTimers[functionName] = getTimer(
            "business.operation.time",
            "Time taken to execute business operations",
            "operation", functionName,
            "application_name", applicationProperties.applicationName ?: "Unknown"
        )
    }

    private fun getTimer(name: String, description: String, vararg tags: String): Timer =
        Timer.builder(name)
            .description(description)
            .tags(*tags)
            .register(meterRegistry)

    @Around("@annotation(timeHttpRequest)")
    fun measureHttpRequest(
        joinPoint: ProceedingJoinPoint,
        timeHttpRequest: TimeHttpRequest
    ): Any? {
        return httpRequestTimers[timeHttpRequest.uri]?.recordCallable { joinPoint.proceed() }
    }

    @Around("@annotation(timeNoSQLOperation)")
    fun measureDatabaseNoSQLQuery(
        joinPoint: ProceedingJoinPoint,
        timeNoSQLOperation: TimeDatabaseNoSQLQuery
    ): Any? {
        return databaseNoSQLTimers[timeNoSQLOperation.value]?.recordCallable { joinPoint.proceed() }
    }

    @Around("@annotation(timeSQLOperation)")
    fun measureDatabaseSQLQuery(
        joinPoint: ProceedingJoinPoint,
        timeSQLOperation: TimeDatabaseSQLQuery
    ): Any? {
        // joinPoint.signature.name - не очень хорошо, потому что надо будет создавать метрики динамически
        return databaseSQLTimers[timeSQLOperation.value]?.recordCallable { joinPoint.proceed() }
    }

    @Around("@annotation(timeBusinessOperation)")
    fun measureBusinessOperation(
        joinPoint: ProceedingJoinPoint,
        timeBusinessOperation: TimeBusinessOperation
    ): Any? {
        return businessTimers[timeBusinessOperation.value]?.recordCallable { joinPoint.proceed() }
    }
}
*/
