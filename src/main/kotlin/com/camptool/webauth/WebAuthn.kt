package com.camptool.webauth

import com.camptool.Globals.Companion.vertx
import com.camptool.database.AuthenticatorTable
import io.vertx.core.Future
import io.vertx.ext.auth.webauthn.*
import io.vertx.kotlin.coroutines.vertxFuture

val webAuthn: WebAuthn = WebAuthn.create(
    vertx,
    WebAuthnOptions()
        .setRelyingParty(RelyingParty().setName("CampTool"))
        .setUserVerification(UserVerification.PREFERRED)
).authenticatorFetcher { query: Authenticator ->
    vertxFuture(vertx) {
        authenticatorFetcher(query)
    }
}.authenticatorUpdater { authenticator: Authenticator ->
    vertxFuture(vertx) {
        AuthenticatorTable.upsert(authenticator)
    }
    Future.succeededFuture()
}

suspend fun authenticatorFetcher(query: Authenticator): List<Authenticator> {
    return if (query.credID.isNullOrBlank()) AuthenticatorTable.getByUsername(query.userName)
    else {
        val cred = AuthenticatorTable.getByUUID(query.credID)

        if (cred == null) listOf()
        else listOf(cred)
    }
}