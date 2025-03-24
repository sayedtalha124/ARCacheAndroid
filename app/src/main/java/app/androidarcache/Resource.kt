package app.androidarcache

import android.R.attr.data

data class Resource<out T>(val status: Status, val data: T?, val message: String?) {
    companion object {
        fun <T> success(data: T?): Resource<T> =
            Resource(status = Status.DOWNLOADED, data = data, message = null)

        fun <T> error(message: String?): Resource<T> =
            Resource(status = Status.ERROR, data = null, message = message ?: "Server error")

        fun <T> loading(data: T?): Resource<T> =
            Resource(status = Status.PROGRESS, data = data, message = null)



    }
}
enum class Status {
    DOWNLOADED,
    ERROR,
    PROGRESS
}