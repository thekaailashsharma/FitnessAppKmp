package org.awi.fitness.model

import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

// Updated Client data class
data class Client @OptIn(ExperimentalUuidApi::class) constructor(
    val id: String = Uuid.random().toString(),
    val firstName: String,
    val lastName: String,
    val joinDate: String,
    val email: String = "",
    val phone: String = "",
    val plan: String = "Basic Plan",
    val status: String = "Active",
    val goal: String = "",
    val notes: String = "",
    val password: String? = null,
    val assignedWorkouts: List<String> = emptyList(),
    val assignedMeals: List<String> = emptyList(),
    val measurements: Map<String, Float> = emptyMap(),
    val progress: List<ProgressEntry> = emptyList(),
    val validTill: String = "",
    val assignedWorkoutGroups: List<String> = emptyList(),
    val assignedMealGroups: List<String> = emptyList()
)
data class ProgressEntry(
    val date: String,
    val weight: Float,
    val bodyFat: Float? = null,
    val notes: String = ""
)


fun Client.toFirestoreRequest(): FirestoreRequest {
    return FirestoreRequest(
        fields = ClientFields(
            id = StringValue(id),
            firstName = StringValue(firstName),
            lastName = StringValue(lastName),
            email = StringValue(email),
            phone = StringValue(phone),
            joinDate = StringValue(joinDate),
            plan = StringValue(plan),
            status = StringValue(status),
            goal = StringValue(goal.orEmpty()),
            notes = StringValue(notes.orEmpty()),
            password = password?.let { StringValue(it) }
        )
    )
}