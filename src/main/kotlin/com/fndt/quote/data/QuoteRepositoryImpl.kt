package com.fndt.quote.data

import com.fndt.quote.domain.dto.ID
import com.fndt.quote.domain.dto.Quote
import com.fndt.quote.domain.dto.User
import com.fndt.quote.domain.repository.QuoteRepository
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update

class QuoteRepositoryImpl(private val dbProvider: DatabaseProvider) : QuoteRepository {
    private val quotesTable: DatabaseProvider.Quotes by dbProvider
    private val filterBuilder get() = QuoteFilterImpl.factory(dbProvider).create()

    override fun get(): List<Quote> {
        return filterBuilder.build().getQuotes()
    }

    override fun add(quote: Quote): ID = transaction {
        val quoteExists = findById(quote.id) != null
        return@transaction if (quoteExists) update(quote) else insert(quote)
    }

    override fun remove(quoteId: Int) {
        quotesTable.deleteWhere { quotesTable.id eq quoteId }
    }

    override fun findById(id: Int): Quote? {
        return filterBuilder.setQuoteId(id).build().findQuote()
    }

    override fun findByUser(user: User): List<Quote> {
        return filterBuilder.setUser(user).build()
            .getQuotes()
    }

    private fun insert(quote: Quote): ID {
        return quotesTable.insert { insert ->
            insert[body] = quote.body
            insert[createdAt] = quote.createdAt
            insert[user] = EntityID(quote.user.id, DatabaseProvider.Users)
            insert[isPublic] = quote.isPublic
        }[quotesTable.id].value
    }

    private fun update(quote: Quote): ID {
        quotesTable.update({ quotesTable.id eq quote.id }) { update ->
            update[body] = quote.body
            update[isPublic] = quote.isPublic
        }
        return quote.id
    }
}
