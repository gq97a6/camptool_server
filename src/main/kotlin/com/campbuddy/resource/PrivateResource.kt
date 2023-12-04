package com.campbuddy.resource

import jakarta.ws.rs.GET
import jakarta.ws.rs.Path
import com.campbuddy.service.PrivateService

@Path("/private2")
class PrivateResource {
    @GET
    suspend fun getData() = PrivateService.getAllData()
}