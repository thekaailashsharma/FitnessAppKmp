package org.awi.fitness.viewmodel

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.awi.fitness.repository.SubscriptionOffering
import org.awi.fitness.repository.SubscriptionPlan
import org.awi.fitness.repository.SubscriptionRepository

data class SubscriptionUiState(
    val isLoading: Boolean = false,
    val isPremium: Boolean = false,
    val offering: SubscriptionOffering? = null,
    val selectedPlan: SubscriptionPlan? = null,
    val successMessage: String? = null,
    val errorMessage: String? = null,
    val isPurchasing: Boolean = false,
    val isRestoring: Boolean = false
)

class SubscriptionViewModel {
    private val repository = SubscriptionRepository()
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    private val _state = MutableStateFlow(SubscriptionUiState())
    val state: StateFlow<SubscriptionUiState> = _state.asStateFlow()

    init {
        loadOfferings()
    }

    fun loadOfferings() {
        scope.launch {
            _state.value = _state.value.copy(isLoading = true, errorMessage = null)
            val result = repository.getOfferings()
            result.fold(
                onSuccess = { offering ->
                    val defaultSelected = offering.annualPlan ?: offering.plans.firstOrNull()
                    _state.value = _state.value.copy(
                        isLoading = false,
                        offering = offering,
                        selectedPlan = defaultSelected
                    )
                },
                onFailure = { error ->
                    _state.value = _state.value.copy(
                        isLoading = false,
                        errorMessage = error.message
                    )
                }
            )
        }
    }

    fun selectPlan(plan: SubscriptionPlan) {
        _state.value = _state.value.copy(selectedPlan = plan)
    }

    fun purchaseSelectedPlan() {
        val plan = _state.value.selectedPlan ?: return
        scope.launch {
            _state.value = _state.value.copy(isPurchasing = true, errorMessage = null, successMessage = null)
            val result = repository.purchasePlan(plan)
            result.fold(
                onSuccess = { hasPremium ->
                    _state.value = _state.value.copy(
                        isPurchasing = false,
                        isPremium = hasPremium,
                        successMessage = if (hasPremium) "PURCHASE_SUCCESS" else null
                    )
                },
                onFailure = { error ->
                    val msg = if (error.message == "cancelled") "CANCELLED" else "PURCHASE_FAILED"
                    _state.value = _state.value.copy(
                        isPurchasing = false,
                        errorMessage = msg
                    )
                }
            )
        }
    }

    fun restorePurchases() {
        scope.launch {
            _state.value = _state.value.copy(isRestoring = true, errorMessage = null, successMessage = null)
            val result = repository.restorePurchases()
            result.fold(
                onSuccess = { hasPremium ->
                    _state.value = _state.value.copy(
                        isRestoring = false,
                        isPremium = hasPremium,
                        successMessage = if (hasPremium) "RESTORE_SUCCESS" else "RESTORE_FAILED"
                    )
                },
                onFailure = { error ->
                    _state.value = _state.value.copy(
                        isRestoring = false,
                        errorMessage = "RESTORE_FAILED"
                    )
                }
            )
        }
    }

    fun clearMessages() {
        _state.value = _state.value.copy(successMessage = null, errorMessage = null)
    }
}
