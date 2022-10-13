package com.syncloudsoft.mobserve.spy

import kotlinx.serialization.Serializable

@Serializable
data class Event(
    val event: String,
    val direction: String,
    val participant: String,
    val date: Long,
    val content: String? = null,
    val duration: Long? = null
)
