package com.campbuddy.resource

import jakarta.annotation.security.PermitAll
import jakarta.annotation.security.RolesAllowed
import jakarta.enterprise.context.RequestScoped
import jakarta.inject.Inject
import jakarta.ws.rs.GET
import jakarta.ws.rs.InternalServerErrorException
import jakarta.ws.rs.Path
import jakarta.ws.rs.Produces
import jakarta.ws.rs.core.Context
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.SecurityContext
import org.eclipse.microprofile.jwt.JsonWebToken


@Path("/secured2")
@RequestScoped
class TokenSecuredResource {
    @Inject
    lateinit var jwt: JsonWebToken

    @GET
    @Path("permit-all")
    @PermitAll
    @Produces(MediaType.TEXT_PLAIN)
    fun hello(@Context ctx: SecurityContext): String {
        return getResponseString(ctx)
    }

    @GET
    @Path("roles-allowed")
    @RolesAllowed("User", "Admin")
    @Produces(MediaType.TEXT_PLAIN)
    fun helloRolesAllowed(@Context ctx: SecurityContext): String {
        return getResponseString(ctx) + ", birthdate: " + jwt.getClaim<Any>("birthdate").toString()
    }

    private fun getResponseString(ctx: SecurityContext): String {
        val name = when {
            ctx.userPrincipal == null -> "anonymous"
            ctx.userPrincipal.name != jwt.name -> {
                throw InternalServerErrorException("Principal and JsonWebToken names do not match")
            }
            else -> ctx.userPrincipal.name
        }

        return "hello + $name, isHttps: ${ctx.isSecure}, authScheme: ${ctx.authenticationScheme}, hasJWT: ${hasJwt()}"
    }

    private fun hasJwt(): Boolean {
        return jwt.claimNames != null
    }
}