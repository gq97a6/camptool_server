package com.campbuddy.resource

import com.campbuddy.database.KidTable
import com.campbuddy.classes.Kid
import com.campbuddy.classes.kids
import jakarta.ws.rs.*
import jakarta.ws.rs.core.Response

@Path("/kid")
class KidResource {
    @GET
    @Path("/{uuid}")
    suspend fun get(@PathParam("uuid") uuid: String): Response {
        KidTable.get(uuid)?.let { return Response.ok(it).build() }
        return Response.status(Response.Status.NO_CONTENT).build()
    }

    @POST
    @Path("/{uuid}")
    suspend fun upsert(data: Kid, @PathParam("uuid") uuid: String): Response {
        KidTable.upsert(data)
        return Response.ok().build()
    }

    @GET
    suspend fun getAll(): Response {
        return Response.ok(KidTable.getAll()).build()
    }

    @POST
    @Path("/mockup")
    suspend fun populate(): Response {
        kids.forEach { KidTable.upsert(it) }
        return Response.ok("done").build()
    }
}