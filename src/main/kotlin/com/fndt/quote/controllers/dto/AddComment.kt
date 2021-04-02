package com.fndt.quote.controllers.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AddComment(
    @SerialName("body") val commentBody: String,
)
