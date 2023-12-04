package com.campbuddy.webauth

import io.quarkus.hibernate.reactive.panache.common.WithTransaction
import io.quarkus.hibernate.reactive.panache.kotlin.PanacheEntityBase
import io.quarkus.security.webauthn.WebAuthnUserProvider
import io.smallrye.mutiny.Uni
import io.vertx.ext.auth.webauthn.AttestationCertificates
import io.vertx.ext.auth.webauthn.Authenticator
import jakarta.enterprise.context.ApplicationScoped
import com.campbuddy.webauth.WebAuthnCredential.Companion.findByCredID

@ApplicationScoped
class MyWebAuthnSetup : WebAuthnUserProvider {
    @WithTransaction
    override fun findWebAuthnCredentialsByUserName(userName: String): Uni<List<Authenticator>> {
        return WebAuthnCredential.findByUserName(userName)
            .flatMap { dbs: List<WebAuthnCredential> -> toAuthenticators(dbs) }
    }

    @WithTransaction
    override fun findWebAuthnCredentialsByCredID(credID: String): Uni<List<Authenticator>> {
        return findByCredID(credID)
            .flatMap { dbs: List<WebAuthnCredential> -> toAuthenticators(dbs) }
    }

    @WithTransaction
    override fun updateOrStoreWebAuthnCredentials(authenticator: Authenticator): Uni<Void> {
        // leave the scooby user to the manual endpoint, because if we do it here it will be
        // created/udated twice
        return if (authenticator.userName == "scooby") Uni.createFrom().nullItem() else User.findByUserName(
            authenticator.userName
        )
            .flatMap { user: User? ->
                // new user
                if (user == null) {
                    val newUser = User()
                    newUser.userName = authenticator.userName
                    val credential = WebAuthnCredential(authenticator, newUser)
                    return@flatMap credential.persist<PanacheEntityBase>()
                        .flatMap<Any> { newUser.persist<PanacheEntityBase>() }
                        .onItem().ignore().andContinueWithNull()
                } else {
                    // existing user
                    user.webAuthnCredential!!.counter = authenticator.counter
                    return@flatMap Uni.createFrom().nullItem<Void>()
                }
            }
    }

    override fun getRoles(userId: String): Set<String> {
        if (userId == "admin") {
            val ret: MutableSet<String> = HashSet()
            ret.add("user")
            ret.add("admin")
            return ret
        }
        return setOf("user")
    }

    companion object {
        private fun toAuthenticators(dbs: List<WebAuthnCredential>): Uni<List<Authenticator>> {
            // can't call combine/uni on empty list
            if (dbs.isEmpty()) return Uni.createFrom().item(emptyList())
            val ret: MutableList<Uni<Authenticator>> = ArrayList(dbs.size)
            for (db in dbs) {
                ret.add(toAuthenticator(db))
            }

            return Uni.combine().all().unis<Any>(ret).with {
                it as List<Authenticator>
            }
        }

        private fun toAuthenticator(credential: WebAuthnCredential): Uni<Authenticator> {
            return credential.fetch<List<WebAuthnCertificate>>(credential.x5c)
                .map { x5c: List<WebAuthnCertificate> ->
                    val ret = Authenticator()
                    ret.setAaguid(credential.aaguid)
                    val attestationCertificates = AttestationCertificates()
                    attestationCertificates.setAlg(credential.alg)
                    val x5cs: MutableList<String?> = ArrayList(x5c.size)
                    for (webAuthnCertificate in x5c) {
                        x5cs.add(webAuthnCertificate.x5c)
                    }
                    ret.setAttestationCertificates(attestationCertificates)
                    ret.setCounter(credential.counter)
                    ret.setCredID(credential.credID)
                    ret.setFmt(credential.fmt)
                    ret.setPublicKey(credential.publicKey)
                    ret.setType(credential.type)
                    ret.setUserName(credential.userName)
                    ret
                }
        }
    }
}
