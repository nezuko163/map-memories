package com.nezuko.common

sealed class ResultModel<out T> {
    data class Success<out T>(val data: T) : ResultModel<T>()
    data class Failure<out T>(val e: Exception, val data: T? = null) : ResultModel<T>()
    class Loading<out T> : ResultModel<T>()
    class None<out T> : ResultModel<T>()

    companion object {
        fun <T> success(data: T): ResultModel<T> = Success(data)
        fun <T> failure(e: Exception, data: T? = null): ResultModel<T> = Failure(e, data)
        fun <T> failure(message: String?, data: T? = null): ResultModel<T> =
            Failure(Exception(message), data)

        fun <T> loading(): ResultModel<T> = Loading()
        fun <T> none(): ResultModel<T> = None()
    }
}