package com.campbuddy.webauth

import io.vertx.core.Future
import io.vertx.core.Vertx
import io.vertx.ext.auth.webauthn.*

val authDb = mutableListOf<Authenticator>()

val webAuthn: WebAuthn = WebAuthn.create(
    Vertx.vertx(),
    WebAuthnOptions()
        .setRelyingParty(RelyingParty().setName("CampBuddy"))
        .setUserVerification(UserVerification.PREFERRED)
).authenticatorFetcher { query: Authenticator ->
    authDb.filter {
        it.userName == query.userName
    }.let {
        Future.succeededFuture(it)
    }
}.authenticatorUpdater { authenticator: Authenticator ->
    authDb.indexOfFirst {
        it.userName == authenticator.userName
    }.let {
        if (it == -1) {
            authDb.add(authenticator)
            Future.succeededFuture()
        } else {
            authDb[it] = authenticator
            Future.succeededFuture()
        }
    }
}