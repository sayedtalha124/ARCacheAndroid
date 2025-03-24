package app.androidarcache

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

class DownloadVM(
    application: Application,
) : AndroidViewModel(application = application) {
    var modelUrl =
        "https://github.com/KhronosGroup/glTF-Sample-Models/raw/refs/heads/main/2.0/DamagedHelmet/glTF-Binary/DamagedHelmet.glb/"
    var fileName = "model1.glb"
    fun downloadAndSaveModel(
    ) {

        var context = getApplication<Application>().applicationContext
        val cacheDir = File(context?.cacheDir, "models")
        if (!cacheDir.exists()) cacheDir.mkdirs()
        //saving model in temporary file when download completes we will save it in modelFile.
        val tempFile = File(cacheDir, "temp.glb")
        val modelFile = File(cacheDir, fileName)
        // ✅ If fileName already cached, return it
        if (modelFile.exists()) {
            fileDownloadMutableLiveData.postValue(
                Resource.success(
                    DownloadProgressModel(
                        101,
                        fileName
                    )
                )
            )
            return
        }
        fileDownloadMutableLiveData.postValue(Resource.success(DownloadProgressModel(0)))

        val apiService = RetrofitHelper.downloadFileAPIService()
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val body = apiService.downloadFile(modelUrl).body()
                if (body != null) {
                    var input: InputStream? = null
                    try {
                        input = body.byteStream()
                        val fos = FileOutputStream(tempFile.path)
                        fos.use { output ->
                            val buffer = ByteArray(4 * 1024) // or other buffer size
                            var read: Int
                            var totalBytes = 0
                            val fileSize = body.contentLength()
                            while (input.read(buffer).also { read = it } != -1) {
                                output.write(buffer, 0, read)
                                totalBytes += read
                                val progress = ((totalBytes * 100) / fileSize).toInt()
                                fileDownloadMutableLiveData.postValue(
                                    Resource.loading(
                                        DownloadProgressModel(progress)
                                    )
                                )
                            }


                            output.flush()
                        }
                    } catch (e: Exception) {
                        Log.e("saveFile", e.toString())
                        fileDownloadMutableLiveData.postValue(Resource.error(e.toString()))
                    } finally {
                        input?.close()
                    }.let {
                        var file = tempFile.copyTo(modelFile)
                        if (file.exists()) {
                            tempFile.delete()
                        }


                        fileDownloadMutableLiveData.postValue(
                            Resource.success(
                                DownloadProgressModel(
                                    101,
                                    fileName
                                )
                            )
                        )

                    }
                }
            }

        }


    }

    var fileDownloadMutableLiveData = MutableLiveData<Resource<DownloadProgressModel>>()


}