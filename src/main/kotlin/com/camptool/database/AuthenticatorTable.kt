package com.camptool.database

import com.camptool.Globals.Companion.mainClient
import com.camptool.Globals.Companion.vertx
import io.vertx.ext.auth.webauthn.Authenticator
import io.vertx.kotlin.coroutines.awaitEvent
import io.vertx.kotlin.coroutines.dispatcher
import io.vertx.mutiny.sqlclient.Tuple
import kotlinx.coroutines.*

@OptIn(DelicateCoroutinesApi::class)
object AuthenticatorTable {

    suspend fun getByUUID(uuid: String) = withContext(Dispatchers.IO) {
        mainClient.query("SELECT * FROM authenticator WHERE uuid = '$uuid';").executeAndAwait().firstOrNull()?.let {
            Authenticator().apply {
                setUserName(it.getString("username"))
                credID = it.getString("uuid")
                publicKey = it.getString("publickey")
                counter = it.getLong("counter")
            }
        }
    }

    suspend fun getByUsername(username: String) = withContext(Dispatchers.IO) {
        mainClient
            .query("SELECT * FROM authenticator WHERE username = '$username';")
            .executeAndAwait()
            .map {
                Authenticator().apply {
                    setUserName(username)
                    credID = it.getString("uuid")
                    publicKey = it.getString("publickey")
                    counter = it.getLong("counter")
                }
            }
    }

    suspend fun upsert(authenticator: Authenticator) = withContext(Dispatchers.IO) {
        val params = Tuple.tuple()
            .addString(authenticator.credID)
            .addString(authenticator.userName)
            .addString(authenticator.publicKey)
            .addLong(authenticator.counter)
            .addLong(authenticator.counter)

        val upsertQuery = """
                INSERT INTO authenticator (uuid, username, publickey, counter) 
                VALUES (?, ?, ?, ?) 
                ON DUPLICATE KEY UPDATE 
                counter = ?;
            """.trimIndent()

        return@withContext try {
            mainClient.preparedQuery(upsertQuery).executeAndAwait(params)
            true
        } catch (e: Exception) {
            false
        }
    }
}