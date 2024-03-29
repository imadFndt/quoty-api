package com.fndt.quote

import com.fndt.quote.requests.*
import com.fndt.quote.rest.dto.AddComment
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.server.testing.*
import io.ktor.util.*
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

// Covers add comment get comment ban temporary
@InternalAPI
class CommentsApplicationTest {
    private val regularCredentials = "regular:a"
    private val moderatorCredentials = "moderator:a"

    val quoteId = 1

    @Test
    fun `add comment`() = withTestApplication(Application::module) {
        val commentBody = "NewComment"
        addComment(quoteId, AddComment(commentBody), regularCredentials).run {
            print(response.content)
            assertEquals(HttpStatusCode.OK, response.status())
        }
        getComments(quoteId, regularCredentials).run {
            assertTrue(this.find { it.body == commentBody } != null)
        }
    }

    @Test
    fun `ban and comment`(): Unit = withTestApplication(Application::module) {
        val (newLogin, newPassword) = "b" to "b"
        register(newLogin, newPassword)

        val credentials = "$newLogin:$newPassword"
        val user = getUserByCredentials(credentials)

        banUser(user.id, moderatorCredentials).run {
            assertEquals(HttpStatusCode.OK, response.status())
        }

        addComment(quoteId, AddComment("b"), credentials).run {
            assertEquals(HttpStatusCode.Unauthorized, response.status())
        }
    }
}
