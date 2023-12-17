package com.campbuddy

import io.quarkus.reactive.datasource.ReactiveDataSource
import io.quarkus.runtime.StartupEvent
import io.vertx.core.Vertx
import io.vertx.mutiny.mysqlclient.MySQLPool
import jakarta.enterprise.context.ApplicationScoped
import jakarta.enterprise.event.Observes
import jakarta.inject.Inject

@ApplicationScoped
class Globals {
    @Inject
    var vertx: Vertx? = null

    @Inject
    @ReactiveDataSource("main")
    private lateinit var mainClient: MySQLPool

    fun onStart(@Observes event: StartupEvent) {
        Companion.mainClient = mainClient
        Companion.vertx = vertx!!
    }

    companion object {
        lateinit var mainClient: MySQLPool
        lateinit var vertx: Vertx
    }
}