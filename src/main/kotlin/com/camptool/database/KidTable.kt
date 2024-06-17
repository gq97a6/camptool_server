package com.camptool.database

import com.camptool.Globals.Companion.mainClient
import com.camptool.Globals.Companion.mapper
import com.camptool.classes.Kid
import io.vertx.mutiny.sqlclient.Tuple
import jakarta.enterprise.context.ApplicationScoped
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@ApplicationScoped
object KidTable {

    suspend fun get(uuid: String) = withContext(Dispatchers.IO) {
        mainClient.query("SELECT * FROM kid WHERE uuid = '$uuid'").executeAndAwait().firstOrNull()?.let {
            mapper.readValue(it.getString("data"), Kid::class.java)
        }
    }

    suspend fun getAll() = withContext(Dispatchers.IO) {
        mainClient.query("SELECT * FROM kid").executeAndAwait().map {
            mapper.readValue(it.getString("data"), Kid::class.java)
        }
    }

    suspend fun upsert(kid: Kid) = withContext(Dispatchers.IO) {
        val params = Tuple.tuple()
            .addString(kid.uuid)
            .addString(mapper.writeValueAsString(kid))
            .addString(mapper.writeValueAsString(kid))

        val upsertQuery = """
                INSERT INTO kid (uuid, data) 
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