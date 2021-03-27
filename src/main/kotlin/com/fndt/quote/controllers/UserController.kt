package com.fndt.quote.controllers

import com.fndt.quote.controllers.dto.UserCredentials
import com.fndt.quote.controllers.factory.UsersUseCaseFactory
import com.fndt.quote.controllers.util.*
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.response.*
import io.ktor.routing.*

class UserController(private val useCaseManager: UsersUseCaseFactory) : RoutingController {
    override fun route(routing: Routing) = routing {
        authenticate()
        getUserInfo()
    }

    private fun Route.authenticate() {
        route(REGISTRATION_ENDPOINT) { get { call.registerAndRespond() } }
    }

    private fun Route.getUserInfo() {
        getExt(ROLE_ENDPOINT) { respond(it.user) }
    }

    private suspend fun ApplicationCall.registerAndRespond() {
        val credentials = receiveCatching<UserCredentials>() ?: return
        val result = try {
            useCaseManager.registerUseCase(credentials.login, credentials.password).run()
            SUCCESS to HttpStatusCode.OK
        } catch (e: Exception) {
            FAILURE to HttpStatusCode.NotAcceptable
        }
        respondText(text = result.first, status = result.second)
    }
}
