package com.syncloudsoft.mobserve.spy

import android.content.Context
import android.content.SharedPreferences
import android.database.ContentObserver
import android.database.Cursor
import android.net.Uri
import android.os.Handler
import androidx.preference.PreferenceManager
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkRequest
import androidx.work.workDataOf
import com.syncloudsoft.mobserve.data.ForwardingRule
import com.syncloudsoft.mobserve.ui.ForwardingRuleViewModel
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import timber.log.Timber
import java.lang.Exception
import java.util.regex.Pattern

abstract class BaseObserver(
    handler: Handler,
    private val context: Context,
    private val uri: Uri,
    private val projection: Array<String>,
    private val lastTsColumn: String,
    private val lastTsPreference: String,
) : ContentObserver(handler) {

    private val sharedPreferences: SharedPreferences
        get() = PreferenceManager.getDefaultSharedPreferences(context)

    private var lastTsInPreferences: Long
        get() = sharedPreferences.getLong(lastTsPreference, System.currentTimeMillis())
        set(value) {
            sharedPreferences.edit()
                .putLong(lastTsPreference, value)
                .apply()
        }

    override fun onChange(selfChange: Boolean) {
        Timber.v("Looks like something changed in %s", PhoneCallContentObserver.URI)
        var lastTs = lastTsInPreferences;
        try {
            Timber.v("Last TS %d; current TS %d.", lastTs, System.currentTimeMillis())
            context.contentResolver.query(
                uri, projection, "$lastTsColumn > ?", arrayOf("$lastTs"),
                "$lastTsColumn DESC"
            ).use { cursor ->
                if (cursor != null && cursor.moveToFirst()) {
                    lastTs = cursor.getLong(cursor.getColumnIndexOrThrow(lastTsColumn))
                    do processRow(cursor) while (cursor.moveToNext())
                } else {
                    Timber.w("No new data found in %s.", PhoneCallContentObserver.URI)
                }
            }
        } catch (e: Exception) {
            Timber.e(e, "Could not observe change in %s.", PhoneCallContentObserver.URI)
        }

        lastTsInPreferences = lastTs
    }

    protected fun matchesRule(forwardingRule: ForwardingRule, event: Event) : Boolean {
        if (forwardingRule.event != ForwardingRuleViewModel.EVENT_ALL
            && forwardingRule.event != event.event) {
            return false
        }

        if (forwardingRule.direction != ForwardingRuleViewModel.DIRECTION_ALL
            && forwardingRule.direction != event.direction) {
            return false
        }

        if (!forwardingRule.participantPattern.isNullOrEmpty()
            && !Pattern.matches(forwardingRule.participantPattern!!, event.participant)) {
            return false
        }

        if (event.event == ForwardingRuleViewModel.EVENT_SMS
            && !forwardingRule.contentPattern.isNullOrEmpty()
            && !Pattern.matches(forwardingRule.contentPattern!!, event.content!!)) {
            return false
        }

        return true
    }

    protected fun notifyWebhook(forwardingRule: ForwardingRule, event: Event) {
        val request: WorkRequest =
            OneTimeWorkRequestBuilder<PostJsonWorker>()
                .setInputData(workDataOf(
                    PostJsonWorker.DATA_JSON to Json.encodeToString(event),
                    PostJsonWorker.DATA_URL to forwardingRule.webhookUrl,
                ))
                .build()
        WorkManager
            .getInstance(context)
            .enqueue(request)
    }

    abstract fun processRow(cursor: Cursor)
}
