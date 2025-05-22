package org.awi.fitness.model

import kotlinx.serialization.Serializable

@Serializable
data class WorkoutPlanFirestoreRequest(
    val fields: WorkoutPlanFields
)

@Serializable
data class WorkoutPlanFields(
    val id: StringValue? = null,
    val name: StringValue? = null,
    val description: StringValue? = null,
    val difficulty: StringValue? = null,
    val duration: IntegerValue? = null,
    val category: StringValue? = null,
    val imageUrl: StringValue? = null
)

@Serializable
data class ExerciseFirestoreRequest(
    val fields: ExerciseFields
)

@Serializable
data class ExerciseFields(
    val id: StringValue? = null,
    val planId: StringValue? = null,
    val name: StringValue? = null,
    val description: StringValue? = null,
    val sets: IntegerValue? = null,
    val reps: IntegerValue? = null,
    val restTime: IntegerValue? = null,
    val videoUrl: StringValue? = null,
    val thumbnailUrl: StringValue? = null,
    val isCompleted: BooleanValue? = null,
    val dayOfWeek: IntegerValue? = null,
    val orderInDay: IntegerValue? = null
)

@Serializable
data class WorkoutActivityFirestoreRequest(
    val fields: WorkoutActivityFields
)

@Serializable
data class WorkoutActivityFields(
    val id: StringValue? = null,
    val type: StringValue? = null,
    val description: StringValue? = null,
    val timestamp: LongValue? = null,
    val exerciseId: StringValue? = null,
    val planId: StringValue? = null
)

// Extension functions for converting models to Firestore requests
fun WorkoutPlan.toFirestoreRequest(): WorkoutPlanFirestoreRequest {

    return WorkoutPlanFirestoreRequest(
        fields = WorkoutPlanFields(
            id  = StringValue(id),
            name = StringValue(name),
            description = StringValue(description),
            difficulty = StringValue(difficulty.name),
            duration = IntegerValue(duration),
            category = StringValue(category.name),
            imageUrl = StringValue(imageUrl ?: "")
        )
    )
}

fun Exercise.toFirestoreRequest(): ExerciseFirestoreRequest {
    val fieldsMap = mutableMapOf(
        "planId" to mapOf("stringValue" to planId),
        "name" to mapOf("stringValue" to name),
        "description" to mapOf("stringValue" to description),
        "sets" to mapOf("integerValue" to sets.toString()),
        "reps" to mapOf("integerValue" to reps.toString()),
        "restTime" to mapOf("integerValue" to restTime.toString()),
        "isCompleted" to mapOf("booleanValue" to isCompleted),
        "dayOfWeek" to mapOf("integerValue" to dayOfWeek.toString()),
        "orderInDay" to mapOf("integerValue" to orderInDay.toString())
    )

    // Only add optional fields if they're not null
    videoUrl?.let {
        fieldsMap["videoUrl"] = mapOf("stringValue" to it)
    }
    thumbnailUrl?.let {
        fieldsMap["thumbnailUrl"] = mapOf("stringValue" to it)
    }

    return ExerciseFirestoreRequest(
        fields = ExerciseFields(
            planId = StringValue(planId),
            name = StringValue(name),
            description = StringValue(description),
            sets = IntegerValue(sets),
            reps = IntegerValue(reps),
            restTime = IntegerValue(restTime),
            isCompleted = BooleanValue(isCompleted),
            dayOfWeek = IntegerValue(dayOfWeek),
            orderInDay = IntegerValue(orderInDay),
            videoUrl = videoUrl?.let { StringValue(it) },
            thumbnailUrl = thumbnailUrl?.let { StringValue(it) }
        )
    )
}

fun WorkoutActivity.toFirestoreRequest(): WorkoutActivityFirestoreRequest {
    val fieldsMap = mutableMapOf(
        "type" to mapOf("stringValue" to type.name),
        "description" to mapOf("stringValue" to description),
        "timestamp" to mapOf("integerValue" to timestamp.toString()),
        "exerciseId" to mapOf("stringValue" to exerciseId),
        "planId" to mapOf("stringValue" to planId)
    )

    return WorkoutActivityFirestoreRequest(
        fields = WorkoutActivityFields(
            type = StringValue(type.name),
            description = StringValue(description),
            timestamp = LongValue(timestamp),
            exerciseId = StringValue(exerciseId),
            planId = StringValue(planId)
        )
    )
}

// Extension functions for converting Firestore documents to models
fun FirestoreDocument<WorkoutPlanFields>.toWorkoutPlan(): WorkoutPlan {
    val documentId = name?.split("/")?.last() ?: ""
    return WorkoutPlan(
        id = documentId,
        name = fields?.name?.value ?: "",
        description = fields?.description?.value ?: "",
        difficulty = fields?.difficulty?.value?.let { WorkoutDifficulty.valueOf(it) } ?: WorkoutDifficulty.BEGINNER,
        duration = fields?.duration?.value ?: 12,
        category = fields?.category?.value?.let { WorkoutCategory.valueOf(it) } ?: WorkoutCategory.STRENGTH,
        imageUrl = fields?.imageUrl?.value
    )
}

fun FirestoreDocument<ExerciseFields>.toExercise(): Exercise {
    val documentId = name?.split("/")?.last() ?: ""
    return Exercise(
        id = documentId,
        planId = fields?.planId?.value ?: "",
        name = fields?.name?.value ?: "",
        description = fields?.description?.value ?: "",
        sets = fields?.sets?.value ?: 0,
        reps = fields?.reps?.value ?: 0,
        restTime = fields?.restTime?.value ?: 0,
        videoUrl = fields?.videoUrl?.value,
        thumbnailUrl = fields?.thumbnailUrl?.value,
        isCompleted = fields?.isCompleted?.value ?: false,
        dayOfWeek = fields?.dayOfWeek?.value ?: 1,
        orderInDay = fields?.orderInDay?.value ?: 0
    )
}

fun FirestoreDocument<WorkoutActivityFields>.toWorkoutActivity(): WorkoutActivity {
    val documentId = name?.split("/")?.last() ?: ""
    return WorkoutActivity(
        id = documentId,
        type = fields?.type?.value?.let { ActivityType.valueOf(it) } ?: ActivityType.EXERCISE_COMPLETED,
        description = fields?.description?.value ?: "",
        timestamp = fields?.timestamp?.value ?: 0L,
        exerciseId = fields?.exerciseId?.value ?: "",
        planId = fields?.planId?.value ?: ""
    )
}