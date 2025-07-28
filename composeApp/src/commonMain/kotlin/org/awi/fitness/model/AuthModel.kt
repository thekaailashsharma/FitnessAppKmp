package org.awi.fitness.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UserRegistrationRequest(
    val email: String,
    val password: String
)

@Serializable
data class UserLoginRequest(
    val email: String,
    val password: String
)

@Serializable
data class SignUpFirebaseAuth(
    @SerialName("email")
    val email: String?,
    @SerialName("password")
    val password: String?,
    @SerialName("returnSecureToken")
    val returnSecureToken: Boolean? = true
)

@Serializable
data class SignUpFirebaseResponse(
    @SerialName("idToken")
    val idToken: String? = null,
    @SerialName("email")
    val email: String? = null,
    @SerialName("refreshToken")
    val refreshToken: String? = null,
    @SerialName("expiresIn")
    val expiresIn: String? = null,
    @SerialName("localId")
    val localId: String? = null,
    val error: SignUpFirebaseError? = null
)

@Serializable
data class RefreshTokenResponse(
    @SerialName("id_token")
    val idToken: String? = null,
    @SerialName("user_id")
    val userId: String? = null,
    @SerialName("refresh_token")
    val refreshToken: String? = null,
    @SerialName("expires_in")
    val expiresIn: String? = null,
    val error: SignUpFirebaseError? = null
)

@Serializable
data class SignUpFirebaseError(
    @SerialName("code")
    val code: Int? = null,
    @SerialName("message")
    val message: String? = null,
    @SerialName("errors")
    val errors: List<FirebaseErrorDetail>? = null
)

@Serializable
data class FirebaseErrorDetail(
    @SerialName("message")
    val message: String? = null,
    @SerialName("domain")
    val domain: String? = null,
    @SerialName("reason")
    val reason: String? = null
)

@Serializable
data class RefreshTokenRequest(
    @SerialName("grant_type")
    val grantType: String,
    @SerialName("refresh_token")
    val refreshToken: String
)