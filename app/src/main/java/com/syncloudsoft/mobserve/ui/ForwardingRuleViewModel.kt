package com.syncloudsoft.mobserve.ui

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.syncloudsoft.mobserve.R
import com.syncloudsoft.mobserve.data.ForwardingRule

class ForwardingRuleViewModel(forwardingRule: ForwardingRule?) : ViewModel() {

    val event = MutableLiveData<String>(forwardingRule?.event)

    var eventRadio: Int
        get() {
            return when (event.value) {
                EVENT_CALL -> R.id.event_call_radio
                EVENT_SMS -> R.id.event_sms_radio
                else -> R.id.event_all_radio
            }
        }
        set(value) {
            event.value = when (value) {
                R.id.event_call_radio -> EVENT_CALL
                R.id.event_sms_radio -> EVENT_SMS
                else -> EVENT_ALL
            }
        }

    val name = MutableLiveData<String>(forwardingRule?.name)

    val direction = MutableLiveData(forwardingRule?.direction ?: DIRECTION_ALL)

    var directionRadio: Int
        get() {
            return when (direction.value) {
                DIRECTION_INCOMING -> R.id.direction_incoming_radio
                DIRECTION_OUTGOING -> R.id.direction_outgoing_radio
                DIRECTION_MISSED -> R.id.direction_missed_radio
                else -> R.id.direction_all_radio
            }
        }
        set(value) {
            direction.value = when (value) {
                R.id.direction_incoming_radio -> DIRECTION_INCOMING
                R.id.direction_outgoing_radio -> DIRECTION_OUTGOING
                R.id.direction_missed_radio -> DIRECTION_MISSED
                else -> DIRECTION_ALL
            }
        }

    val participantPattern = MutableLiveData<String>(forwardingRule?.participantPattern)

    val contentPattern = MutableLiveData<String>(forwardingRule?.contentPattern)

    val webhookUrl = MutableLiveData<String>(forwardingRule?.webhookUrl)

    companion object {

        const val EVENT_ALL = "all"
        const val EVENT_CALL = "call"
        const val EVENT_SMS = "sms"

        const val DIRECTION_ALL = "all"
        const val DIRECTION_INCOMING = "incoming"
        const val DIRECTION_OUTGOING = "outgoing"
        const val DIRECTION_MISSED = "missed"
    }
}
