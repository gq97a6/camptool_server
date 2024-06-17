package com.camptool.resource

import com.camptool.Globals.Companion.domain
import com.camptool.database.UserTable
import jakarta.ws.rs.POST
import jakarta.ws.rs.Path
import jakarta.ws.rs.core.NewCookie
import jakarta.ws.rs.core.Response
import org.mindrot.jbcrypt.BCrypt


@Path("/login")
class LoginResource {

    data class LoginData(
        val username: String?,
        val password: String?,
    )

    @POST
    suspend fun login(data: LoginData): Response {
        // Validate data
        if (data.username.isNullOrBlank() || data.password.isNullOrBlank())
            return Response.status(Response.Status.BAD_REQUEST).build()

        // Get user from database
        val user = UserTable.getByUsername(data.username) ?: return Response.status(Response.Status.NOT_FOUND).build()

        // Check password
        if (!BCrypt.checkpw(data.password, user.password)) return Response.status(Response.Status.UNAUTHORIZED).build()

        val loginCookie = NewCookie.Builder("username")
            .value(data.username)
            .domain(".$domain")
            .path("/")
            .maxAge(3600)
            .secure(false)
            .httpOnly(false)
            .build()

        return Response.ok().cookie(loginCookie).build()
    }
}