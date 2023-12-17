package com.campbuddy.database

import com.campbuddy.Globals.Companion.mainClient
import com.campbuddy.`object`.User
import io.vertx.mutiny.sqlclient.Tuple
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object UserTable {
    suspend fun getByUUID(uuid: String) = withContext(Dispatchers.IO) {
        mainClient.query("SELECT * FROM user WHERE uuid = '$uuid'").executeAndAwait().firstOrNull()?.let {
            User(
                it.getString("uuid"),
                it.getString("username"),
                it.getString("password")
            )
        }
    }

    suspend fun getByUsername(username: String) = withContext(Dispatchers.IO) {
        mainClient.query("SELECT * FROM user WHERE username = '$username'").executeAndAwait().firstOrNull()?.let {
            User(
                it.getString("uuid"),
                it.getString("username"),
                it.getString("password")
            )
        }
    }

    suspend fun upsert(user: User) = withContext(Dispatchers.IO) {
        val params = Tuple.tuple()
            .addString(user.uuid)
            .addString(user.username)
            .addString(user.password)
            .addString(user.username)
            .addString(user.password)

        val upsertQuery = """
                INSERT INTO user (uuid, username, password) 
                VALUES (?, ?, ?) 
                ON DUPLICATE KEY UPDATE 
                username = ?, 
                password = ?;
            """.trimIndent()

        return@withContext try {
            mainClient.preparedQuery(upsertQuery).executeAndAwait(params)
            true
        } catch (e: Exception) {
            false
        }
    }
}