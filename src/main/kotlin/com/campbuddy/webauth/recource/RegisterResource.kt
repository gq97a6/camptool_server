package com.campbuddy.webauth.recource

import com.campbuddy.webauth.authDb
import com.campbuddy.webauth.webAuthn
import io.vertx.core.json.JsonObject
import io.vertx.ext.auth.authentication.Credentials
import io.vertx.kotlin.coroutines.awaitEvent
import jakarta.ws.rs.Consumes
import jakarta.ws.rs.POST
import jakarta.ws.rs.Path
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.Response
import java.util.*


@Path("/register/webauthn")
class RegisterResource {

    data class RegisterBeginData(
        val username: String?,
        val displayName: String?
    )

    @Path("/begin")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    suspend fun begin(data: RegisterBeginData): Response {
        if (data.username.isNullOrBlank() || data.displayName.isNullOrBlank())
            return Response.status(Response.Status.BAD_REQUEST).build()

        val userId = UUID.randomUUID().toString() //TODO: ENSURE UNIQUE

        val user = JsonObject()
            .put("id", userId)
            .put("rawId", userId)
            .put("name", data.username)
            .put("displayName", data.displayName)

        return awaitEvent { h ->
            webAuthn.createCredentialsOptions(user).onSuccess {
                h.handle(Response.status(Response.Status.OK).entity(it.toString()).build())
            }.onFailure {
                h.handle(Response.status(Response.Status.BAD_REQUEST).entity(it.localizedMessage).build())
            }
        }
    }

    data class RegisterFinishData(
        val username: String?,
        val challenge: String?,
        val id: String?,
        val rawId: String?,
        val type: String?,
        val response: Response?,
    ) {
        data class Response(val attestationObject: String?, val clientDataJSON: String?)
    }

    @Path("/finish")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    suspend fun finish(data: RegisterFinishData): Response {
        data.apply {
            if (username.isNullOrBlank() ||
                challenge.isNullOrBlank() ||
                id.isNullOrBlank() ||
                rawId.isNullOrBlank() ||
                type.isNullOrBlank() ||
                response == null ||
                response.attestationObject.isNullOrBlank() ||
                response.clientDataJSON.isNullOrBlank()
            ) return Response.status(Response.Status.BAD_REQUEST).build()
        }

        val request = JsonObject()
            .put("id", data.id)
            .put("rawId", data.rawId)
            .put("type", data.type)
            .put(
                "response", JsonObject()
                    .put("attestationObject", data.response?.attestationObject)
                    .put("clientDataJSON", data.response?.clientDataJSON)
            )

        return awaitEvent { h ->
            webAuthn
                .authenticate(
                    JsonObject()
                        .put("username", data.username)
                        .put("origin", "http://localhost")
                        .put("domain", "localhost")
                        .put("challenge", data.challenge)
                        .put("webauthn", request)
                )
                .onSuccess {
                    h.handle(Response.status(Response.Status.OK).entity(it.toString()).build())
                }.onFailure {
                    h.handle(Response.status(Response.Status.BAD_REQUEST).entity(it.toString()).build())
                }
        }
    }
}