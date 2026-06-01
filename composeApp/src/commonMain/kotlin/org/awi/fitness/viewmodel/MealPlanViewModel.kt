package org.awi.fitness.viewmodel

import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus
import kotlinx.datetime.todayIn
import org.awi.fitness.data.UserSettings
import org.awi.fitness.model.DietaryType
import org.awi.fitness.model.Meal
import org.awi.fitness.model.MealGoal
import org.awi.fitness.model.MealPlan
import org.awi.fitness.model.MealSlot
import org.awi.fitness.model.toDomain
import org.awi.fitness.repository.GeminiRepository
import org.awi.fitness.repository.MealPlanRepository

enum class GenerationStep {
    IDLE,
    ANALYZING_PREFERENCES,
    CALLING_AI,
    PARSING_RESPONSE,
    SAVING_PLAN
}

data class MealPlanUiState(
    val isLoading: Boolean = false,
    val isGenerating: Boolean = false,
    val isSwapping: Boolean = false,
    val error: String? = null,
    val plans: List<MealPlan> = emptyList(),
    val activePlan: MealPlan? = null,
    val selectedDayOfWeek: Int = 1,
    val todayDayOfWeek: Int = 1,
    val todayDateString: String = "",
    val selectedDateString: String = "",
    val generationStep: GenerationStep = GenerationStep.IDLE
)

class MealPlanViewModel {
    private val mealPlanRepository = MealPlanRepository()
    private val geminiRepository = GeminiRepository()
    private val userSettings = UserSettings.getInstance()

    private val _state = MutableStateFlow(MealPlanUiState())
    val state: StateFlow<MealPlanUiState> = _state.asStateFlow()

    init {
        updateTodayInfo()
    }

    private fun updateTodayInfo() {
        val today = org.awi.fitness.utils.todayLocalDate()
        val dayOfWeek = today.dayOfWeek.ordinal + 1
        val dateString = today.toString()
        _state.update {
            it.copy(
                todayDayOfWeek = dayOfWeek,
                selectedDayOfWeek = dayOfWeek,
                todayDateString = dateString,
                selectedDateString = dateString
            )
        }
    }

    fun selectDay(dayOfWeek: Int) {
        val today = org.awi.fitness.utils.todayLocalDate()
        val diff = dayOfWeek - (today.dayOfWeek.ordinal + 1)
        val selectedDate = today.plus(DatePeriod(days = diff))
        _state.update {
            it.copy(
                selectedDayOfWeek = dayOfWeek,
                selectedDateString = selectedDate.toString()
            )
        }
    }

    fun getMealsForDay(dayOfWeek: Int): List<Meal> {
        val plan = _state.value.activePlan ?: return emptyList()
        return plan.meals
            .filter { it.dayOfWeek == dayOfWeek }
            .sortedBy { slot ->
                when (slot.mealSlot) {
                    MealSlot.BREAKFAST -> 0
                    MealSlot.LUNCH -> 1
                    MealSlot.DINNER -> 2
                    MealSlot.SNACK -> 3
                }
            }
    }

    fun getMealsForSlot(dayOfWeek: Int, slot: MealSlot): List<Meal> {
        return getMealsForDay(dayOfWeek).filter { it.mealSlot == slot }
    }

    fun getSlotsForDay(dayOfWeek: Int): List<MealSlot> {
        val plan = _state.value.activePlan ?: return emptyList()
        return plan.meals
            .filter { it.dayOfWeek == dayOfWeek }
            .map { it.mealSlot }
            .distinct()
            .sortedBy {
                when (it) {
                    MealSlot.BREAKFAST -> 0
                    MealSlot.LUNCH -> 1
                    MealSlot.DINNER -> 2
                    MealSlot.SNACK -> 3
                }
            }
    }

    fun getDailyMacros(dayOfWeek: Int, dateString: String): DailyMacros {
        val plan = _state.value.activePlan ?: return DailyMacros()
        val dayMeals = getMealsForDay(dayOfWeek)
        val completions = userSettings.getCompletionsForDate(dateString)

        val effectiveTarget = plan.targetCalories.takeIf { it > 0 }
            ?: userSettings.calculatedCalories.takeIf { it > 100 }
            ?: 2000

        val consumedCal = dayMeals.filter { completions.contains(it.id) }.sumOf { it.calories }
        val consumedP = dayMeals.filter { completions.contains(it.id) }.sumOf { it.protein }
        val consumedC = dayMeals.filter { completions.contains(it.id) }.sumOf { it.carbs }
        val consumedF = dayMeals.filter { completions.contains(it.id) }.sumOf { it.fat }

        return DailyMacros(
            targetCalories = effectiveTarget,
            targetProtein = plan.targetProtein,
            targetCarbs = plan.targetCarbs,
            targetFat = plan.targetFat,
            consumedCalories = consumedCal,
            consumedProtein = consumedP,
            consumedCarbs = consumedC,
            consumedFat = consumedF
        )
    }

    fun isMealCompleted(mealId: String, dateString: String): Boolean {
        return userSettings.isMealCompleted(dateString, mealId)
    }

    fun toggleMealCompletion(mealId: String, dateString: String) {
        val wasCompleted = userSettings.isMealCompleted(dateString, mealId)
        userSettings.toggleMealCompletion(dateString, mealId)
        // Auto-progress MEALS challenges when a meal is marked as eaten (not un-eaten)
        if (!wasCompleted) {
            org.awi.fitness.viewmodel.ViewModelStore.challenges.autoProgressMealChallenges()
        }
        // Also persist the isCompleted flag to Firestore for cross-device sync
        val plan = _state.value.activePlan ?: return
        val updatedMeals = plan.meals.map { meal ->
            if (meal.id == mealId) meal.copy(isCompleted = !wasCompleted) else meal
        }
        val updatedPlan = plan.copy(meals = updatedMeals)
        _state.update { it.copy(activePlan = updatedPlan) }
        // Fire-and-forget — local state already updated above
        kotlinx.coroutines.MainScope().launch {
            mealPlanRepository.updatePlan(updatedPlan)
        }
    }

    fun isSelectedDayToday(): Boolean {
        return _state.value.selectedDayOfWeek == _state.value.todayDayOfWeek
    }

    suspend fun loadIfNeeded() {
        if (_state.value.activePlan != null || _state.value.isLoading) return
        loadPlans()
    }

    suspend fun loadPlans() {
        val email = userSettings.userEmail ?: return
        _state.update { it.copy(isLoading = true, error = null) }

        mealPlanRepository.getPlansForUser(email).fold(
            onSuccess = { plans ->
                val active = plans.firstOrNull { it.isActive }
                _state.update { it.copy(isLoading = false, plans = plans, activePlan = active) }
            },
            onFailure = { error ->
                _state.update { it.copy(isLoading = false, error = error.message) }
            }
        )
    }

    suspend fun generateAndSavePlan(
        goal: MealGoal,
        dietaryType: DietaryType,
        restrictions: String,
        mealsPerDay: Int,
        targetCalories: Int,
        targetProtein: Int,
        targetCarbs: Int,
        targetFat: Int
    ): Result<MealPlan> {
        _state.update { it.copy(isGenerating = true, error = null, generationStep = GenerationStep.ANALYZING_PREFERENCES) }
        val lang = userSettings.language.value

        delay(600)

        _state.update { it.copy(generationStep = GenerationStep.CALLING_AI) }
        val genResult = geminiRepository.generateMealPlan(
            goal = goal,
            dietaryType = dietaryType,
            restrictions = restrictions,
            mealsPerDay = mealsPerDay,
            targetCalories = targetCalories,
            targetProtein = targetProtein,
            targetCarbs = targetCarbs,
            targetFat = targetFat,
            language = lang
        )

        if (genResult.isFailure) {
            _state.update { it.copy(isGenerating = false, error = genResult.exceptionOrNull()?.message, generationStep = GenerationStep.IDLE) }
            return Result.failure(genResult.exceptionOrNull() ?: Exception("Generation failed"))
        }

        _state.update { it.copy(generationStep = GenerationStep.PARSING_RESPONSE) }
        val generated = genResult.getOrThrow()
        val email = userSettings.userEmail ?: ""
        val now = org.awi.fitness.utils.todayLocalDate().toString()

        val plan = MealPlan(
            clientEmail = email,
            name = generated.planName.ifBlank { "${goal.displayName} ${dietaryType.displayName} Plan" },
            description = generated.description,
            dietaryType = dietaryType,
            mealGoal = goal,
            targetCalories = targetCalories,
            targetProtein = targetProtein,
            targetCarbs = targetCarbs,
            targetFat = targetFat,
            mealsPerDay = mealsPerDay,
            createdAt = now,
            isActive = true,
            meals = generated.meals.map { it.toDomain() },
            shoppingList = generated.shoppingList
        )

        _state.update { it.copy(generationStep = GenerationStep.SAVING_PLAN) }

        _state.value.plans.filter { it.isActive }.forEach { oldPlan ->
            mealPlanRepository.updatePlan(oldPlan.copy(isActive = false))
        }

        val saveResult = mealPlanRepository.createPlan(plan)
        return saveResult.fold(
            onSuccess = { saved ->
                userSettings.clearShoppingChecks()
                loadPlans()
                _state.update { it.copy(isGenerating = false, generationStep = GenerationStep.IDLE) }
                Result.success(saved)
            },
            onFailure = { error ->
                _state.update { it.copy(isGenerating = false, error = error.message, generationStep = GenerationStep.IDLE) }
                Result.failure(error)
            }
        )
    }

    suspend fun swapMeal(meal: Meal) {
        val plan = _state.value.activePlan ?: return
        _state.update { it.copy(isSwapping = true, error = null) }

        val existingNames = plan.meals.map { it.name }
        val result = geminiRepository.generateMealSwap(
            mealSlot = meal.mealSlot,
            dayOfWeek = meal.dayOfWeek,
            dietaryType = plan.dietaryType,
            targetCalories = plan.targetCalories,
            targetProtein = plan.targetProtein,
            targetCarbs = plan.targetCarbs,
            targetFat = plan.targetFat,
            existingMealNames = existingNames,
            restrictions = ""
        )

        result.fold(
            onSuccess = { swapped ->
                val newMeal = swapped.toDomain()
                val updatedMeals = plan.meals.map {
                    if (it.id == meal.id) newMeal.copy(id = meal.id) else it
                }
                val updatedPlan = plan.copy(meals = updatedMeals)
                mealPlanRepository.updatePlan(updatedPlan)
                loadPlans()
                _state.update { it.copy(isSwapping = false) }
            },
            onFailure = { error ->
                _state.update { it.copy(isSwapping = false, error = error.message) }
            }
        )
    }

    suspend fun setActivePlan(planId: String) {
        val plans = _state.value.plans
        plans.forEach { plan ->
            val shouldBeActive = plan.id == planId
            if (plan.isActive != shouldBeActive) {
                mealPlanRepository.updatePlan(plan.copy(isActive = shouldBeActive))
            }
        }
        userSettings.clearShoppingChecks()
        loadPlans()
    }

    suspend fun deletePlan(planId: String) {
        mealPlanRepository.deletePlan(planId)
        loadPlans()
    }

    suspend fun addManualMeal(meal: Meal) {
        val plan = _state.value.activePlan ?: return
        val updatedPlan = plan.copy(meals = plan.meals + meal)
        mealPlanRepository.updatePlan(updatedPlan)
        loadPlans()
    }

    suspend fun updateMeal(updatedMeal: Meal) {
        val plan = _state.value.activePlan ?: return
        val updatedMeals = plan.meals.map { if (it.id == updatedMeal.id) updatedMeal else it }
        val updatedPlan = plan.copy(meals = updatedMeals)
        mealPlanRepository.updatePlan(updatedPlan)
        loadPlans()
    }

    suspend fun removeMeal(mealId: String) {
        val plan = _state.value.activePlan ?: return
        val updatedMeals = plan.meals.filter { it.id != mealId }
        val updatedPlan = plan.copy(meals = updatedMeals)
        mealPlanRepository.updatePlan(updatedPlan)
        loadPlans()
    }

    fun clearError() {
        _state.update { it.copy(error = null) }
    }
}

data class DailyMacros(
    val targetCalories: Int = 0,
    val targetProtein: Int = 0,
    val targetCarbs: Int = 0,
    val targetFat: Int = 0,
    val consumedCalories: Int = 0,
    val consumedProtein: Int = 0,
    val consumedCarbs: Int = 0,
    val consumedFat: Int = 0
)
