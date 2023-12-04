package com.campbuddy.service

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.campbuddy.`object`.UserDB
import com.campbuddy.service.PoolService.Companion.privateClient

object PrivateService {
    suspend fun getAllData() = withContext(Dispatchers.IO) {
        privateClient.query("SELECT * FROM users").executeAndAwait().map {
            UserDB(
                it.getLong("id"),
                it.getString("username"),
                it.getString("password")
            )
        }
    }
}