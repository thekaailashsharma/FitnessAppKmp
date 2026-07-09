package org.awi.fitness.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import compose.icons.TablerIcons
import compose.icons.tablericons.*
import kotlinx.coroutines.launch
import org.awi.fitness.data.StringKey
import org.awi.fitness.data.UserSettings
import org.awi.fitness.navigation.LocalAppNavigation
import org.awi.fitness.navigation.RootRoute
import org.awi.fitness.repository.SubscriptionRepository
import org.awi.fitness.theme.GreenAccent
import org.awi.fitness.ui.components.FitnessTextField
import org.awi.fitness.ui.components.GoldButton
import org.awi.fitness.ui.components.TajlyLogoMark
import org.awi.fitness.viewmodel.AuthState
import org.awi.fitness.viewmodel.AuthViewModel
import org.awi.fitness.viewmodel.LanguageViewModel

class AuthScreen(
    private val viewModel: AuthViewModel,
    private val languageViewModel: LanguageViewModel
) : Screen {

    @Composable
    override fun Content() {
        val appNavigation = LocalAppNavigation.current
        val state by viewModel.state.collectAsState()
        val email by viewModel.email.collectAsState()
        val password by viewModel.password.collectAsState()
        val scope = rememberCoroutineScope()

        var isSignUp by remember { mutableStateOf(false) }
        var isLoading by remember { mutableStateOf(false) }
        var errorMessage by remember { mutableStateOf<String?>(null) }
        var showForgotPassword by remember { mutableStateOf(false) }
        var resetEmailSent by remember { mutableStateOf(false) }

        // Route after successful auth based on subscription status
        LaunchedEffect(state) {
            when (state) {
                is AuthState.Success -> {
                    isLoading = true
                    val hasPremium = try {
                        SubscriptionRepository().checkPremiumAccess()
                    } catch (e: Exception) {
                        false
                    }
                    val settings = UserSettings.getInstance()
                    // A returning user SIGNING IN already has an account — don't drag them
                    // back through onboarding (intro animation + name form). Mark onboarding
                    // complete so they land on the app/paywall. Sign-UP here still onboards.
                    if (!isSignUp) settings.hasCompletedOnboarding = true
                    appNavigation.navigateTo(
                        when {
                            !hasPremium -> RootRoute.Paywall
                            !settings.hasCompletedOnboarding -> RootRoute.Onboarding
                            else -> RootRoute.Main
                        }
                    )
                }
                is AuthState.Error -> {
                    errorMessage = (state as AuthState.Error).message
                    isLoading = false
                }
                is AuthState.Loading -> {
                    isLoading = true
                    errorMessage = null
                }
                is AuthState.PasswordResetSent -> {
                    resetEmailSent = true
                    showForgotPassword = false
                    isLoading = false
                }
                else -> isLoading = false
            }
        }

        if (showForgotPassword) {
            ForgotPasswordSheet(
                languageViewModel = languageViewModel,
                initialEmail = email,
                onDismiss = { showForgotPassword = false },
                onSend = { resetEmail ->
                    scope.launch { viewModel.sendPasswordReset(resetEmail) }
                }
            )
        }

        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .imePadding()
                    .systemBarsPadding()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp)
                    .padding(top = 56.dp, bottom = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Logo
                TajlyLogoMark(size = 72.dp, animated = true)
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = languageViewModel.getString(StringKey.APP_NAME),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = if (isSignUp) languageViewModel.getString(StringKey.CREATE_ACCOUNT) else languageViewModel.getString(StringKey.WELCOME_BACK),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(40.dp))

                // Password reset success banner
                AnimatedVisibility(
                    visible = resetEmailSent,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    Surface(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                        shape = RoundedCornerShape(12.dp),
                        color = GreenAccent.copy(alpha = 0.12f)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                TablerIcons.CircleCheck,
                                contentDescription = null,
                                tint = GreenAccent,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                languageViewModel.getString(StringKey.AUTH_RESET_SENT),
                                style = MaterialTheme.typography.bodySmall,
                                color = GreenAccent
                            )
                        }
                    }
                }

                // Error banner
                AnimatedVisibility(
                    visible = errorMessage != null,
                    enter = fadeIn() + expandVertically(tween(200)),
                    exit = fadeOut() + shrinkVertically(tween(200))
                ) {
                    errorMessage?.let { err ->
                        Surface(
                            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.7f)
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.Top,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    TablerIcons.AlertCircle,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(18.dp).padding(top = 1.dp)
                                )
                                Text(
                                    text = err,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onErrorContainer
                                )
                            }
                        }
                    }
                }

                // Form fields
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    FitnessTextField(
                        value = email,
                        onValueChange = {
                            viewModel.updateEmail(it)
                            errorMessage = null
                            resetEmailSent = false
                        },
                        label = languageViewModel.getString(StringKey.EMAIL),
                        languageViewModel = languageViewModel,
                        leadingIcon = TablerIcons.Mail,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Email,
                            imeAction = ImeAction.Next
                        ),
                        isError = errorMessage?.contains("email", ignoreCase = true) == true
                    )

                    FitnessTextField(
                        value = password,
                        onValueChange = {
                            viewModel.updatePassword(it)
                            errorMessage = null
                        },
                        label = languageViewModel.getString(StringKey.PASSWORD),
                        languageViewModel = languageViewModel,
                        leadingIcon = TablerIcons.Lock,
                        isPassword = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Done
                        ),
                        isError = errorMessage?.contains("password", ignoreCase = true) == true ||
                                  errorMessage?.contains("incorrect", ignoreCase = true) == true
                    )

                    // Forgot password — only shown on sign-in
                    AnimatedVisibility(visible = !isSignUp) {
                        Box(modifier = Modifier.fillMaxWidth()) {
                            Text(
                                text = languageViewModel.getString(StringKey.FORGOT_PASSWORD),
                                style = MaterialTheme.typography.bodySmall,
                                color = GreenAccent,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier
                                    .align(Alignment.CenterEnd)
                                    .clickable { showForgotPassword = true }
                                    .padding(vertical = 4.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Primary action button
                GoldButton(
                    text = if (isSignUp) languageViewModel.getString(StringKey.CREATE_ACCOUNT) else languageViewModel.getString(StringKey.SIGN_IN),
                    onClick = {
                        scope.launch {
                            if (isSignUp) viewModel.signUp() else viewModel.signIn()
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    loading = isLoading
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Toggle sign in / sign up
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isSignUp) languageViewModel.getString(StringKey.ALREADY_HAVE_ACCOUNT) else languageViewModel.getString(StringKey.DONT_HAVE_ACCOUNT),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    TextButton(
                        onClick = {
                            isSignUp = !isSignUp
                            errorMessage = null
                            viewModel.resetToInitial()
                        },
                        modifier = Modifier.padding(start = 2.dp)
                    ) {
                        Text(
                            text = if (isSignUp) languageViewModel.getString(StringKey.SIGN_IN) else languageViewModel.getString(StringKey.SIGN_UP),
                            color = GreenAccent,
                            fontWeight = FontWeight.SemiBold,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Subscription reassurance
                Text(
                    text = languageViewModel.getString(StringKey.AUTH_TRIAL_LINE),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ForgotPasswordSheet(
    languageViewModel: LanguageViewModel,
    initialEmail: String,
    onDismiss: () -> Unit,
    onSend: (String) -> Unit
) {
    var resetEmail by remember { mutableStateOf(initialEmail) }
    var isSending by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 40.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    languageViewModel.getString(StringKey.FORGOT_PASSWORD_TITLE),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = onDismiss) {
                    Icon(TablerIcons.X, contentDescription = "Close")
                }
            }

            Text(
                languageViewModel.getString(StringKey.FORGOT_PASSWORD_DESC),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            OutlinedTextField(
                value = resetEmail,
                onValueChange = { resetEmail = it },
                label = { Text(languageViewModel.getString(StringKey.EMAIL)) },
                leadingIcon = { Icon(TablerIcons.Mail, contentDescription = null) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Done
                )
            )

            Button(
                onClick = {
                    isSending = true
                    onSend(resetEmail)
                },
                enabled = resetEmail.contains("@") && !isSending,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                colors = ButtonDefaults.buttonColors(containerColor = GreenAccent),
                shape = RoundedCornerShape(14.dp)
            ) {
                if (isSending) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = Color.White
                    )
                } else {
                    Text(languageViewModel.getString(StringKey.SEND_RESET_LINK), fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}
