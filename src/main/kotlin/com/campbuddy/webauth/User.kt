package com.campbuddy.webauth

import io.quarkus.hibernate.reactive.panache.kotlin.PanacheCompanion
import io.quarkus.hibernate.reactive.panache.kotlin.PanacheEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.OneToOne
import jakarta.persistence.Table

@Table(name = "user_table")
@Entity
open class User : PanacheEntity() {
    @Column(unique = true)
    var userName: String? = null

    // non-owning side, so we can add more credentials later
    @OneToOne(mappedBy = "user")
    var webAuthnCredential: WebAuthnCredential? = null

    companion object : PanacheCompanion<User> {
        @JvmStatic
        fun findByUserName(userName: String) = find("userName", userName).firstResult()
    }
}
