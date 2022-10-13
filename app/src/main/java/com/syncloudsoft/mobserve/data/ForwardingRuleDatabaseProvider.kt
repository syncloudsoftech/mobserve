package com.syncloudsoft.mobserve.data

import android.content.Context
import androidx.room.Room
import com.vaibhavpandey.katora.contracts.MutableContainer
import com.vaibhavpandey.katora.contracts.Provider

class ForwardingRuleDatabaseProvider : Provider {

    override fun provide(container: MutableContainer) {
        container.singleton(ForwardingRuleDatabase::class.java) {
            Room.databaseBuilder(
                it[Context::class.java],
                ForwardingRuleDatabase::class.java, "forwarding-rules"
            )
                .allowMainThreadQueries()
                .build()
        }
    }
}
