package com.campbuddy.webauth

import io.quarkus.hibernate.reactive.panache.kotlin.PanacheEntity
import jakarta.persistence.Entity
import jakarta.persistence.ManyToOne

@Entity
open class WebAuthnCertificate : PanacheEntity() {
    @ManyToOne
    var webAuthnCredential: WebAuthnCredential? = null

    /**
     * The list of X509 certificates encoded as base64url.
     */
    var x5c: String? = null
}
