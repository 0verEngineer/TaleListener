package org.overengineer.talelistener.ui.screen


import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.dokar.sonner.ToastType
import com.dokar.sonner.Toaster
import com.dokar.sonner.rememberToasterState
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import org.overengineer.talelistener.domain.error.LoginError
import org.overengineer.talelistener.ui.screen.library.LibraryScreen
import org.overengineer.talelistener.ui.screen.settings.SettingsScreen
import org.overengineer.talelistener.ui.viewmodel.LoginViewModel
import org.overengineer.talelistener.ui.viewmodel.LoginViewModel.LoginState
import talelistener.composeapp.generated.resources.Res
import talelistener.composeapp.generated.resources.connectionError
import talelistener.composeapp.generated.resources.credentialsInvalid
import talelistener.composeapp.generated.resources.hostDown
import talelistener.composeapp.generated.resources.hostInvalid
import talelistener.composeapp.generated.resources.hostUrlMissing
import talelistener.composeapp.generated.resources.login
import talelistener.composeapp.generated.resources.password
import talelistener.composeapp.generated.resources.passwordMissing
import talelistener.composeapp.generated.resources.serverRequired
import talelistener.composeapp.generated.resources.serverUrl
import talelistener.composeapp.generated.resources.showPassword
import talelistener.composeapp.generated.resources.username
import talelistener.composeapp.generated.resources.usernameMissing

class LoginScreen: Screen {
    @Composable
    override fun Content() {
        val viewModel = koinInject<LoginViewModel>()
        val navigator = LocalNavigator.currentOrThrow

        val toaster = rememberToasterState()
        Toaster(state = toaster)

        val loginState by viewModel.loginState.collectAsState()
        val loginError by viewModel.loginError.collectAsState()

        val host by viewModel.host.collectAsState("")
        val username by viewModel.username.collectAsState("")
        val password by viewModel.password.collectAsState("")

        var showPassword by remember { mutableStateOf(false) }

        val hostDown = stringResource(Res.string.hostDown)
        val hostUrlMissing = stringResource(Res.string.hostUrlMissing)
        val passwordMissing = stringResource(Res.string.passwordMissing)
        val usernameMissing = stringResource(Res.string.usernameMissing)
        val invalidCredentials = stringResource(Res.string.credentialsInvalid)
        val invalidHostUrl = stringResource(Res.string.hostInvalid)
        val connectionError = stringResource(Res.string.connectionError)

        LaunchedEffect(loginState) {

            if (loginState is LoginState.Loading) {
                return@LaunchedEffect
            }

            when (loginState) {
                is LoginState.Success -> {
                    toaster.dismissAll()
                    navigator.push(LibraryScreen())
                }
                is LoginState.Error -> loginError?.let {
                    val message = when (it) {
                        LoginError.InternalError -> hostDown
                        LoginError.MissingCredentialsHost -> hostUrlMissing
                        LoginError.MissingCredentialsPassword -> passwordMissing
                        LoginError.MissingCredentialsUsername -> usernameMissing
                        LoginError.Unauthorized -> invalidCredentials
                        LoginError.InvalidCredentialsHost -> invalidHostUrl
                        LoginError.NetworkError -> connectionError
                    }

                    toaster.show(
                        message = message,
                        type = ToastType.Error
                    )
                }
                else -> {}
            }

            viewModel.readyToLogin()
        }

        Scaffold(
            modifier = Modifier
                .systemBarsPadding()
                .fillMaxSize(),

            content = { innerPadding ->
                Box(
                    modifier = Modifier
                        .padding(innerPadding)
                        .fillMaxSize(),
                ) {
                    Column(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .fillMaxWidth(0.8f),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(
                            text = stringResource(Res.string.login),
                            style = TextStyle(
                                fontSize = 24.sp,
                                fontWeight = FontWeight.SemiBold,
                                letterSpacing = 2.sp,
                                textAlign = TextAlign.Center,
                            ),
                            modifier = Modifier
                                .padding(vertical = 32.dp),
                        )

                        OutlinedTextField(
                            value = host,
                            onValueChange = { viewModel.setHost(it.trim()) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri),
                            label = { Text(stringResource(Res.string.serverUrl)) },
                            shape = RoundedCornerShape(16.dp),
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        )

                        OutlinedTextField(
                            value = username,
                            onValueChange = { viewModel.setUsername(it.trim()) },
                            label = { Text(stringResource(Res.string.username)) },
                            shape = RoundedCornerShape(16.dp),
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp)
                        )

                        OutlinedTextField(
                            value = password,
                            visualTransformation = if (!showPassword) PasswordVisualTransformation() else VisualTransformation.None,
                            onValueChange = { viewModel.setPassword(it) },
                            trailingIcon = {
                                IconButton(
                                    onClick = { showPassword = !showPassword },
                                ) {
                                    Icon(
                                        imageVector = if (showPassword) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                                        contentDescription = stringResource(Res.string.showPassword),
                                    )
                                }
                            },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            label = { Text(stringResource(Res.string.password)) },
                            shape = RoundedCornerShape(16.dp),
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        )

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 32.dp),
                        ) {
                            Button(
                                onClick = { viewModel.login() },
                                modifier = Modifier
                                    .weight(1f),
                                shape = RoundedCornerShape(
                                    topStart = 16.dp,
                                    bottomStart = 16.dp,
                                    topEnd = 0.dp,
                                    bottomEnd = 0.dp,
                                )
                            ) {
                                Spacer(modifier = Modifier.width(28.dp))
                                Box(
                                    modifier = Modifier.fillMaxWidth(),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Text(
                                        text = stringResource(Res.string.login),
                                        fontSize = 16.sp,
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(1.dp))

                            Button(
                                onClick = {
                                    navigator.push(SettingsScreen())
                                },
                                modifier = Modifier.width(56.dp),
                                shape = RoundedCornerShape(
                                    topStart = 0.dp,
                                    bottomStart = 0.dp,
                                    topEnd = 16.dp,
                                    bottomEnd = 16.dp,
                                ),
                                contentPadding = PaddingValues(0.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Settings,
                                    contentDescription = "Settings",
                                    modifier = Modifier.size(24.dp),
                                )
                            }
                        }

                        CircularProgressIndicator(
                            color = MaterialTheme.colorScheme.primary,
                            strokeWidth = 4.dp,
                            modifier = Modifier
                                .padding(vertical = 16.dp)
                                .alpha(if (loginState !is LoginState.Idle) 1f else 0f),
                        )
                    }

                    Text(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .alpha(0.5f)
                            .padding(bottom = 32.dp),
                        text = stringResource(Res.string.serverRequired),
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Normal,
                            color = MaterialTheme.colorScheme.onBackground,
                            letterSpacing = 0.5.sp,
                            lineHeight = 32.sp
                        ),
                        textAlign = TextAlign.Center,
                    )
                }
            }
        )
    }
}
