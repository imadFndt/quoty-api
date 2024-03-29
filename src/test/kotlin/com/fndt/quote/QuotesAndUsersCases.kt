package com.fndt.quote

import com.fndt.quote.domain.dto.AuthRole
import com.fndt.quote.domain.filter.Access
import com.fndt.quote.requests.*
import com.fndt.quote.rest.dto.LikeRequest
import com.fndt.quote.rest.dto.out.OutQuote
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.server.testing.*
import io.ktor.util.*
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull

@InternalAPI
class QuotesAndUsersCases {

    private val regularCredentials = "regular:a"
    private val moderatorCredentials = "moderator:a"

    @Test
    fun registration() = withTestApplication(Application::module) {
        register("a", "a").run {
            assertEquals(HttpStatusCode.OK, response.status())
        }
    }

    @Test
    fun auth() = withTestApplication(Application::module) {
        getUserByCredentials(regularCredentials).run {
            assertEquals(AuthRole.REGULAR, role)
        }

        getUserByCredentials(moderatorCredentials).run {
            assertEquals(AuthRole.MODERATOR, role)
        }
    }

    @Test
    fun `quotes use cases`() = withTestApplication(Application::module) {
        val newBody = "New quote"
        val newAuthor = "Barash"
        val listBefore = getQuotes(createSearchMap(access = Access.ALL), moderatorCredentials)

        sendNewQuote(newBody, newAuthor, regularCredentials).run {
            assertEquals(HttpStatusCode.OK, response.status())
        }

        sendLike(LikeRequest(6, likeAction = true), regularCredentials).run {
            assertEquals(HttpStatusCode.OK, response.status())
        }

        val listAfterModerator = getQuotes(createSearchMap(access = Access.ALL), moderatorCredentials)
        val result = listAfterModerator.quotes subtract listBefore.quotes
        val quote = result.firstOrNull()

        assertNotNull(quote)
        assertTrue(result.size == 1 && quote.run { body == newBody && author.name == newAuthor && likes == 1 && !didILike })

        approveQuote(quote, moderatorCredentials)

        val listAfterRegular = getQuotes(createSearchMap(access = Access.PUBLIC), regularCredentials)
        val updatedQuote = listAfterRegular.quotes.find { it.id == quote.id }
        assertNotNull(updatedQuote)
        assertEquals(true, updatedQuote.isPublic)
    }

    @Test
    fun `random quote`() = withTestApplication(Application::module) {
        val regularQuotes = getQuotesOfTheDay(regularCredentials)
        val regularQuotesSet = mutableSetOf(regularQuotes)
        assertTrue { regularQuotesSet.size == 1 }

        val moderatorQuotes = getQuotesOfTheDay(moderatorCredentials)
        val moderatorQuotesSet = mutableSetOf(moderatorQuotes)
        assertTrue { moderatorQuotesSet.size == 1 }
    }

    private fun TestApplicationEngine.getQuotesOfTheDay(credentials: String): List<OutQuote> {
        val list = mutableListOf<OutQuote>()
        for (i in 0..100) {
            val quote = getQuoteOfTheDay(credentials)
            list.add(quote)
        }
        return list
    }
}
