package com.campbuddy.webauth

import io.quarkus.hibernate.reactive.panache.kotlin.PanacheCompanion
import io.quarkus.hibernate.reactive.panache.kotlin.PanacheEntity
import io.smallrye.mutiny.Uni
import io.vertx.ext.auth.webauthn.Authenticator
import io.vertx.ext.auth.webauthn.PublicKeyCredential
import jakarta.persistence.*
import org.hibernate.reactive.mutiny.Mutiny

@Table(uniqueConstraints = [UniqueConstraint(columnNames = ["userName", "credID"])])
@Entity
open class WebAuthnCredential : PanacheEntity {
    /**
     * The username linked to this authenticator
     */
    var userName: String? = null

    /**
     * The type of key (must be "public-key")
     */
    var type = "public-key"

    /**
     * The non user identifiable id for the authenticator
     */
    var credID: String? = null

    /**
     * The public key associated with this authenticator
     */
    var publicKey: String? = null

    /**
     * The signature counter of the authenticator to prevent replay attacks
     */
    var counter: Long = 0
    var aaguid: String? = null
    /**
     * The Authenticator attestation certificates object, a JSON like:
     * <pre>`{
     * "alg": "string",
     * "x5c": [
     * "base64"
     * ]
     * }
    `</pre> *
     */
    /**
     * The algorithm used for the public credential
     */
    var alg: PublicKeyCredential? = null

    /**
     * The list of X509 certificates encoded as base64url.
     */
    @OneToMany(mappedBy = "webAuthnCredential")
    var x5c: MutableList<WebAuthnCertificate> = ArrayList()
    var fmt: String? = null

    // owning side
    @OneToOne
    var user: User? = null

    constructor()
    constructor(authenticator: Authenticator, user: User) {
        aaguid = authenticator.aaguid
        if (authenticator.attestationCertificates != null) alg = authenticator.attestationCertificates.alg
        counter = authenticator.counter
        credID = authenticator.credID
        fmt = authenticator.fmt
        publicKey = authenticator.publicKey
        type = authenticator.type
        userName = authenticator.userName
        if (authenticator.attestationCertificates != null
            && authenticator.attestationCertificates.x5c != null
        ) {
            for (x5c in authenticator.attestationCertificates.x5c) {
                val cert = WebAuthnCertificate()
                cert.x5c = x5c
                cert.webAuthnCredential = this
                this.x5c.add(cert)
            }
        }
        this.user = user
        user.webAuthnCredential = this
    }

    fun <T> fetch(association: T): Uni<T> {
        return getSession().flatMap { session: Mutiny.Session -> session.fetch(association) }
    }

    companion object: PanacheCompanion<WebAuthnCredential> {
        @JvmStatic
        fun findByUserName(userName: String) = list("userName", userName)

        @JvmStatic
        fun findByCredID(credID: String) = list("credID", credID)
    }
}
