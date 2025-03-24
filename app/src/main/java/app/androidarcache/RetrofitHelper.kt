package app.androidarcache

import com.google.gson.GsonBuilder
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Streaming
import retrofit2.http.Url

object RetrofitHelper {
    interface APIServices {
        @Streaming
        @GET
        suspend fun downloadFile(@Url fileUrl: String): Response<ResponseBody>
    }

    fun downloadFileAPIService(): APIServices {
        val gson = GsonBuilder()
            .setLenient()
            .create()
        val retrofit = Retrofit.Builder()
            .baseUrl("https://github.com/KhronosGroup/glTF-Sample-Models/raw/refs/heads/main/2.0/DamagedHelmet/glTF-Binary/DamagedHelmet.glb/")
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
        return  retrofit.create(APIServices::class.java)
    }}
