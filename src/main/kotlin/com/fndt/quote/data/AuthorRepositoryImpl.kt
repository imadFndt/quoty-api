package com.fndt.quote.data

import com.fndt.quote.data.util.toAuthor
import com.fndt.quote.domain.dto.Author
import com.fndt.quote.domain.dto.ID
import com.fndt.quote.domain.repository.AuthorRepository
import org.jetbrains.exposed.sql.*

class AuthorRepositoryImpl(databaseProvider: DatabaseProvider) : AuthorRepository {
    private val authorTable: DatabaseProvider.Authors by databaseProvider

    override fun get(): List<Author> {
        return authorTable.selectAll().map { it.toAuthor() }
    }

    override fun findByName(name: String): Author? {
        return find(name = name)
    }

    override fun add(item: Author): ID {
        val authorExists = findById(item.id) != null
        return if (authorExists) update(item) else insert(item)
    }

    override fun remove(item: Author) {
        authorTable.deleteWhere { authorTable.id eq item.id }
    }

    override fun findById(itemId: ID): Author? {
        return find(authorId = itemId)
    }

    private fun find(authorId: ID? = null, name: String? = null): Author? {
        return authorTable.selectAll().apply {
            authorId?.let { andWhere { authorTable.id eq authorId } }
            name?.let { andWhere { authorTable.name eq name } }
        }.firstOrNull()?.toAuthor()
    }

    private fun insert(author: Author): ID {
        return authorTable.insert { insert ->
            insert[name] = author.name
        }[authorTable.id].value
    }

    private fun update(author: Author): ID {
        authorTable.update { update ->
            update[name] = author.name
        }
        return author.id
    }
}
