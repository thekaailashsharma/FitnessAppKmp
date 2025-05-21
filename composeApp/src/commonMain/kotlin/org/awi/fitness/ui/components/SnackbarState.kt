package org.awi.fitness.ui.components

import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class SnackbarManager(
    private val snackbarHostState: SnackbarHostState,
    private val scope: CoroutineScope
) {
    fun showMessage(message: String, duration: SnackbarDuration = SnackbarDuration.Short) {
        scope.launch {
            snackbarHostState.showSnackbar(
                message = message,
                duration = duration
            )
        }
    }
}

@Composable
fun rememberSnackbarManager(scope: CoroutineScope): SnackbarManager {
    val snackbarHostState = remember { SnackbarHostState() }
    return remember(snackbarHostState, scope) {
        SnackbarManager(snackbarHostState, scope)
    }
} 