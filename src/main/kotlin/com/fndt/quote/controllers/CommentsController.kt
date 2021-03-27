package com.fndt.quote.controllers

import com.fndt.quote.controllers.dto.AddComment
import com.fndt.quote.controllers.factory.CommentsUseCaseFactory
import com.fndt.quote.controllers.util.*
import io.ktor.auth.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*

class CommentsController(private val useCaseFactory: CommentsUseCaseFactory) : RoutingController {
    override fun route(routing: Routing) = routing.routePathWithAuth(COMMENTS_ENDPOINT) {
        getComments()
        addComment()
    }

    private fun Route.getComments() {
        getExt { principal ->
            getAndCheckIntParameter(ID)?.let {
                respond(useCaseFactory.getCommentsUseCase(it, principal.user).run())
            }
        }
    }

    private fun Route.addComment() {
        postExt { principal ->
            val quoteId = getAndCheckIntParameter(ID) ?: run {
                respondText(PARAMETER_FAIL, status = HttpStatusCode.BadRequest)
                return@postExt
            }
            val (body) = receiveCatching<AddComment>() ?: run {
                respondText(PARAMETER_FAIL, status = HttpStatusCode.BadRequest)
                return@postExt
            }
            val result = useCaseFactory.addCommentsUseCase(body, quoteId, principal.user).run()
            respond(result)
        }
    }
}
