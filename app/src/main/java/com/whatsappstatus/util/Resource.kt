package com.whatsappstatus.util

sealed class Resource<T>(val data: T? = null, val message: String? = null) {
    /**
     * should be used as a SingleEvent LiveData
     */
    class InitialValue<T>() : Resource<T>()
    class Success<T>(data: T?) : Resource<T>(data, null)
    class Loading<T>(data: T? = null) : Resource<T>(data, null)
    class Error<T>(message: String?) : Resource<T>(null, message)
    class ShimmersLoading<T>() : Resource<T>(null, null)
}