package com.campbuddy.service

import io.quarkus.reactive.datasource.ReactiveDataSource
import io.quarkus.runtime.StartupEvent
import io.vertx.mutiny.mysqlclient.MySQLPool
import jakarta.enterprise.context.ApplicationScoped
import jakarta.enterprise.event.Observes
import jakarta.inject.Inject

@ApplicationScoped
class PoolService {
    @Inject
    @ReactiveDataSource("private")
    private lateinit var privateClient: MySQLPool

    @Inject
    @ReactiveDataSource("public")
    private lateinit var publicClient: MySQLPool

    fun onStart(@Observes event: StartupEvent) {
        Companion.privateClient = privateClient
        Companion.publicClient = publicClient
    }

    companion object {
        lateinit var privateClient: MySQLPool
        lateinit var publicClient: MySQLPool
    }
}