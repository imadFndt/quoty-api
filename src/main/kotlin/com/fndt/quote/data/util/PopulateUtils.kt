package com.fndt.quote.data.util

import com.fndt.quote.data.DatabaseProvider
import com.fndt.quote.data.DatabaseProvider.Quotes.body
import com.fndt.quote.data.DatabaseProvider.Quotes.createdAt
import com.fndt.quote.data.DatabaseProvider.TagsOnQuotes.quote
import com.fndt.quote.data.DatabaseProvider.TagsOnQuotes.tag
import com.fndt.quote.domain.dto.Quote
import com.fndt.quote.domain.dto.Tag
import com.fndt.quote.domain.dto.User
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.batchInsert
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction

fun populateDb() = transaction {
    val tables = arrayOf(
        DatabaseProvider.Quotes,
        DatabaseProvider.Users,
        DatabaseProvider.Tags,
        DatabaseProvider.TagsOnQuotes,
        DatabaseProvider.Comments,
        DatabaseProvider.LikesOnQuotes,
    )
    SchemaUtils.drop(*tables)
    SchemaUtils.create(*tables)
    DatabaseProvider.Users.batchInsert(usersList) { user ->
        this[DatabaseProvider.Users.id] = user.id
        this[DatabaseProvider.Users.blockedUntil] = user.blockedUntil
        this[DatabaseProvider.Users.hashedPassword] = user.hashedPassword
        this[DatabaseProvider.Users.role] = user.role
        this[DatabaseProvider.Users.name] = user.name
    }
    DatabaseProvider.Quotes.batchInsert(quotesList) { quote ->
        this[body] = quote.body
        this[createdAt] = System.currentTimeMillis()
        this[DatabaseProvider.Quotes.isPublic] = true

        val userId = DatabaseProvider.Users
            .slice(DatabaseProvider.Users.id)
            .select { DatabaseProvider.Users.name eq quote.user.name }
            .firstOrNull()
            ?.let { it[DatabaseProvider.Users.id] } ?: run { throw IllegalArgumentException() }
        this[DatabaseProvider.Quotes.user] = userId
    }
    DatabaseProvider.Comments.insert { insert ->
        insert[body] = "Хуйня"
        insert[quoteId] = 1
        insert[createdAt] = System.currentTimeMillis()
        insert[user] = 1
    }
    val tags = listOf("Смешарики", "За жизнь")
    DatabaseProvider.Tags.batchInsert(tagsList) { tag ->
        this[DatabaseProvider.Tags.id] = tag.id
        this[DatabaseProvider.Tags.name] = tag.name
        this[DatabaseProvider.Tags.isPublic] = tag.isPublic
    }
    DatabaseProvider.TagsOnQuotes.batchInsert(tags) { current ->
        this[quote] = 1
        this[tag] = DatabaseProvider.Tags
            .slice(DatabaseProvider.Tags.id)
            .select { DatabaseProvider.Tags.name eq current }
            .firstOrNull()
            ?.let { it[DatabaseProvider.Tags.id] } ?: run { throw IllegalArgumentException() }
    }
    DatabaseProvider.TagsOnQuotes.insert { insertStatement ->
        insertStatement[quote] = 1
        insertStatement[tag] = DatabaseProvider.Tags
            .slice(DatabaseProvider.Tags.id)
            .select { DatabaseProvider.Tags.name eq "Смешарики" }
            .firstOrNull()
            ?.let { it[DatabaseProvider.Tags.id] } ?: run { throw IllegalArgumentException() }
    }
    DatabaseProvider.LikesOnQuotes.insert { insertStatement ->
        insertStatement[quote] = 2
        insertStatement[user] = 1
    }
    DatabaseProvider.LikesOnQuotes.insert { insertStatement ->
        insertStatement[quote] = 1
        insertStatement[user] = 1
    }
    DatabaseProvider.Users.batchInsert(usersList) { item ->
        this[DatabaseProvider.Users.name] = item.name
        this[DatabaseProvider.Users.hashedPassword] = item.hashedPassword
        this[DatabaseProvider.Users.role] = item.role
    }
    commit()
}

internal val usersList = listOf(
    User(id = 1, name = "a", password = "a".toHashed()),
    User(id = 2, name = "b", password = "b".toHashed()),
    User(id = 3, name = "c", password = "c".toHashed()),
)
internal val quotesList = listOf(
    Quote(id = 1, body = "Я просто выгляжу как лось, а в душе я бабочка.", createdAt = 0, user = usersList[0]),
    Quote(id = 2, body = "В вопросах дружбы размер не имеет значения.", createdAt = 0, user = usersList[1]),
    Quote(
        id = 3,
        body = "Почему я должен делать вид, что мне хорошо, когда мне в действительности плохо?",
        createdAt = 0,
        user = usersList[0],
    ),
    Quote(
        id = 4,
        body = "Нет, со мной не всё в порядке. Не бывает, чтобы у кого-то всё было в порядке.",
        createdAt = 0,
        user = usersList[1],
    ),
    Quote(
        id = 5,
        body = "Не надо строить иллюзий, которые могут закончиться травмпунктом.",
        createdAt = 0,
        user = usersList[0],
    ),
)
val tagsList = listOf(Tag(1, "Смешарики", true), Tag(2, "За жизнь", true))
