
/*
 * This file is part of TaleListener, licensed under the GPLv3 license.
 * Original code by GrakovNe from the lissen project (https://github.com/GrakovNe/lissen-android), licensed under the MIT License.
 * Original file location: org.grakovne.lissen.channel.audiobookshelf.common
 * Modifications:
 * - Updated package statement and adjusted imports.
 * - Updated to work with TaleListeners code
 */

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
import androidx.compose.material.Button
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.aakira.napier.Napier
import org.overengineer.talelistener.ui.viewmodel.LoginViewModel
import org.overengineer.talelistener.ui.viewmodel.LoginViewModel.LoginState
import withMinimumTime

@Composable
fun LoginScreen(viewModel: LoginViewModel) {
    val loginState by viewModel.loginState.collectAsState()
    val loginError by viewModel.loginError.collectAsState()

    val host by viewModel.host.collectAsState("")
    val username by viewModel.username.collectAsState("")
    val password by viewModel.password.collectAsState("")

    var showPassword by remember { mutableStateOf(false) }

    LaunchedEffect(loginState) {
        if (loginState is LoginState.Loading) {
            return@LaunchedEffect
        }

        withMinimumTime(500) {
            Napier.d("Tried to log in with result $loginState and possible error is $loginError")
        }

        when (loginState) {
            is LoginState.Success -> {
                // todo
                //navController.showLibrary(clearHistory = true)
            }
            is LoginState.Error -> loginError?.let {
                // todo
                //Toast.makeText(context, it.makeText(context), LENGTH_SHORT).show()
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
                        text = "Login",
                        // todo
                        //text = stringResource(R.string.login_screen_title),
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
                        label = { Text("Server URL") },
                        // todo
                        //label = { Text(stringResource(R.string.login_screen_server_url_input)) },
                        shape = RoundedCornerShape(16.dp),
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .testTag("hostInput"),
                    )

                    OutlinedTextField(
                        value = username,
                        onValueChange = { viewModel.setUsername(it.trim()) },
                        label = { Text("Username") },
                        // todo
                        //label = { Text(stringResource(R.string.login_screen_login_input)) },
                        shape = RoundedCornerShape(16.dp),
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp)
                            .testTag("usernameInput"),
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
                                    imageVector = if (showPassword) Icons.Filled.Settings else Icons.Filled.Settings,
                                    // todo icons
                                    //imageVector = if (showPassword) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                                    contentDescription = "Show password",
                                    // todo
                                    //contentDescription = stringResource(R.string.login_screen_show_password_hint),
                                )
                            }
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        label = { Text("Password") },
                        // todo
                        //label = { Text(stringResource(R.string.login_screen_password_input)) },
                        shape = RoundedCornerShape(16.dp),
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .testTag("passwordInput"),
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 32.dp),
                    ) {
                        Button(
                            onClick = { viewModel.login() },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("loginButton"),
                            shape = RoundedCornerShape(
                                topStart = 16.dp,
                                bottomStart = 16.dp,
                                topEnd = 0.dp,
                                bottomEnd = 0.dp,
                            ),
                        ) {
                            Spacer(modifier = Modifier.width(28.dp))
                            Box(
                                modifier = Modifier.fillMaxWidth(),
                                contentAlignment = Alignment.Center,
                            ) {
                                Text(
                                    text = "Connect",
                                    // todo
                                    //text = stringResource(R.string.login_screen_connect_button_text),
                                    fontSize = 16.sp,
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(1.dp))

                        Button(
                            onClick = {
                                // todo
                                //navController.showSettings()
                            },
                            modifier = Modifier.width(56.dp),
                            shape = RoundedCornerShape(
                                topStart = 0.dp,
                                bottomStart = 0.dp,
                                topEnd = 16.dp,
                                bottomEnd = 16.dp,
                            ),
                            contentPadding = PaddingValues(0.dp),
                        ) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = "Settings",
                                modifier = Modifier.size(24.dp),
                            )
                        }
                    }

                    CircularProgressIndicator(
                        // todo
                        color = Color.Blue,
                        //color = FoxOrange,
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
                    text = "Server is required",
                    // todo
                    //text = stringResource(R.string.audiobookshelf_server_is_required),
                    // todo
                    /*style = typography.bodySmall.copy(
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Normal,
                        color = colorScheme.onBackground,
                        letterSpacing = 0.5.sp,
                        lineHeight = 32.sp,
                    ),*/
                    textAlign = TextAlign.Center,
                )
            }
        },
    )
}
