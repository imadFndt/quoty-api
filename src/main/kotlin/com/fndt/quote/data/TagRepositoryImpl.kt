package com.fndt.quote.data

import com.fndt.quote.data.util.toTag
import com.fndt.quote.domain.dto.ID
import com.fndt.quote.domain.dto.Tag
import com.fndt.quote.domain.filter.Access
import com.fndt.quote.domain.repository.TagRepository
import org.jetbrains.exposed.sql.*

class TagRepositoryImpl(dbProvider: DatabaseProvider) : TagRepository {
    private val tagsTable: DatabaseProvider.Tags by dbProvider

    override fun get(): List<Tag> {
        return tagsTable.selectAll().map { it.toTag() }
    }

    override fun add(item: Tag): ID {
        val tagExists = findById(item.id) != null
        return if (tagExists) update(item) else insert(item)
    }

    override fun remove(item: Tag) {
        tagsTable.deleteWhere { tagsTable.id eq item.id }
    }

    private fun insert(tag: Tag): ID {
        return tagsTable.insert { insert ->
            insert[name] = tag.name
            insert[isPublic] = tag.isPublic
        }[tagsTable.id].value
    }

    private fun update(tag: Tag): ID {
        tagsTable.update({ tagsTable.id eq tag.id }) { update ->
            update[name] = tag.name
            update[isPublic] = tag.isPublic
        }
        return tag.id
    }

    override fun findById(itemId: Int): Tag? {
        return tagsTable.select { tagsTable.id eq itemId }.firstOrNull()?.toTag()
    }

    override fun findByAccess(access: Access): List<Tag> {
        return tagsTable.selectAll()
            .apply { applyAccess(access, tagsTable) }
            .map { it.toTag() }
    }

    override fun findByName(name: String): Tag? {
        return tagsTable.select { tagsTable.name eq name }.firstOrNull()?.toTag()
    }
}
