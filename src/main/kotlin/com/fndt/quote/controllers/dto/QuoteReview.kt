package com.fndt.quote.controllers.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class QuoteReview(val decision: Boolean, @SerialName("quote_id") val quoteId: Int)
