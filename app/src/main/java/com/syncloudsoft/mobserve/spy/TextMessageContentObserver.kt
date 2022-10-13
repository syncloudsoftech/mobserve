package com.syncloudsoft.mobserve.spy

import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.Handler
import com.syncloudsoft.mobserve.data.ForwardingRule
import com.syncloudsoft.mobserve.ui.ForwardingRuleViewModel
import timber.log.Timber

class TextMessageContentObserver(
    private val context: Context,
    private val forwardingRules: Array<ForwardingRule>,
    handler: Handler
) : BaseObserver(handler, context, URI, PROJECTION, LAST_TS_COLUMN, LAST_TS_PREFERENCE) {

    override fun processRow(cursor: Cursor) {
        val direction = if (cursor.getInt(0) == 1)
            ForwardingRuleViewModel.DIRECTION_INCOMING
        else
            ForwardingRuleViewModel.DIRECTION_OUTGOING
        val participant = cursor.getString(1)
        Timber.v("Processing %s call from/to %s.", direction, participant)
        val content = cursor.getString(2)
        val date = cursor.getLong(3)
        val event = Event("sms", direction, participant, date, content = content)
        forwardingRules.onEach {
            val match = matchesRule(it, event)
            Timber.v("If matches with %s, %b", it.name, match)
            if (match) notifyWebhook(it, event)
        }
    }

    companion object {

        const val LAST_TS_COLUMN = "date"
        const val LAST_TS_PREFERENCE = "lastTextMessageTs"
        val PROJECTION = arrayOf("type", "address", "body", "date")
        val URI: Uri = Uri.parse("content://sms")
    }
}
