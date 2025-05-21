package org.awi.fitness.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class FirestoreResponse<T>(
    val name: String? = null,
    val fields: T? = null,
    val createTime: String? = null,
    val updateTime: String? = null,
    val error: SignUpFirebaseError? = null
)

@Serializable
data class FirestoreRequest(
    val fields: ClientFields
)

@Serializable
data class ClientFields(
    val id: StringValue? = null,
    val firstName: StringValue? = null,
    val lastName: StringValue? = null,
    val email: StringValue? = null,
    val phone: StringValue? = null,
    val joinDate: StringValue? = null,
    val plan: StringValue? = null,
    val status: StringValue? = null,
    val goal: StringValue? = null,
    val notes: StringValue? = null,
    val password: StringValue? = null,
    val endDate: StringValue? = null
)

@Serializable
data class StringValue(
    @SerialName("stringValue")
    val value: String
)

@Serializable
data class BooleanValue(
    @SerialName("booleanValue")
    val value: Boolean
)

@Serializable
data class NumberValue(
    @SerialName("doubleValue")
    val value: Double
)

@Serializable
data class FirestoreDocument<T>(
    @SerialName("name")
    val name: String? = null,
    @SerialName("fields")
    val fields: T? = null
)

@Serializable
data class ProgramFirestoreRequest(
    val fields: ProgramFields
)

@Serializable
data class ProgramFields(
    val id: StringValue? = null,
    val type: StringValue? = null,
    val name: StringValue? = null,
    val description: StringValue? = null,
    val assignedClients: ArrayValueWrapper? = null,
    val calories: IntegerValue? = null,
    val protein: IntegerValue? = null,
    val carbs: IntegerValue? = null,
    val fat: IntegerValue? = null,
    val ingredients: ArrayValueWrapper? = null,
    val instructions: ArrayValueWrapper? = null,
    val mealType: StringValue? = null,
    val dietaryTags: ArrayValueWrapper? = null,
    val videoUrls: ArrayValueWrapper? = null
)

@Serializable
data class ArrayValueWrapper(
    val arrayValue: ArrayValue? = ArrayValue(emptyList())
)

@Serializable
data class ArrayValue(
    val values: List<StringValue> = emptyList()
)

@Serializable
data class IntegerValue(
    @SerialName("integerValue")
    val value: Int
)

@Serializable
data class FirestoreListResponse<T>(
    @SerialName("documents")
    val documents: List<FirestoreDocument<T>>? = null
)

fun FirestoreResponse<ClientFields>.toClient(): Client {
    val documentId = name?.split("/")?.last() ?: ""
    return Client(
        id = documentId,
        firstName = fields?.firstName?.value ?: "",
        lastName = fields?.lastName?.value ?: "",
        email = fields?.email?.value ?: "",
        phone = fields?.phone?.value ?: "",
        plan = fields?.plan?.value ?: "",
        joinDate = fields?.joinDate?.value ?: "",
        goal = fields?.goal?.value ?: "",
        notes = fields?.notes?.value ?: "",
        status = fields?.status?.value ?: "Active",
        endDate = fields?.endDate?.value ?: ""
    )
}


fun FirestoreDocument<*>.toClientDocument(): FirestoreDocument<ClientFields> {
    @Suppress("UNCHECKED_CAST")
    return this as FirestoreDocument<ClientFields>
}

fun FirestoreDocument<*>.toProgramDocument(): FirestoreDocument<ProgramFields> {
    @Suppress("UNCHECKED_CAST")
    return this as FirestoreDocument<ProgramFields>
} 