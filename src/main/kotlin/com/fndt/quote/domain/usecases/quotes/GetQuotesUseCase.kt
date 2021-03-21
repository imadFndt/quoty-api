package com.fndt.quote.domain.usecases.quotes

import com.fndt.quote.domain.QuoteFilter
import com.fndt.quote.domain.RequestManager
import com.fndt.quote.domain.dto.AuthRole
import com.fndt.quote.domain.dto.Quote
import com.fndt.quote.domain.dto.User
import com.fndt.quote.domain.manager.PermissionManager
import com.fndt.quote.domain.usecases.RequestUseCase

class GetQuotesUseCase(
    private val searchUser: User? = null,
    private val filterBuilder: QuoteFilter.Builder,
    override val requestingUser: User,
    private val permissionManager: PermissionManager,
    requestManager: RequestManager
) : RequestUseCase<List<Quote>>(requestManager) {
    override suspend fun makeRequest(): List<Quote> {
        val access =
            if (requestingUser.role == AuthRole.MODERATOR || requestingUser.role == AuthRole.ADMIN) null else true
        return filterBuilder.setAccess(access).setUser(searchUser).build()
            .getQuotes()
    }

    override fun validate(user: User?): Boolean {
        return permissionManager.hasGetQuotesPermission(requestingUser)
    }
}