package app.androidarcache

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.drawable.ColorDrawable
import android.view.View
import android.widget.ProgressBar
import androidx.core.content.ContextCompat
import app.androidarcache.Utils.Companion.showLog

object ProgressDialog {
    fun showLoadingDialog(context: Context): Dialog {
        val progressDialog = Dialog(context)
        val dialog = ProgressBar(context)


        dialog.isIndeterminate = true
        dialog.visibility = View.VISIBLE
        dialog.indeterminateDrawable
            .setColorFilter(ContextCompat.getColor(context, R.color.white), PorterDuff.Mode.SRC_IN)
        try {
            progressDialog.apply {
                window
                    ?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                setContentView(dialog)
                setCancelable(false)
                val activity = context as Activity
                if (!activity.isFinishing)
                    activity.runOnUiThread {
                        show()
                    }
            }


        } catch (e: Exception) {
            showLog(e.message.toString())
        }
        return progressDialog
    }
}