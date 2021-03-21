package com.fndt.quote.controllers

import com.fndt.quote.controllers.factory.SelectionUseCaseFactory
import com.fndt.quote.controllers.util.*
import io.ktor.http.*
import io.ktor.response.*
import io.ktor.routing.*

class SelectionsController(private val useCaseFactory: SelectionUseCaseFactory) : RoutingController {
    override fun route(routing: Routing) = routing.routePathWithAuth("") {
        getExt(POPULARS_ENDPOINT) { principal -> respond(useCaseFactory.getPopularsUseCase(principal.user).run()) }
        getExt(SEARCH_ENDPOINT) { principal ->
            val query = parameters[QUERY_ARG] ?: run {
                respondText(QUERY_NOT_RECEIVED, status = HttpStatusCode.NotAcceptable)
                return@getExt
            }
            respond(useCaseFactory.getSearchUseCase(query, principal.user).run())
        }
        getExt(TAG_SELECTION_ENDPOINT) { principal ->
            val tagId = getAndCheckIntParameter(TAG_ARG) ?: run {
                respondText("$MISSING_PARAMETER $TAG_ARG", status = HttpStatusCode.BadRequest)
                return@getExt
            }
            respond(useCaseFactory.getTagSelectionUseCase(tagId, principal.user).run())
        }
    }
}
