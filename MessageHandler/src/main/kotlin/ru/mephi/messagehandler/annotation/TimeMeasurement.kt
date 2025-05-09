package ru.mephi.messagehandler.annotation

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class TimeHttpRequest(val method: String = "", val uri: String = "")