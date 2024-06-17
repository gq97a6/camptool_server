package com.camptool.resource

import com.camptool.classes.Day
import com.camptool.classes.days
import com.camptool.database.DayTable
import jakarta.ws.rs.*
import jakarta.ws.rs.core.Response

@Path("/day")
class DayResource {
    @GET
    @Path("/{uuid}")
    suspend fun get(@PathParam("uuid") uuid: String): Response {
        DayTable.get(uuid)?.let { return Response.ok(it).build() }
        return Response.status(Response.Status.NO_CONTENT).build()
    }

    @POST
    @Path("/{uuid}")
    suspend fun upsert(data: Day, @PathParam("uuid") uuid: String): Response {
        DayTable.upsert(data)
        return Response.ok().build()
    }

    @GET
    suspend fun getAll(): Response {
        return Response.ok(DayTable.getAll()).build()
    }

    @POST
    @Path("/mockup")
    suspend fun populate(): Response {
        days.forEach { DayTable.upsert(it) }
        return Response.ok("done").build()
    }
}