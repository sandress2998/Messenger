package ru.mephi.authentication.annotation

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class TimeHttpRequest(val uri: String = "")
