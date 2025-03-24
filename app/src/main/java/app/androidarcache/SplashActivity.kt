package app.androidarcache

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import app.androidarcache.arCore.DownloadAR

class SplashActivity : AppCompatActivity() {
    private val downloadVM: DownloadVM by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_splash)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        var dialog = DownloadAR()

        downloadVM.downloadAndSaveModel()
        dialog?.progressDialog(this)
        downloadVM.fileDownloadMutableLiveData.observe(this) {
            if (it != null) {
                when (it.status) {
                    Status.PROGRESS -> {
                        runOnUiThread {
                            it.data?.let {
                                dialog.progressBar?.progress = it.progress ?: 0
                                dialog.tvValue?.text = "Downloading... " + it.progress.toString()

                            }

                        }
                    }

                    Status.DOWNLOADED -> {
                        runOnUiThread {
                            dialog.dismiss()
                            it.data?.let {
                                Intent(this, MainActivity::class.java).apply {
                                    putExtra("fileName", it.fileName)
                                    startActivity(this)
                                    finish()
                                }
                            }

                        }
                    }

                    Status.ERROR -> {
                        Toast.makeText(this, it.message, Toast.LENGTH_SHORT).show()

                    }

                    else -> {


                    }


                }
            }

        }
    }
}