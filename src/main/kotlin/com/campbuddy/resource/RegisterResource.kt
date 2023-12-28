package com.campbuddy.resource

import com.campbuddy.database.UserTable
import com.campbuddy.`object`.User
import jakarta.ws.rs.POST
import jakarta.ws.rs.Path
import jakarta.ws.rs.core.Response

@Path("/register")
class RegisterResource {

    data class LoginData(
        val username: String?,
        val password: String?,
    )

    @POST
    suspend fun register(data: LoginData): Response {
        // Validate data
        if (data.username.isNullOrBlank() || data.password.isNullOrBlank())
            return Response.status(Response.Status.BAD_REQUEST).build()

        val response = UserTable.register(User("", data.username, data.password))
        return when(response.first) {
            UserTable.RegisterResponse.USERNAME_TAKEN -> Response.status(Response.Status.CONFLICT)
            UserTable.RegisterResponse.SUCCESS -> Response.status(Response.Status.OK)
            UserTable.RegisterResponse.UUID_GENERATION_ERROR -> Response.status(Response.Status.INTERNAL_SERVER_ERROR)
            UserTable.RegisterResponse.UPSERT_ERROR -> Response.status(Response.Status.INTERNAL_SERVER_ERROR)
        }.build()
    }
}