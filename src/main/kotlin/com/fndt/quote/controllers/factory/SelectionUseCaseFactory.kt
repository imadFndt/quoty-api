package com.fndt.quote.controllers.factory

import com.fndt.quote.domain.QuoteFilter
import com.fndt.quote.domain.RequestManager
import com.fndt.quote.domain.dto.Quote
import com.fndt.quote.domain.dto.User
import com.fndt.quote.domain.manager.PermissionManager
import com.fndt.quote.domain.repository.TagRepository
import com.fndt.quote.domain.usecases.PopularsUseCase
import com.fndt.quote.domain.usecases.SearchUseCase
import com.fndt.quote.domain.usecases.TagSelectionUseCase
import com.fndt.quote.domain.usecases.UseCase

class SelectionUseCaseFactory(
    private val filterBuilderFactory: QuoteFilter.Builder.Factory,
    private val tagRepository: TagRepository,
    private val permissionManager: PermissionManager,
    private val requestManager: RequestManager,
) {
    fun getSearchUseCase(query: String, user: User): UseCase<List<Quote>> {
        return SearchUseCase(query, filterBuilderFactory.create(), user, permissionManager, requestManager)
    }

    fun getPopularsUseCase(user: User): UseCase<List<Quote>> {
        return PopularsUseCase(filterBuilderFactory.create(), user, permissionManager, requestManager)
    }

    fun getTagSelectionUseCase(tagId: Int, user: User): TagSelectionUseCase {
        return TagSelectionUseCase(
            tagId, filterBuilderFactory.create(), tagRepository, user, permissionManager, requestManager
        )
    }
}
