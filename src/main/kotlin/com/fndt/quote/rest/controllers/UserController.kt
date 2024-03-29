package com.fndt.quote.rest.controllers

import com.fndt.quote.rest.UrlSchemeProvider
import com.fndt.quote.rest.dto.UpdateRole
import com.fndt.quote.rest.dto.UserCredentials
import com.fndt.quote.rest.dto.out.toOutUser
import com.fndt.quote.rest.factory.UsersUseCaseFactory
import com.fndt.quote.rest.util.*
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.yield
import java.io.File
import java.io.InputStream
import java.io.OutputStream

class UserController(
    private val useCaseManager: UsersUseCaseFactory,
    private val uploadDir: String
) : RoutingController {
    override fun route(routing: Routing) = routing {
        register()
        getUserInfo()
        updateAvatar()
        banUser()
        permanentBan()
        changeRole()
        getUser()
    }

    private fun Route.register() = post(REGISTRATION_ENDPOINT) {
        val credentials = call.receive<UserCredentials>()
        useCaseManager.registerUseCase(credentials.login, credentials.password).run()
        call.respond(SUCCESS)
    }

    private fun Route.getUserInfo() = routePathWithAuth(ROLE_ENDPOINT) {
        getExt { respond(it.user.toOutUser(UrlSchemeProvider)) }
    }

    private fun Route.updateAvatar() = routePathWithAuth(AVATAR_ENDPOINT) {
        postExt { principal ->
            val file = downloadImage(uploadDir)
            useCaseManager.changeProfilePictureUseCase(file, principal.user).run()
            respond(SUCCESS)
        }
    }

    private fun Route.banUser() = routePathWithAuth(BAN_ENDPOINT) {
        postExt { principal ->
            val userId = parameters[ID]!!.toInt()
            useCaseManager.getBanUseCase(userId, principal.user).run()
            respond(SUCCESS)
        }
    }

    private fun Route.permanentBan() = routePathWithAuth(PERMANENT_BAN_ENDPOINT) {
        postExt { principal ->
            val userId = parameters[ID]!!.toInt()
            useCaseManager.getPermanentBanUseCase(userId, principal.user).run()
            respond(SUCCESS)
        }
    }

    private fun Route.changeRole() = routePathWithAuth(ROLE_ENDPOINT) {
        postExt { principal ->
            val (role, id) = receive<UpdateRole>()
            useCaseManager.getChangeRoleUseCase(id, role, principal.user).run()
            respond(SUCCESS)
        }
    }

    private fun Route.getUser() = routePathWithAuth(USER_ENDPOINT) {
        getExt { principal ->
            val userId = parameters[ID]!!.toInt()
            useCaseManager.getUserUseCase(userId, principal.user).run().also {
                respond(it.toOutUser(UrlSchemeProvider))
            }
        }
    }
}

private val File.nameAndExtension: Pair<String, String> get() = nameWithoutExtension to extension

private suspend fun ApplicationCall.downloadImage(uploadDir: String): File {
    val multipart = receiveMultipart()
    var result: File? = null
    multipart.forEachPart { part ->
        when (part) {
            is PartData.FileItem -> {
                if (part.contentType != ContentType.Image.PNG) throw IllegalArgumentException("bad image")
                part.contentDisposition
                val (title, ext) = File(part.originalFileName).nameAndExtension
                val file = File(
                    uploadDir, "upload-${System.currentTimeMillis()}-${title.hashCode()}.$ext"
                )
                part.streamProvider().use { input ->
                    file.outputStream().buffered().use { output -> input.copyToSuspend(output) }
                }
                result = file
            }
            else -> Unit
        }
        part.dispose()
    }
    return result ?: throw IllegalStateException()
}

private suspend fun InputStream.copyToSuspend(
    out: OutputStream,
    bufferSize: Int = DEFAULT_BUFFER_SIZE,
    yieldSize: Int = 4 * 1024 * 1024,
    dispatcher: CoroutineDispatcher = Dispatchers.IO
): Long {
    return withContext(dispatcher) {
        val buffer = ByteArray(bufferSize)
        var bytesCopied = 0L
        var bytesAfterYield = 0L
        while (true) {
            val bytes = read(buffer).takeIf { it >= 0 } ?: break
            out.write(buffer, 0, bytes)
            if (bytesAfterYield >= yieldSize) {
                yield()
                bytesAfterYield %= yieldSize
            }
            bytesCopied += bytes
            bytesAfterYield += bytes
        }
        return@withContext bytesCopied
    }
}
