package com.syncloudsoft.mobserve.spy

import android.content.Context
import android.database.ContentObserver
import android.os.Handler
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkRequest
import androidx.work.workDataOf
import com.syncloudsoft.mobserve.data.ForwardingRule
import com.syncloudsoft.mobserve.ui.ForwardingRuleViewModel
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.util.regex.Pattern

abstract class BaseObserver(
    private val appContext: Context,
    handler: Handler
) : ContentObserver(handler) {

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
            .getInstance(appContext)
            .enqueue(request)
    }
}
