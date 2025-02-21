package si.pocketalbum

import android.content.Context

class UserException(private val userMessage: Int, cause: Throwable? = null) : Exception(cause) {
    fun getMessage(ctx: Context): String {
        return ctx.getString(userMessage)
    }
}