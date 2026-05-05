package org.awi.fitness.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import compose.icons.TablerIcons
import compose.icons.tablericons.*
import kotlinx.coroutines.launch
import com.russhwolf.settings.set
import org.awi.fitness.data.Language
import org.awi.fitness.data.StringKey
import org.awi.fitness.data.UserSettings
import org.awi.fitness.getDeviceLanguageCode
import org.awi.fitness.ui.components.FitnessButton
import org.awi.fitness.ui.components.FitnessTextField
import org.awi.fitness.viewmodel.AuthState
import org.awi.fitness.viewmodel.AuthViewModel
import org.awi.fitness.viewmodel.LanguageViewModel

class AuthScreen(
    private val viewModel: AuthViewModel,
    private val languageViewModel: LanguageViewModel,
    private val prefillEmail: String? = null
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
        var showForgotPasswordDialog by remember { mutableStateOf(false) }
        var showLanguageBanner by remember { mutableStateOf(false) }
        val userSettings = remember { UserSettings.getInstance() }

        LaunchedEffect(Unit) {
            prefillEmail?.let { viewModel.updateEmail(it) }

            if (!userSettings.settings.hasKey("language_auto_detected")) {
                val deviceLang = getDeviceLanguageCode()
                if (deviceLang == "nl") {
                    userSettings.setLanguage(Language.DUTCH.code)
                    languageViewModel.setLanguage(Language.DUTCH)
                }
                userSettings.settings["language_auto_detected"] = true
                showLanguageBanner = true
            }
        }

        LaunchedEffect(state) {
            when (state) {
                is AuthState.Success -> {
                    navigator.replace(MainScreen())
                }
                is AuthState.Error -> {
                    val err = state as AuthState.Error
                    errorMessage = if (err.code == "INCORRECT_PASSWORD") {
                        languageViewModel.getString(StringKey.INCORRECT_PASSWORD)
                    } else {
                        err.message
                    }
                    isLoading = false
                }
                is AuthState.Loading -> {
                    isLoading = true
                    errorMessage = null
                }
                is AuthState.ClientNotFound, is AuthState.AccessRequestSent -> {
                    isLoading = false
                }
                else -> {
                    isLoading = false
                }
            }
        }

        if (showForgotPasswordDialog) {
            ForgotPasswordDialog(
                languageViewModel = languageViewModel,
                initialEmail = email,
                onDismiss = { showForgotPasswordDialog = false },
                onSend = { resetEmail ->
                    scope.launch {
                        val success = viewModel.sendPasswordReset(resetEmail)
                        showForgotPasswordDialog = false
                        if (success) {
                            errorMessage = null
                        }
                    }
                }
            )
        }

        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .imePadding()
                    .systemBarsPadding()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 24.dp)
                        .padding(top = 48.dp, bottom = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Language banner
                    AnimatedVisibility(
                        visible = showLanguageBanner,
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically()
                    ) {
                        val currentLang = userSettings.language.collectAsState()
                        val langName = if (currentLang.value == "nl") "Nederlands" else "English"
                        Surface(
                            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = TablerIcons.World,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "${languageViewModel.getString(StringKey.LANGUAGE_SET_TO)} $langName",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onBackground,
                                    modifier = Modifier.weight(1f)
                                )
                                Text(
                                    text = languageViewModel.getString(StringKey.CHANGE),
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.clickable {
                                        val newLang = if (currentLang.value == "nl") Language.ENGLISH else Language.DUTCH
                                        userSettings.setLanguage(newLang.code)
                                        languageViewModel.setLanguage(newLang)
                                    }
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Icon(
                                    imageVector = TablerIcons.X,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp).clickable { showLanguageBanner = false },
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    // Language toggle (always visible)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        val currentLang = userSettings.language.collectAsState()
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.clickable {
                                val newLang = if (currentLang.value == "nl") Language.ENGLISH else Language.DUTCH
                                userSettings.setLanguage(newLang.code)
                                languageViewModel.setLanguage(newLang)
                            }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = TablerIcons.World,
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (currentLang.value == "nl") "NL" else "EN",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // App Logo and Title
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
                            text = languageViewModel.getString(StringKey.ENTER_CREDENTIALS),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    when (state) {
                        is AuthState.ClientNotFound -> {
                            // Request Access card
                            RequestAccessCard(
                                languageViewModel = languageViewModel,
                                onRequestAccess = { scope.launch { viewModel.requestAccess() } },
                                onBack = { viewModel.resetToInitial() }
                            )
                        }
                        is AuthState.AccessRequestSent -> {
                            // Confirmation card
                            AccessRequestSentCard(
                                languageViewModel = languageViewModel,
                                onBack = { viewModel.resetToInitial() }
                            )
                        }
                        else -> {
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
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                FitnessTextField(
                                    value = email,
                                    onValueChange = {
                                        viewModel.updateEmail(it)
                                        errorMessage = null
                                    },
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
                                    isError = errorMessage?.contains("password", ignoreCase = true) == true,
                                    errorMessage = if (errorMessage?.contains("password", ignoreCase = true) == true) errorMessage else null,
                                    isPassword = true
                                )

                                // Forgot password link
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End
                                ) {
                                    Text(
                                        text = languageViewModel.getString(StringKey.FORGOT_PASSWORD),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.clickable { showForgotPasswordDialog = true }
                                    )
                                }

                                FitnessButton(
                                    onClick = {
                                        scope.launch { viewModel.authenticate() }
                                    },
                                    modifier = Modifier.fillMaxWidth().height(52.dp),
                                    loading = isLoading
                                ) {
                                    Text(text = languageViewModel.getString(StringKey.CONTINUE))
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
    }
}

@Composable
private fun RequestAccessCard(
    languageViewModel: LanguageViewModel,
    onRequestAccess: () -> Unit,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Icon(
            imageVector = TablerIcons.UserPlus,
            contentDescription = null,
            modifier = Modifier.size(56.dp),
            tint = MaterialTheme.colorScheme.primary
        )

        Text(
            text = languageViewModel.getString(StringKey.REQUEST_ACCESS_TITLE),
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center
        )

        Text(
            text = languageViewModel.getString(StringKey.REQUEST_ACCESS_DESC),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        FitnessButton(
            onClick = onRequestAccess,
            modifier = Modifier.fillMaxWidth().height(52.dp)
        ) {
            Text(text = languageViewModel.getString(StringKey.REQUEST_ACCESS))
        }

        TextButton(onClick = onBack) {
            Text(
                text = languageViewModel.getString(StringKey.BACK),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun AccessRequestSentCard(
    languageViewModel: LanguageViewModel,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Icon(
            imageVector = TablerIcons.CircleCheck,
            contentDescription = null,
            modifier = Modifier.size(56.dp),
            tint = MaterialTheme.colorScheme.primary
        )

        Text(
            text = languageViewModel.getString(StringKey.REQUEST_SENT_TITLE),
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center
        )

        Text(
            text = languageViewModel.getString(StringKey.REQUEST_SENT_DESC),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        TextButton(onClick = onBack) {
            Text(
                text = languageViewModel.getString(StringKey.BACK),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ForgotPasswordDialog(
    languageViewModel: LanguageViewModel,
    initialEmail: String,
    onDismiss: () -> Unit,
    onSend: (String) -> Unit
) {
    var resetEmail by remember { mutableStateOf(initialEmail) }
    var sent by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (sent) "" else languageViewModel.getString(StringKey.FORGOT_PASSWORD_TITLE),
                style = MaterialTheme.typography.titleMedium
            )
        },
        text = {
            if (sent) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = TablerIcons.CircleCheck,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = languageViewModel.getString(StringKey.RESET_LINK_SENT),
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                Column {
                    Text(
                        text = languageViewModel.getString(StringKey.FORGOT_PASSWORD_DESC),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = resetEmail,
                        onValueChange = { resetEmail = it },
                        label = { Text(languageViewModel.getString(StringKey.EMAIL)) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        },
        confirmButton = {
            if (sent) {
                TextButton(onClick = onDismiss) {
                    Text("OK")
                }
            } else {
                TextButton(
                    onClick = {
                        onSend(resetEmail)
                        sent = true
                    },
                    enabled = resetEmail.contains("@")
                ) {
                    Text(languageViewModel.getString(StringKey.SEND_RESET_LINK))
                }
            }
        },
        dismissButton = {
            if (!sent) {
                TextButton(onClick = onDismiss) {
                    Text(languageViewModel.getString(StringKey.CANCEL))
                }
            }
        }
    )
}
