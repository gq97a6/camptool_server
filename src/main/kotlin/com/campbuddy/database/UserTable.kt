package com.campbuddy.database

import com.campbuddy.Globals.Companion.mainClient
import com.campbuddy.`object`.User
import io.vertx.mutiny.sqlclient.Tuple
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.mindrot.jbcrypt.BCrypt
import java.util.*

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

    suspend fun register(user: User): Pair<RegisterResponse, User?> = withContext(Dispatchers.IO) {
        // Check if username is taken
        if (getByUsername(user.username) != null) return@withContext Pair(RegisterResponse.USERNAME_TAKEN, null)

        // Generate UUID
        for (i in 0..5) {
            user.uuid = UUID.randomUUID().toString().replace("-", "").uppercase(Locale.getDefault())
            if (getByUUID(user.uuid) == null) break
            else user.uuid = ""
        }

        // Check if UUID was generated
        if (user.uuid.isBlank()) return@withContext Pair(RegisterResponse.UUID_GENERATION_ERROR, null)

        // Hash password
        user.password = BCrypt.hashpw(user.password, BCrypt.gensalt())

        // Upsert user
        if (!upsert(user)) return@withContext Pair(RegisterResponse.UPSERT_ERROR, null)
        else return@withContext Pair(RegisterResponse.SUCCESS, user)
    }

    enum class RegisterResponse {
        SUCCESS,
        USERNAME_TAKEN,
        UUID_GENERATION_ERROR,
        UPSERT_ERROR
    }
}