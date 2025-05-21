package org.awi.fitness.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import compose.icons.TablerIcons
import compose.icons.tablericons.Activity
import compose.icons.tablericons.Lock
import compose.icons.tablericons.Mail
import compose.icons.tablericons.UserOff
import compose.icons.tablericons.UserPlus
import kotlinx.coroutines.launch
import org.awi.fitness.data.StringKey
import org.awi.fitness.ui.components.FitnessButton
import org.awi.fitness.ui.components.FitnessCard
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
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Logo and Title with Animation
                Icon(
                    imageVector = TablerIcons.Activity,
                    contentDescription = null,
                    modifier = Modifier.size(80.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Text(
                    text = languageViewModel.getString(StringKey.APP_NAME),
                    style = MaterialTheme.typography.displayMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                Text(
                    text = if (isSignUp) 
                        languageViewModel.getString(StringKey.CREATE_ACCOUNT)
                    else 
                        languageViewModel.getString(StringKey.SIGN_IN_CONTINUE),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Spacer(modifier = Modifier.height(48.dp))

                // Error message with animation
                AnimatedVisibility(
                    visible = errorMessage != null,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    errorMessage?.let { error ->
                        Text(
                            text = error,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.padding(bottom = 24.dp)
                        )
                    }
                }

                // Login Form
                FitnessCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        verticalArrangement = Arrangement.spacedBy(24.dp)
                    ) {
                        OutlinedTextField(
                            value = email,
                            onValueChange = viewModel::updateEmail,
                            label = { Text(languageViewModel.getString(StringKey.EMAIL)) },
                            leadingIcon = { 
                                Icon(
                                    TablerIcons.Mail,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(64.dp),
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                            singleLine = true,
                            shape = MaterialTheme.shapes.medium,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                                focusedLeadingIconColor = MaterialTheme.colorScheme.primary,
                                unfocusedLeadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )

                        OutlinedTextField(
                            value = password,
                            onValueChange = viewModel::updatePassword,
                            label = { Text(languageViewModel.getString(StringKey.PASSWORD)) },
                            leadingIcon = { 
                                Icon(
                                    TablerIcons.Lock,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            },
                            visualTransformation = PasswordVisualTransformation(),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(64.dp),
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                            singleLine = true,
                            shape = MaterialTheme.shapes.medium,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                                focusedLeadingIconColor = MaterialTheme.colorScheme.primary,
                                unfocusedLeadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant
                            )
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
                                .height(56.dp),
                            loading = isLoading
                        ) {
                            Icon(if (isSignUp) TablerIcons.UserPlus else TablerIcons.UserOff, null)
                            Spacer(Modifier.width(12.dp))
                            Text(
                                if (isSignUp) 
                                    languageViewModel.getString(StringKey.SIGN_UP)
                                else 
                                    languageViewModel.getString(StringKey.SIGN_IN),
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Toggle between Sign In and Sign Up
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isSignUp) 
                            languageViewModel.getString(StringKey.ALREADY_HAVE_ACCOUNT)
                        else 
                            languageViewModel.getString(StringKey.DONT_HAVE_ACCOUNT),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    TextButton(
                        onClick = { isSignUp = !isSignUp }
                    ) {
                        Text(
                            text = if (isSignUp) 
                                languageViewModel.getString(StringKey.SIGN_IN)
                            else 
                                languageViewModel.getString(StringKey.SIGN_UP),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
} 