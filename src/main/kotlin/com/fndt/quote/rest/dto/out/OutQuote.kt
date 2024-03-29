package com.fndt.quote.rest.dto.out

import com.fndt.quote.domain.dto.Author
import com.fndt.quote.domain.dto.Quote
import com.fndt.quote.domain.dto.Tag
import com.fndt.quote.rest.UrlSchemeProvider
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class OutQuote(
    val id: Int,
    val body: String,
    val author: Author,
    @SerialName("created_at") val createdAt: Long,
    val user: OutUser,
    val likes: Int = 0,
    val tags: List<Tag>,
    @SerialName("is_public") val isPublic: Boolean = false,
    @SerialName("did_i_like") val didILike: Boolean = false,
)

fun Quote.toOutQuote(urlProvider: UrlSchemeProvider): OutQuote {
    return OutQuote(id, body, author, createdAt, user.toOutUser(urlProvider), likes, tags, isPublic, didILike)
}
