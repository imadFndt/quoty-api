package com.fndt.quote.rest.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PermanentBan(@SerialName("user_id") val userId: Int)
