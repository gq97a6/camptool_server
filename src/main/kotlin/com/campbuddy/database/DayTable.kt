package com.campbuddy.database

import com.campbuddy.Globals.Companion.mainClient
import com.campbuddy.Globals.Companion.mapper
import com.campbuddy.classes.Day
import io.vertx.mutiny.sqlclient.Tuple
import jakarta.enterprise.context.ApplicationScoped
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@ApplicationScoped
object DayTable {

    suspend fun get(uuid: String) = withContext(Dispatchers.IO) {
        mainClient.query("SELECT * FROM day WHERE uuid = '$uuid'").executeAndAwait().firstOrNull()?.let {
            mapper.readValue(it.getString("data"), Day::class.java)
        }
    }

    suspend fun getAll() = withContext(Dispatchers.IO) {
        mainClient.query("SELECT * FROM day").executeAndAwait().map {
            mapper.readValue(it.getString("data"), Day::class.java)
        }
    }

    suspend fun upsert(day: Day) = withContext(Dispatchers.IO) {
        val params = Tuple.tuple()
            .addString(day.uuid)
            .addString(mapper.writeValueAsString(day))
            .addString(mapper.writeValueAsString(day))

        val upsertQuery = """
                INSERT INTO day (uuid, data) 
                VALUES (?, ?) 
                ON DUPLICATE KEY UPDATE 
                data = ?
            """.trimIndent()

        return@withContext try {
            mainClient.preparedQuery(upsertQuery).executeAndAwait(params)
            true
        } catch (e: Exception) {
            println(e.message)
        }
    }
}