package com.syncloudsoft.mobserve.spy

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.OkHttpClient
import timber.log.Timber
import java.lang.Exception

class PostJsonWorker(appContext: Context, workerParams: WorkerParameters) :
    Worker(appContext, workerParams) {

    override fun doWork(): Result {
        val urlInput =
            inputData.getString(DATA_URL) ?: return Result.failure()
        val json: String =
            inputData.getString(DATA_JSON) ?: return Result.failure()
        Timber.v("Notifying new data to %s", urlInput)
        var code = -1
        try {
            code = postJson(urlInput, json)
        } catch (e: Exception) {
            Timber.e(e, "Failed to notify %s", urlInput)
        }

        Timber.v("URL returned %d status", code)
        return if (code in 200..299) Result.success() else Result.failure()
    }

    private fun postJson(url: String, json: String): Int {
        val request: Request = Request.Builder()
            .url(url)
            .post(json.toRequestBody(MEDIATYPE_JSON))
            .build()
        OkHttpClient()
            .newCall(request)
            .execute().use { response -> return response.code }
    }

    companion object {

        const val DATA_JSON = "json"
        const val DATA_URL = "url"

        private val MEDIATYPE_JSON = "application/json; charset=utf-8".toMediaType()
    }
}
