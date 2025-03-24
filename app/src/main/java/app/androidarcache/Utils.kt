package app.androidarcache

import android.util.Log


class Utils {

    companion object {



        const val TAG = "LOG_MESSAGE"
        fun showLog(content: String) {
            val builder: String =
                content
            Log.e(TAG, "showLog: $builder")
        }
    }
}