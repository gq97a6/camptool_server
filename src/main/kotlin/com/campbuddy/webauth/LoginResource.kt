package com.campbuddy.webauth

import com.campbuddy.Globals.Companion.domain
import com.campbuddy.Globals.Companion.origin
import io.vertx.core.json.JsonObject
import io.vertx.kotlin.coroutines.awaitEvent
import jakarta.ws.rs.Consumes
import jakarta.ws.rs.POST
import jakarta.ws.rs.Path
import jakarta.ws.rs.Produces
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.NewCookie
import jakarta.ws.rs.core.Response

@Path("/login/webauthn")
class LoginResource {

    data class LoginBeginData(val username: String? = null)

    @Path("/begin")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    suspend fun begin(data: LoginBeginData): Response {
        if (data.username.isNullOrBlank()) return Response.status(Response.Status.BAD_REQUEST).build()

        return awaitEvent { h ->
            webAuthn.getCredentialsOptions(data.username)
                .onSuccess {
                    h.handle(Response.status(Response.Status.OK).entity(it.toString()).build())
                }.onFailure {
                    h.handle(Response.status(Response.Status.BAD_REQUEST).entity(it.localizedMessage).build())
                }
        }
    }

    data class LoginFinishData(
        val username: String?,
        val challenge: String?,
        val id: String?,
        val rawId: String?,
        val type: String?,
        val response: Response?,
    ) {
        data class Response(
            val authenticatorData: String?,
            val clientDataJSON: String?,
            val signature: String?,
            val userHandle: String?
        )
    }

    @Path("/finish")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    suspend fun finish(data: LoginFinishData): Response {
        data.apply {
            if (username.isNullOrBlank() ||
                challenge.isNullOrBlank() ||
                id.isNullOrBlank() ||
                rawId.isNullOrBlank() ||
                type.isNullOrBlank() ||
                response == null ||
                response.authenticatorData.isNullOrBlank() ||
                response.clientDataJSON.isNullOrBlank() ||
                response.signature.isNullOrBlank()
            ) return Response.status(Response.Status.BAD_REQUEST).entity("Missing data.").build()
        }

        val body = JsonObject()
            .put("id", data.id)
            .put("rawId", data.rawId)
            .put("type", data.type)
            .put(
                "response", JsonObject()
                    .put("authenticatorData", data.response?.authenticatorData)
                    .put("clientDataJSON", data.response?.clientDataJSON)
                    .put("signature", data.response?.signature)
                    .put("userHandle", data.response?.userHandle)
            )

        return awaitEvent { h ->
            webAuthn
                .authenticate(
                    JsonObject()
                        .put("username", data.username)
                        .put("origin", origin)
                        .put("domain", domain)
                        .put("challenge", data.challenge)
                        .put("webauthn", body)
                )
                .onSuccess {
                    val loginCookie = NewCookie.Builder("username")
                        .value(data.username)
                        .domain(".$domain")
                        .path("/")
                        .maxAge(3600)
                        .secure(false)
                        .httpOnly(false)
                        .build()

                    h.handle(Response.ok().cookie(loginCookie).build())
                }.onFailure {
                    h.handle(Response.status(Response.Status.BAD_REQUEST).entity(it.toString()).build())
                }
        }
    }
}