package com.syncloudsoft.mobserve.spy

import android.content.Context
import android.database.Cursor
import android.os.Handler
import android.provider.CallLog
import com.syncloudsoft.mobserve.data.ForwardingRule
import com.syncloudsoft.mobserve.ui.ForwardingRuleViewModel
import timber.log.Timber

class PhoneCallContentObserver(
    context: Context,
    private val forwardingRules: Array<ForwardingRule>,
    handler: Handler
) : BaseObserver(handler, context, URI, PROJECTION, LAST_TS_COLUMN, LAST_TS_PREFERENCE) {

    override fun processRow(cursor: Cursor) {
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

        const val LAST_TS_COLUMN = CallLog.Calls.DATE
        const val LAST_TS_PREFERENCE = "lastPhoneCallTs"
        val PROJECTION = arrayOf(
            CallLog.Calls.TYPE,
            CallLog.Calls.NUMBER,
            CallLog.Calls.DURATION,
            CallLog.Calls.DATE,
        )
        val URI = CallLog.Calls.CONTENT_URI
    }
}
