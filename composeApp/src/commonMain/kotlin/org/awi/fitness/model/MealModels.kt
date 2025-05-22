package org.awi.fitness.model

import androidx.compose.ui.graphics.vector.ImageVector
import compose.icons.FeatherIcons
import compose.icons.SimpleIcons
import compose.icons.feathericons.Activity
import compose.icons.simpleicons.Ifood
import kotlinx.serialization.Serializable
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
data class Program(
    val id: String = Uuid.random().toString(),
    val type: ProgramType,
    val name: String,
    val description: String,
    val assignedClients: List<String> = emptyList(),
    val stats: List<ProgramStat>,
    val videos: List<String> = emptyList(),
    val calories: Int? = null,
    val protein: Int? = null,
    val carbs: Int? = null,
    val fat: Int? = null,
    val ingredients: List<String> = emptyList(),
    val instructions: List<String> = emptyList(),
    val mealType: String? = null,
    val dietaryTags: List<String> = emptyList(),
    val isAiGenerated: Boolean = false
)

enum class ProgramType(val title: String, val icon: ImageVector) {
    WORKOUT("Workouts", FeatherIcons.Activity),
    MEAL("Meal Plans", SimpleIcons.Ifood)
}

data class ProgramStat(
    val icon: ImageVector,
    val value: String
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
data class ProgramFirestoreRequest(
    val fields: ProgramFields
)

fun FirestoreDocument<ProgramFields>.toProgram(): Program {
    val fields = fields ?: return Program(
        type = ProgramType.MEAL,
        name = "",
        description = "",
        stats = emptyList()
    )

    return Program(
        id = fields.id?.value ?: "",
        type = ProgramType.valueOf(fields.type?.value ?: ProgramType.MEAL.name),
        name = fields.name?.value ?: "",
        description = fields.description?.value ?: "",
        assignedClients = fields.assignedClients?.arrayValue?.values?.map { it.value } ?: emptyList(),
        calories = fields.calories?.value?.toIntOrNull() ?: 0,
        protein = fields.protein?.value?.toIntOrNull() ?: 0,
        carbs = fields.carbs?.value?.toIntOrNull() ?: 0,
        fat = fields.fat?.value?.toIntOrNull() ?: 0,
        ingredients = fields.ingredients?.arrayValue?.values?.map { it.value } ?: emptyList(),
        instructions = fields.instructions?.arrayValue?.values?.map { it.value } ?: emptyList(),
        mealType = fields.mealType?.value,
        dietaryTags = fields.dietaryTags?.arrayValue?.values?.map { it.value } ?: emptyList(),
        videos = fields.videoUrls?.arrayValue?.values?.map { it.value } ?: emptyList(),
        stats = emptyList(),
    )
}