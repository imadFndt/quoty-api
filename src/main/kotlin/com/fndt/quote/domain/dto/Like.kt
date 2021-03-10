package com.fndt.quote.domain.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Like(
    @SerialName("quote_id") val quoteId: Int,
    @SerialName("like_action") val likeAction: Boolean,
)
