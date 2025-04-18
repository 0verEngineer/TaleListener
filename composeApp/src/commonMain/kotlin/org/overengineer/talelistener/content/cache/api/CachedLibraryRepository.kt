package org.overengineer.talelistener.content.cache.api

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext
import org.overengineer.talelistener.content.cache.converter.libraryEntityToLibrary
import org.overengineer.talelistener.db.DBHolder
import org.overengineer.talelistener.domain.Library
import org.overengineer.talelistener.sqldelight.LibraryEntity


class CachedLibraryRepository (
    private val dbHolder: DBHolder
) {
    suspend fun cacheLibraries(libraries: List<Library>) {
        withContext(Dispatchers.IO) {
            val entities = libraries.map {
                LibraryEntity(
                    id = it.id,
                    title = it.title,
                    type = it.type,
                )
            }

            val q = dbHolder.libraryQueries

            q.transaction {
                entities.forEach {
                    q.upsertLibraries(it)
                }
            }

            val placeholders = libraries.map { it.id }.joinToString(",") { "?" }
            val sql = "DELETE FROM libraryEntity WHERE id NOT IN ($placeholders)"

            dbHolder.getDriver().execute(null, sql, libraries.size) {
                libraries.map { it.id }.forEachIndexed { index, id ->
                    bindString(index, id)
                }
            }
        }
    }

    suspend fun fetchLibraries(): List<Library> = withContext(Dispatchers.IO) {
            dbHolder.libraryQueries.fetchLibraries().executeAsList().map { libraryEntityToLibrary(it) }
    }
}
