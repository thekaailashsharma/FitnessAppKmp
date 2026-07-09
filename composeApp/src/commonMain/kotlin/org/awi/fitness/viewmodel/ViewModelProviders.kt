package org.awi.fitness.viewmodel

import androidx.compose.runtime.compositionLocalOf
import org.awi.fitness.data.UserSettings
import org.awi.fitness.data.createSettings

val LocalHomeViewModel = compositionLocalOf<HomeViewModel> { error("No HomeViewModel provided") }
val LocalWorkoutViewModel = compositionLocalOf<WorkoutViewModel> { error("No WorkoutViewModel provided") }
val LocalMealPlanViewModel = compositionLocalOf<MealPlanViewModel> { error("No MealPlanViewModel provided") }
val LocalLanguageViewModel = compositionLocalOf<LanguageViewModel> { error("No LanguageViewModel provided") }
val LocalArticleViewModel = compositionLocalOf<ArticleViewModel> { error("No ArticleViewModel provided") }
val LocalBackdropViewModel = compositionLocalOf<BackdropViewModel> { error("No BackdropViewModel provided") }

object ViewModelStore {
    val home: HomeViewModel by lazy { HomeViewModel() }
    val workout: WorkoutViewModel by lazy { WorkoutViewModel() }
    val mealPlan: MealPlanViewModel by lazy { MealPlanViewModel() }
    val article: ArticleViewModel by lazy { ArticleViewModel() }
    val language: LanguageViewModel by lazy { LanguageViewModel(createSettings()) }
    val challenges: ChallengesViewModel by lazy { ChallengesViewModel() }
    val community: CommunityViewModel by lazy { CommunityViewModel() }
    val backdrop: BackdropViewModel by lazy { BackdropViewModel() }
}
