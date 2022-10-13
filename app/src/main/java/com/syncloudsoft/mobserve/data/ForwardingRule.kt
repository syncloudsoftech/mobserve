package com.syncloudsoft.mobserve.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "forwarding_rules")
data class ForwardingRule(

    @ColumnInfo(name = "event") var event: String,
    @ColumnInfo(name = "name") var name: String,
    @ColumnInfo(name = "direction") var direction: String,
    @ColumnInfo(name = "participant_pattern") var participantPattern: String?,
    @ColumnInfo(name = "content_pattern") var contentPattern: String?,
    @ColumnInfo(name = "webhook_url") var webhookUrl: String,
    @ColumnInfo(name = "_id") @PrimaryKey var id: Int? = null,
)
