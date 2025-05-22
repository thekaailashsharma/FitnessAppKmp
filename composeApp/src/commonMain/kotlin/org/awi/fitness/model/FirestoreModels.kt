package org.awi.fitness.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class FirestoreListResponse<T>(
    val documents: List<FirestoreDocument<T>>? = null
)

@Serializable
data class FirestoreDocument<T>(
    val name: String? = null,
    val fields: T? = null,
    val createTime: String? = null,
    val updateTime: String? = null
)

@Serializable
data class FirestoreResponse<T>(
    val name: String? = null,
    val fields: T? = null,
    val error: FirestoreError? = null
)

@Serializable
data class FirestoreError(
    val code: Int? = null,
    val message: String? = null,
    val status: String? = null
)

@Serializable
data class WorkoutPlanFields(
    @SerialName("id")
    val id: StringValue? = null,
    @SerialName("name")
    val name: StringValue? = null,
    @SerialName("description")
    val description: StringValue? = null,
    @SerialName("difficulty")
    val difficulty: StringValue? = null,
    @SerialName("duration")
    val duration: IntegerValue? = null,
    @SerialName("category")
    val category: StringValue? = null,
    @SerialName("imageUrl")
    val imageUrl: StringValue? = null
)

@Serializable
data class ExerciseFields(
    @SerialName("planId")
    val planId: StringValue? = null,
    @SerialName("name")
    val name: StringValue? = null,
    @SerialName("description")
    val description: StringValue? = null,
    @SerialName("sets")
    val sets: IntegerValue? = null,
    @SerialName("reps")
    val reps: IntegerValue? = null,
    @SerialName("restTime")
    val restTime: IntegerValue? = null,
    @SerialName("videoUrl")
    val videoUrl: StringValue? = null,
    @SerialName("thumbnailUrl")
    val thumbnailUrl: StringValue? = null,
    @SerialName("isCompleted")
    val isCompleted: BooleanValue? = null,
    @SerialName("dayOfWeek")
    val dayOfWeek: IntegerValue? = null,
    @SerialName("orderInDay")
    val orderInDay: IntegerValue? = null
)

@Serializable
data class StringValue(
    @SerialName("stringValue")
    val value: String = ""
)

@Serializable
data class IntegerValue(
    @SerialName("integerValue")
    val value: String = "0"
)

@Serializable
data class BooleanValue(
    @SerialName("booleanValue")
    val value: Boolean = false
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
data class LongValue(
    @SerialName("longValue")
    val value: Long
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

// Extension functions to convert Firestore fields to domain models
fun WorkoutPlanFields.toDomainModel(): WorkoutPlan {
    return WorkoutPlan(
        id = id?.value ?: "",
        name = name?.value ?: "",
        description = description?.value ?: "",
        difficulty = WorkoutDifficulty.valueOf(difficulty?.value ?: "BEGINNER"),
        duration = duration?.value?.toIntOrNull() ?: 45,
        category = WorkoutCategory.valueOf(category?.value ?: "STRENGTH"),
        imageUrl = imageUrl?.value
    )
}

fun ExerciseFields.toDomainModel(documentId: String): Exercise {
    return Exercise(
        id = documentId,
        planId = planId?.value ?: "",
        name = name?.value ?: "",
        description = description?.value ?: "",
        sets = sets?.value?.toIntOrNull() ?: 0,
        reps = reps?.value?.toIntOrNull() ?: 0,
        restTime = restTime?.value?.toIntOrNull() ?: 0,
        videoUrl = videoUrl?.value,
        thumbnailUrl = thumbnailUrl?.value,
        isCompleted = isCompleted?.value ?: false,
        dayOfWeek = dayOfWeek?.value?.toIntOrNull() ?: 1,
        orderInDay = orderInDay?.value?.toIntOrNull() ?: 0
    )
} 