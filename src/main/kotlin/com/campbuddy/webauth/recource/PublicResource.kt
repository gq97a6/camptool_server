package com.campbuddy.webauth.recource

import jakarta.ws.rs.GET
import jakarta.ws.rs.Path
import jakarta.ws.rs.Produces
import jakarta.ws.rs.core.Context
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.SecurityContext

@Path("/api/public")
class PublicResource {
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    fun publicResource(): String {
        return "public"
    }

    @GET
    @Path("/me")
    @Produces(MediaType.TEXT_PLAIN)
    fun me(@Context securityContext: SecurityContext): String {
        val user = securityContext.userPrincipal
        return if (user != null) user.name else "<not logged in>"
    }
}