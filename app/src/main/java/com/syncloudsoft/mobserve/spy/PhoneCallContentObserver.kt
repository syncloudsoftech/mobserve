package com.syncloudsoft.mobserve.spy

import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.Handler
import android.provider.CallLog
import androidx.preference.PreferenceManager
import com.syncloudsoft.mobserve.data.ForwardingRule
import com.syncloudsoft.mobserve.ui.ForwardingRuleViewModel
import timber.log.Timber
import java.lang.Exception

class PhoneCallContentObserver(
    private val appContext: Context,
    private val forwardingRules: Array<ForwardingRule>,
    handler: Handler
) : BaseObserver(appContext, handler) {

    override fun onChange(selfChange: Boolean) {
        Timber.v("Looks like something changed in %s", URI)
        val sharedPreferences =
            PreferenceManager.getDefaultSharedPreferences(appContext)
        var lastTs = sharedPreferences.getLong(LAST_TS_PREF, System.currentTimeMillis())
        try {
            Timber.v("Last TS %d; current TS %d.", lastTs, System.currentTimeMillis())
            appContext.contentResolver.query(
                URI, PROJECTION, "date > ?", arrayOf("$lastTs"),
                "date DESC"
            ).use { cursor ->
                if (cursor != null && cursor.moveToFirst()) {
                    lastTs = cursor.getLong(3)
                    do processRow(cursor) while (cursor.moveToNext())
                } else {
                    Timber.w("No new data found in %s.", URI)
                }
            }
        } catch (e: Exception) {
            Timber.e(e, "Could not observe change in %s.", URI)
        }

        sharedPreferences.edit()
            .putLong(LAST_TS_PREF, lastTs)
            .apply();
    }

    private fun processRow(cursor: Cursor) {
        val direction = when (cursor.getInt(0)) {
            CallLog.Calls.INCOMING_TYPE -> ForwardingRuleViewModel.DIRECTION_INCOMING
            CallLog.Calls.OUTGOING_TYPE -> ForwardingRuleViewModel.DIRECTION_OUTGOING
            else -> ForwardingRuleViewModel.DIRECTION_MISSED
        }
        val participant = cursor.getString(1)
        Timber.v("Processing %s message from/to %s.", direction, participant)
        val duration = cursor.getLong(2)
        val date = cursor.getLong(3)
        val event = Event("call", direction, participant, date, duration = duration)
        forwardingRules.onEach {
            val match = matchesRule(it, event)
            Timber.v("If matches with %s, %b", it.name, match)
            if (match) notifyWebhook(it, event)
        }
    }

    companion object {

        const val LAST_TS_PREF = "lastPhoneCallTs"
        val PROJECTION = arrayOf(
            CallLog.Calls.TYPE,
            CallLog.Calls.NUMBER,
            CallLog.Calls.DURATION,
            CallLog.Calls.DATE,
        )
        val URI: Uri = CallLog.Calls.CONTENT_URI
    }
}
