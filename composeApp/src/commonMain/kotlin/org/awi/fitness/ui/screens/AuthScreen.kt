package org.awi.fitness.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import compose.icons.TablerIcons
import compose.icons.tablericons.*
import kotlinx.coroutines.launch
import org.awi.fitness.data.StringKey
import org.awi.fitness.ui.components.FitnessButton
import org.awi.fitness.ui.components.FitnessCard
import org.awi.fitness.ui.components.FitnessTextField
import org.awi.fitness.viewmodel.AuthState
import org.awi.fitness.viewmodel.AuthViewModel
import org.awi.fitness.viewmodel.LanguageViewModel

class AuthScreen(
    private val viewModel: AuthViewModel,
    private val languageViewModel: LanguageViewModel
) : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val state by viewModel.state.collectAsState()
        val email by viewModel.email.collectAsState()
        val password by viewModel.password.collectAsState()
        val scope = rememberCoroutineScope()
        var isLoading by remember { mutableStateOf(false) }
        var errorMessage by remember { mutableStateOf<String?>(null) }
        var isSignUp by remember { mutableStateOf(false) }

        LaunchedEffect(state) {
            when (state) {
                is AuthState.Success -> {
                    navigator.replace(MainScreen())
                }
                is AuthState.Error -> {
                    errorMessage = (state as AuthState.Error).message
                    isLoading = false
                }
                is AuthState.Loading -> {
                    isLoading = true
                }
                else -> {
                    isLoading = false
                }
            }
        }

        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .imePadding() // Makes content keyboard aware
                    .systemBarsPadding() // Handles system bars on iOS
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 24.dp)
                        .padding(top = 48.dp, bottom = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // App Logo and Title Section - Fixed at the top
                    Column(
                        modifier = Modifier.padding(bottom = 32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = TablerIcons.Activity,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        
                        Text(
                            text = languageViewModel.getString(StringKey.APP_NAME),
                            style = MaterialTheme.typography.headlineMedium,
                            color = MaterialTheme.colorScheme.onBackground
                        )

                        Text(
                            text = if (isSignUp) 
                                languageViewModel.getString(StringKey.CREATE_ACCOUNT)
                            else 
                                languageViewModel.getString(StringKey.SIGN_IN_CONTINUE),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    // Error Message
                    AnimatedVisibility(
                        visible = errorMessage != null,
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically()
                    ) {
                        errorMessage?.let { error ->
                            Text(
                                text = error,
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(bottom = 16.dp)
                            )
                        }
                    }

                    // Login Form
                    Column(
                        modifier = Modifier
                            .fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        FitnessTextField(
                            value = email,
                            onValueChange = { 
                                viewModel.updateEmail(it)
                                errorMessage = null
                            },
                            languageViewModel = languageViewModel,
                            label = languageViewModel.getString(StringKey.EMAIL),
                            leadingIcon = TablerIcons.Mail,
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                            isError = errorMessage?.contains("email", ignoreCase = true) == true,
                            errorMessage = if (errorMessage?.contains("email", ignoreCase = true) == true) errorMessage else null
                        )

                        FitnessTextField(
                            value = password,
                            onValueChange = viewModel::updatePassword,
                            label = languageViewModel.getString(StringKey.PASSWORD),
                            leadingIcon = TablerIcons.Lock,
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                            languageViewModel = languageViewModel,
                            isError = errorMessage?.contains("password", ignoreCase = true) == true,
                            errorMessage = if (errorMessage?.contains("password", ignoreCase = true) == true) errorMessage else null,
                            isPassword = true
                        )

                        FitnessButton(
                            onClick = {
                                scope.launch {
                                    if (isSignUp) {
                                        viewModel.signUp()
                                    } else {
                                        viewModel.signIn()
                                    }
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp),
                            loading = isLoading
                        ) {
                            Text(
                                text = if (isSignUp)
                                    languageViewModel.getString(StringKey.SIGN_UP)
                                else
                                    languageViewModel.getString(StringKey.SIGN_IN)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Sign In/Sign Up Toggle Section
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isSignUp)
                                languageViewModel.getString(StringKey.ALREADY_HAVE_ACCOUNT)
                            else
                                languageViewModel.getString(StringKey.DONT_HAVE_ACCOUNT),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        
                        TextButton(
                            onClick = { 
                                isSignUp = !isSignUp
                                errorMessage = null
                            },
                            modifier = Modifier.padding(start = 4.dp)
                        ) {
                            Text(
                                text = if (isSignUp)
                                    languageViewModel.getString(StringKey.SIGN_IN)
                                else
                                    languageViewModel.getString(StringKey.SIGN_UP),
                                color = MaterialTheme.colorScheme.primary,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }
        }
    }
} 