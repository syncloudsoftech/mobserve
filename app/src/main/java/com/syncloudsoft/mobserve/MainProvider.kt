package com.syncloudsoft.mobserve

import android.content.Context
import com.vaibhavpandey.katora.contracts.MutableContainer
import com.vaibhavpandey.katora.contracts.Provider

class MainProvider(private val context: Context) : Provider {

    override fun provide(container: MutableContainer) {
        container.factory(Context::class.java) { context }
    }
}
