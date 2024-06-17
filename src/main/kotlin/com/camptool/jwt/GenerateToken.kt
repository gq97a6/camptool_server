package com.camptool.jwt

import io.smallrye.jwt.build.Jwt
import org.eclipse.microprofile.jwt.Claims

object JWT {
    fun generate(): String = Jwt.issuer("https://example.com/issuer")
        .upn("jdoe@quarkus.io")
        .groups(HashSet(mutableListOf("User", "Admin")))
        .claim(Claims.birthdate.name, "2001-07-13")
        .sign()
}