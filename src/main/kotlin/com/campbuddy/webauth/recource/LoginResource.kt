package com.campbuddy.webauth.recource

import io.quarkus.hibernate.reactive.panache.kotlin.PanacheEntityBase
import io.quarkus.hibernate.reactive.panache.common.WithTransaction
import io.quarkus.security.webauthn.WebAuthnLoginResponse
import io.quarkus.security.webauthn.WebAuthnRegisterResponse
import io.quarkus.security.webauthn.WebAuthnSecurity
import io.smallrye.mutiny.Uni
import io.vertx.ext.auth.webauthn.Authenticator
import io.vertx.ext.web.RoutingContext
import jakarta.inject.Inject
import jakarta.ws.rs.BeanParam
import jakarta.ws.rs.POST
import jakarta.ws.rs.Path
import jakarta.ws.rs.core.Response
import com.campbuddy.webauth.User
import com.campbuddy.webauth.User.Companion.findByUserName
import com.campbuddy.webauth.WebAuthnCredential
import org.jboss.resteasy.reactive.RestForm

@Path("")
class LoginResource {
    @Inject
    var webAuthnSecurity: WebAuthnSecurity? = null
    @Path("/login")
    @POST
    @WithTransaction
    fun login(
        @RestForm userName: String?,
        @BeanParam webAuthnResponse: WebAuthnLoginResponse,
        ctx: RoutingContext?
    ): Uni<Response> {
        // Input validation
        if (userName.isNullOrEmpty() || !webAuthnResponse.isSet || !webAuthnResponse.isValid) {
            return Uni.createFrom().item(Response.status(Response.Status.BAD_REQUEST).build())
        }
        val userUni = findByUserName(userName)
        return userUni.flatMap { user: User? ->
            if (user == null) {
                // Invalid user
                return@flatMap Uni.createFrom().item<Response>(Response.status(Response.Status.BAD_REQUEST).build())
            }
            val authenticator = webAuthnSecurity!!.login(webAuthnResponse, ctx)
            authenticator // bump the auth counter
                .invoke { auth: Authenticator -> user.webAuthnCredential!!.counter = auth.counter }
                .map { auth: Authenticator ->
                    // make a login cookie
                    webAuthnSecurity!!.rememberUser(auth.userName, ctx)
                    Response.ok().build()
                } // handle login failure
                .onFailure().recoverWithItem { x: Throwable? -> Response.status(Response.Status.BAD_REQUEST).build() }
        }
    }

    @Path("/register")
    @POST
    @WithTransaction
    fun register(
        @RestForm userName: String?,
        @BeanParam webAuthnResponse: WebAuthnRegisterResponse,
        ctx: RoutingContext?
    ): Uni<Response> {
        // Input validation
        if (userName.isNullOrEmpty() || !webAuthnResponse.isSet || !webAuthnResponse.isValid) {
            return Uni.createFrom().item(Response.status(Response.Status.BAD_REQUEST).build())
        }
        val userUni = findByUserName(userName)
        return userUni.flatMap { user: User? ->
            if (user != null) {
                // Duplicate user
                return@flatMap Uni.createFrom().item<Response>(Response.status(Response.Status.BAD_REQUEST).build())
            }
            val authenticator = webAuthnSecurity!!.register(webAuthnResponse, ctx)
            authenticator // store the user
                .flatMap { auth: Authenticator ->
                    val newUser = User()
                    newUser.userName = auth.userName
                    val credential = WebAuthnCredential(auth, newUser)
                    credential.persist<PanacheEntityBase>().flatMap { c: PanacheEntityBase? -> newUser.persist<User>() }
                }.map { newUser: User ->
                    // make a login cookie
                    webAuthnSecurity!!.rememberUser(newUser.userName, ctx)
                    Response.ok().build()
                } // handle login failure
                .onFailure().recoverWithItem { x: Throwable? -> Response.status(Response.Status.BAD_REQUEST).build() }
        }
    }
}