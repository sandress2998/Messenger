package ru.mephi.websocket.config

import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.Timer
import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.springframework.stereotype.Component
import ru.mephi.websocket.annotation.TimeBusinessOperation
import java.util.concurrent.ConcurrentHashMap

@Aspect
@Component
class TimerAspectConfig(private val meterRegistry: MeterRegistry) {
    private val dbQueryTimer = getTimer(
        "db.query.time",
        "Time taken to execute database queries",
        "type", "sql"
    )

    // Для бизнес-операций используем мапу, так как имена динамические
    private val businessTimers = ConcurrentHashMap<String, Timer>()

    fun setBusinessTimer(functionName: String) {
        businessTimers[functionName] = getTimer(
            "business.operation.time",
            "Time taken to execute business operations",
            "operation", functionName
        )
    }

    private fun getTimer(name: String, description: String, vararg tags: String): Timer =
        Timer.builder(name)
            .description(description)
            .tags(*tags)
            .register(meterRegistry)

    @Around("@annotation(ru.mephi.websocket.annotation.TimeDatabaseQuery)")
    fun measureDatabaseQuery(joinPoint: ProceedingJoinPoint): Any? {
        return dbQueryTimer.recordCallable { joinPoint.proceed() }
    }

    @Around("@annotation(timeBusinessOperation)")
    fun measureBusinessOperation(
        joinPoint: ProceedingJoinPoint,
        timeBusinessOperation: TimeBusinessOperation
    ): Any? {
        return businessTimers[timeBusinessOperation.value]?.recordCallable { joinPoint.proceed() }
    }
}