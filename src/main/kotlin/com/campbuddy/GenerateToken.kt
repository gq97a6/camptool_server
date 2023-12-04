package com.campbuddy

import io.smallrye.jwt.build.Jwt
import org.eclipse.microprofile.jwt.Claims

object GenerateToken {
    /**
     * Generate JWT token
     */
    @JvmStatic
    fun main(args: Array<String>) {
        val token = Jwt.issuer("https://example.com/issuer")
            .upn("jdoe@quarkus.io")
            .groups(HashSet(mutableListOf("User", "Admin")))
            .claim(Claims.birthdate.name, "2001-07-13")
            .sign()
        println(token)
    }
}