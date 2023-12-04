package com.campbuddy.resource

import jakarta.ws.rs.GET
import jakarta.ws.rs.Path

@Path("/login2")
class LoginResource {
    @GET
    suspend fun getData() = "key"
}