package ru.mephi.userservice.annotation

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class TimeHttpRequest(val method: String = "", val uri: String = "")

