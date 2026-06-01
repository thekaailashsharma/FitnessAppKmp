package org.awi.fitness.model

import kotlinx.serialization.Serializable
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

// ── Domain Models ──

@Serializable
enum class MealSlot(val displayName: String) {
    BREAKFAST("Breakfast"),
    LUNCH("Lunch"),
    DINNER("Dinner"),
    SNACK("Snack")
}

@Serializable
enum class DietaryType(val displayName: String) {
    BALANCED("Balanced"),
    KETO("Keto"),
    VEGETARIAN("Vegetarian"),
    VEGAN("Vegan"),
    MEDITERRANEAN("Mediterranean"),
    HIGH_PROTEIN("High Protein")
}

@Serializable
enum class MealGoal(val displayName: String) {
    WEIGHT_LOSS("Weight Loss"),
    MUSCLE_GAIN("Muscle Gain"),
    MAINTENANCE("Maintenance"),
    GENERAL_HEALTH("General Health")
}

@Serializable
data class Meal(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val mealSlot: MealSlot = MealSlot.BREAKFAST,
    val dayOfWeek: Int = 1,
    val calories: Int = 0,
    val protein: Int = 0,
    val carbs: Int = 0,
    val fat: Int = 0,
    val ingredients: List<String> = emptyList(),
    val instructions: List<String> = emptyList(),
    val prepTimeMinutes: Int = 0,
    val dietaryTags: List<String> = emptyList()
)

@OptIn(ExperimentalUuidApi::class)
@Serializable
data class MealPlan(
    val id: String = Uuid.random().toString(),
    val clientEmail: String = "",
    val name: String = "",
    val description: String = "",
    val dietaryType: DietaryType = DietaryType.BALANCED,
    val mealGoal: MealGoal = MealGoal.GENERAL_HEALTH,
    val targetCalories: Int = 2000,
    val targetProtein: Int = 150,
    val targetCarbs: Int = 200,
    val targetFat: Int = 65,
    val mealsPerDay: Int = 3,
    val createdAt: String = "",
    val isActive: Boolean = true,
    val meals: List<Meal> = emptyList(),
    val shoppingList: Map<String, List<String>> = emptyMap()
)

// ── Firestore DTOs ──

@Serializable
data class MealPlanFields(
    val clientEmail: StringValue? = null,
    val name: StringValue? = null,
    val description: StringValue? = null,
    val dietaryType: StringValue? = null,
    val mealGoal: StringValue? = null,
    val targetCalories: IntegerValue? = null,
    val targetProtein: IntegerValue? = null,
    val targetCarbs: IntegerValue? = null,
    val targetFat: IntegerValue? = null,
    val mealsPerDay: IntegerValue? = null,
    val createdAt: StringValue? = null,
    val isActive: BooleanValue? = null,
    val mealsData: StringValue? = null,
    val shoppingListData: StringValue? = null
)

@Serializable
data class MealPlanFirestoreRequest(
    val fields: MealPlanFields
)

// ── Gemini Response DTOs ──

@Serializable
data class MealPlanGenerationResponse(
    val planName: String = "",
    val description: String = "",
    val meals: List<GeneratedMeal> = emptyList(),
    val shoppingList: Map<String, List<String>> = emptyMap()
)

@Serializable
data class GeneratedMeal(
    val id: String = "",
    val name: String = "",
    val mealSlot: String = "",
    val dayOfWeek: Int = 1,
    val calories: Int = 0,
    val protein: Int = 0,
    val carbs: Int = 0,
    val fat: Int = 0,
    val ingredients: List<String> = emptyList(),
    val instructions: List<String> = emptyList(),
    val prepTimeMinutes: Int = 0,
    val dietaryTags: List<String> = emptyList()
)

@Serializable
data class GeneratedMealSwapResponse(
    val id: String = "",
    val name: String = "",
    val mealSlot: String = "",
    val dayOfWeek: Int = 1,
    val calories: Int = 0,
    val protein: Int = 0,
    val carbs: Int = 0,
    val fat: Int = 0,
    val ingredients: List<String> = emptyList(),
    val instructions: List<String> = emptyList(),
    val prepTimeMinutes: Int = 0,
    val dietaryTags: List<String> = emptyList()
)

// ── Mappers ──

private val mealPlanJson = kotlinx.serialization.json.Json {
    ignoreUnknownKeys = true
    isLenient = true
    coerceInputValues = true
    encodeDefaults = true
}

fun GeneratedMeal.toDomain(): Meal {
    val slot = try {
        MealSlot.valueOf(mealSlot.uppercase())
    } catch (_: Exception) {
        MealSlot.BREAKFAST
    }
    return Meal(
        id = id.ifBlank { "d${dayOfWeek}_${slot.name.lowercase()}" },
        name = name,
        mealSlot = slot,
        dayOfWeek = dayOfWeek,
        calories = calories,
        protein = protein,
        carbs = carbs,
        fat = fat,
        ingredients = ingredients,
        instructions = instructions,
        prepTimeMinutes = prepTimeMinutes,
        dietaryTags = dietaryTags
    )
}

fun GeneratedMealSwapResponse.toDomain(): Meal {
    val slot = try {
        MealSlot.valueOf(mealSlot.uppercase())
    } catch (_: Exception) {
        MealSlot.BREAKFAST
    }
    return Meal(
        id = id.ifBlank { "swap_${dayOfWeek}_${slot.name.lowercase()}" },
        name = name,
        mealSlot = slot,
        dayOfWeek = dayOfWeek,
        calories = calories,
        protein = protein,
        carbs = carbs,
        fat = fat,
        ingredients = ingredients,
        instructions = instructions,
        prepTimeMinutes = prepTimeMinutes,
        dietaryTags = dietaryTags
    )
}

fun MealPlan.toFirestoreRequest(): MealPlanFirestoreRequest {
    val mealsJson = mealPlanJson.encodeToString<List<Meal>>(meals)
    val shoppingJson = mealPlanJson.encodeToString<Map<String, List<String>>>(shoppingList)
    return MealPlanFirestoreRequest(
        fields = MealPlanFields(
            clientEmail = StringValue(clientEmail),
            name = StringValue(name),
            description = StringValue(description),
            dietaryType = StringValue(dietaryType.name),
            mealGoal = StringValue(mealGoal.name),
            targetCalories = IntegerValue(targetCalories.toString()),
            targetProtein = IntegerValue(targetProtein.toString()),
            targetCarbs = IntegerValue(targetCarbs.toString()),
            targetFat = IntegerValue(targetFat.toString()),
            mealsPerDay = IntegerValue(mealsPerDay.toString()),
            createdAt = StringValue(createdAt),
            isActive = BooleanValue(isActive),
            mealsData = StringValue(mealsJson),
            shoppingListData = StringValue(shoppingJson)
        )
    )
}

fun FirestoreDocument<MealPlanFields>.toMealPlan(): MealPlan {
    val documentId = name?.split("/")?.last() ?: ""
    val f = fields ?: return MealPlan(id = documentId)

    val meals: List<Meal> = try {
        f.mealsData?.value?.takeIf { it.isNotBlank() }?.let {
            mealPlanJson.decodeFromString<List<Meal>>(it)
        } ?: emptyList()
    } catch (_: Exception) {
        emptyList()
    }

    val shopping: Map<String, List<String>> = try {
        f.shoppingListData?.value?.takeIf { it.isNotBlank() }?.let {
            mealPlanJson.decodeFromString<Map<String, List<String>>>(it)
        } ?: emptyMap()
    } catch (_: Exception) {
        emptyMap()
    }

    return MealPlan(
        id = documentId,
        clientEmail = f.clientEmail?.value ?: "",
        name = f.name?.value ?: "",
        description = f.description?.value ?: "",
        dietaryType = try { DietaryType.valueOf(f.dietaryType?.value ?: "") } catch (_: Exception) { DietaryType.BALANCED },
        mealGoal = try { MealGoal.valueOf(f.mealGoal?.value ?: "") } catch (_: Exception) { MealGoal.GENERAL_HEALTH },
        targetCalories = f.targetCalories?.value?.toIntOrNull() ?: 2000,
        targetProtein = f.targetProtein?.value?.toIntOrNull() ?: 150,
        targetCarbs = f.targetCarbs?.value?.toIntOrNull() ?: 200,
        targetFat = f.targetFat?.value?.toIntOrNull() ?: 65,
        mealsPerDay = f.mealsPerDay?.value?.toIntOrNull() ?: 3,
        createdAt = f.createdAt?.value ?: "",
        isActive = f.isActive?.value ?: false,
        meals = meals,
        shoppingList = shopping
    )
}

fun FirestoreResponse<MealPlanFields>.toMealPlan(): MealPlan {
    val documentId = name?.split("/")?.last() ?: ""
    val f = fields ?: return MealPlan(id = documentId)

    val meals: List<Meal> = try {
        f.mealsData?.value?.takeIf { it.isNotBlank() }?.let {
            mealPlanJson.decodeFromString<List<Meal>>(it)
        } ?: emptyList()
    } catch (_: Exception) {
        emptyList()
    }

    val shopping: Map<String, List<String>> = try {
        f.shoppingListData?.value?.takeIf { it.isNotBlank() }?.let {
            mealPlanJson.decodeFromString<Map<String, List<String>>>(it)
        } ?: emptyMap()
    } catch (_: Exception) {
        emptyMap()
    }

    return MealPlan(
        id = documentId,
        clientEmail = f.clientEmail?.value ?: "",
        name = f.name?.value ?: "",
        description = f.description?.value ?: "",
        dietaryType = try { DietaryType.valueOf(f.dietaryType?.value ?: "") } catch (_: Exception) { DietaryType.BALANCED },
        mealGoal = try { MealGoal.valueOf(f.mealGoal?.value ?: "") } catch (_: Exception) { MealGoal.GENERAL_HEALTH },
        targetCalories = f.targetCalories?.value?.toIntOrNull() ?: 2000,
        targetProtein = f.targetProtein?.value?.toIntOrNull() ?: 150,
        targetCarbs = f.targetCarbs?.value?.toIntOrNull() ?: 200,
        targetFat = f.targetFat?.value?.toIntOrNull() ?: 65,
        mealsPerDay = f.mealsPerDay?.value?.toIntOrNull() ?: 3,
        createdAt = f.createdAt?.value ?: "",
        isActive = f.isActive?.value ?: false,
        meals = meals,
        shoppingList = shopping
    )
}
