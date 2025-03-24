package arCore

import android.app.Dialog
import android.content.Context
import android.view.Window
import android.view.WindowManager
import android.widget.ProgressBar
import android.widget.TextView
import app.androidarcache.R

class DownloadAR {
    var progressBar: ProgressBar? = null
    var tvValue: TextView? = null
    var dialog: Dialog? = null


    fun progressDialog(context: Context): Dialog {
        dialog = Dialog(context)
        dialog?.apply {
            requestWindowFeature(Window.FEATURE_NO_TITLE)

            setCancelable(false)
            setContentView(R.layout.dialog)
            progressBar =
                findViewById<(ProgressBar)>(R.id.progress_horizontal)
            tvValue = findViewById<TextView>(R.id.value123)
            show()
            window?.    setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT)

        }

        return dialog!!
    }

    fun dismiss() {
        dialog?.dismiss()
    }


}