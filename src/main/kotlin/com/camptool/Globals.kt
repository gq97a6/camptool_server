package com.camptool

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JSR310Module
import io.quarkus.reactive.datasource.ReactiveDataSource
import io.quarkus.runtime.StartupEvent
import io.vertx.core.Vertx
import io.vertx.mutiny.mysqlclient.MySQLPool
import jakarta.enterprise.context.ApplicationScoped
import jakarta.enterprise.event.Observes
import jakarta.inject.Inject
import org.eclipse.microprofile.config.inject.ConfigProperty


@ApplicationScoped
class Globals {
    @ConfigProperty(name = "server.origin")
    var origin: String? = null

    @ConfigProperty(name = "server.domain")
    var domain: String? = null

    @Inject
    var vertx: Vertx? = null

    @Inject
    @ReactiveDataSource("main")
    private lateinit var mainClient: MySQLPool

    fun onStart(@Observes event: StartupEvent) {
        Companion.mainClient = mainClient
        Companion.vertx = vertx!!
        Companion.origin = origin ?: "http://localhost"
        Companion.domain = domain ?: "localhost"
        mapper.findAndRegisterModules()
    }

    companion object {
        lateinit var mainClient: MySQLPool
        lateinit var vertx: Vertx
        lateinit var origin: String
        lateinit var domain: String
        var mapper: ObjectMapper = ObjectMapper()
    }
}