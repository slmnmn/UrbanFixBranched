package com.example.urbanfix.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.urbanfix.R
import com.example.urbanfix.data.UserPreferencesManager
import com.example.urbanfix.navigation.Pantallas
import com.example.urbanfix.viewmodel.LoginState
import com.example.urbanfix.viewmodel.LoginViewModel

@Composable
fun LoginScreen(
    navController: NavController,
    loginViewModel: LoginViewModel = viewModel()
) {
    val context = LocalContext.current
    val userPreferencesManager = remember { UserPreferencesManager(context) }

    val email by loginViewModel.email.collectAsState()
    val password by loginViewModel.password.collectAsState()
    val loginState by loginViewModel.loginState.collectAsState()

    var rememberMe by remember { mutableStateOf(false) }
    var passwordVisible by remember { mutableStateOf(false) }

    LaunchedEffect(loginState) {
        val currentState = loginState

        if (loginState is LoginState.Success) {

            userPreferencesManager.saveCredentials(
                email = email,
                password = password,
                rememberMe = rememberMe
            )

            val userData = (currentState as LoginState.Success).userData
            val userRole = (currentState as LoginState.Success).role

            userPreferencesManager.saveUserData(
                id = userData.id,
                name = userData.nombre,
                email = userData.email,
                phone = userData.telefono,
                role = userRole,
                companyName = userData.entidad_nombre
            )

            navController.navigate(Pantallas.Home.ruta) {
                popUpTo(navController.graph.startDestinationId) { inclusive = true }
            }
        }
    }

    LaunchedEffect(Unit) {
        val isRemembered = userPreferencesManager.getRememberMe()
        rememberMe = isRemembered
        if (isRemembered) {
            loginViewModel.onEmailChange(userPreferencesManager.getSavedEmail())
            loginViewModel.onPasswordChange(userPreferencesManager.getSavedPassword())
        }
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        Image(
            painter = painterResource(id = R.drawable.log_back),
            contentDescription = stringResource(R.string.login_background_description),
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        Column(
            modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(215.dp))

            Card(
                modifier = Modifier.fillMaxWidth().shadow(elevation = 20.dp, shape = RoundedCornerShape(32.dp), clip = false),
                shape = RoundedCornerShape(32.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFA8DADC))
            ) {
                Column(
                    modifier = Modifier.padding(22.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier.size(128.dp).background(Color.White, CircleShape).padding(2.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.circular_logo),
                            contentDescription = stringResource(R.string.logo_content_description),
                            modifier = Modifier.size(130.dp).clip(CircleShape)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = stringResource(R.string.login_credentials_prompt),
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = Color(0xFF1D3557),
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Text(
                        text = stringResource(R.string.email_label),
                        fontSize = 14.sp,
                        color = Color(0xFF658384),
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.fillMaxWidth().padding(start = 4.dp, bottom = 4.dp)
                    )

                    OutlinedTextField(
                        value = email,
                        onValueChange = { loginViewModel.onEmailChange(it) },
                        placeholder = { Text(stringResource(R.string.email_placeholder), fontSize = 13.sp, color = Color.Gray) },
                        modifier = Modifier.fillMaxWidth().height(52.dp).border(width = 1.5.dp, color = Color(0xFF1D3557).copy(alpha = 0.8f), shape = RoundedCornerShape(26.dp)),
                        shape = RoundedCornerShape(26.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        singleLine = true,
                        colors = TextFieldDefaults.colors(focusedContainerColor = Color(0xFFE8E8E8), unfocusedContainerColor = Color(0xFFE8E8E8), disabledContainerColor = Color(0xFFE8E8E8), focusedIndicatorColor = Color.Transparent, unfocusedIndicatorColor = Color.Transparent, disabledIndicatorColor = Color.Transparent)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = stringResource(R.string.password_label),
                        fontSize = 14.sp,
                        color = Color(0xFF658384),
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.fillMaxWidth().padding(start = 4.dp, bottom = 4.dp)
                    )

                    OutlinedTextField(
                        value = password,
                        onValueChange = { loginViewModel.onPasswordChange(it) },
                        placeholder = { Text(stringResource(R.string.password_placeholder), fontSize = 13.sp, color = Color.Gray) },
                        modifier = Modifier.fillMaxWidth().height(52.dp).border(width = 1.5.dp, color = Color(0xFF1D3557).copy(alpha = 0.8f), shape = RoundedCornerShape(26.dp)),
                        shape = RoundedCornerShape(26.dp),
                        singleLine = true,
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            val image = if (passwordVisible) R.drawable.watch else R.drawable.hide
                            Image(
                                painter = painterResource(id = image),
                                contentDescription = stringResource(R.string.toggle_password_visibility),
                                modifier = Modifier.size(20.dp).clickable { passwordVisible = !passwordVisible }
                            )
                        },
                        colors = TextFieldDefaults.colors(focusedContainerColor = Color(0xFFE8E8E8), unfocusedContainerColor = Color(0xFFE8E8E8), disabledContainerColor = Color(0xFFE8E8E8), focusedIndicatorColor = Color.Transparent, unfocusedIndicatorColor = Color.Transparent, disabledIndicatorColor = Color.Transparent)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.clickable { rememberMe = !rememberMe }
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(20.dp)
                                    .border(
                                        width = 2.dp,
                                        color = if (rememberMe) Color(0xFFE63946) else Color(0xFF1D3557),
                                        shape = CircleShape
                                    )
                                    .background(
                                        color = Color(0xFFE8E8E8),
                                        shape = CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                if (rememberMe) {
                                    Box(
                                        modifier = Modifier
                                            .size(10.dp)
                                            .background(Color(0xFFE63946), CircleShape)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            Text(
                                text = stringResource(R.string.remember_me),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1D3557)
                            )
                        }

                        Text(
                            text = stringResource(R.string.forgot_password),
                            color = Color(0xFFE63946),
                            fontSize = 13.sp,
                            textDecoration = TextDecoration.Underline,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.clickable {
                                navController.navigate(Pantallas.Olvido.ruta)
                            }
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Button(
                        onClick = {
                            println("LOGIN_DEBUG: Botón presionado. Llamando a viewModel.loginUser()")
                            loginViewModel.loginUser()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF1FAEE)),
                        shape = RoundedCornerShape(26.dp),
                        modifier = Modifier.fillMaxWidth().padding(top = 10.dp, bottom = 10.dp, start = 50.dp, end = 50.dp).height(52.dp).shadow(8.dp, RoundedCornerShape(26.dp)).border(width = 2.dp, color = Color(0xFF4AB7B6), shape = RoundedCornerShape(26.dp))
                    ) {
                        if (loginState is LoginState.Loading) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color(0xFF1D3557), strokeWidth = 2.dp)
                        } else {
                            Text(
                                text = stringResource(R.string.login_button),
                                color = Color(0xFF1D3557),
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = stringResource(R.string.no_account_question),
                            color = Color(0xFF1D3557),
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = stringResource(R.string.register_link),
                            color = Color(0xFFE63946),
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            textDecoration = TextDecoration.Underline,
                            modifier = Modifier.clickable {
                                navController.navigate(Pantallas.Registro.ruta)
                            }
                        )
                    }
                }
            }
        }

        if (loginState is LoginState.Error) {
            ErrorDialog(
                errorMessage = (loginState as LoginState.Error).message,
                onDismiss = { loginViewModel.dismissError() }
            )
        }
    }
}

@Composable
fun ErrorDialog(
    errorMessage: String,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 1.dp),
            shape = RoundedCornerShape(1.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            )
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .background(Color(0xFFFF4B3A)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(R.string.error_dialog_title),
                        color = Color.Black,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Text(
                    text = errorMessage,
                    fontSize = 15.sp,
                    fontFamily = FontFamily.SansSerif,
                    color = Color.Black,
                    fontStyle = FontStyle.Italic,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp, horizontal = 24.dp)
                )

                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF1D3557)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 48.dp)
                        .padding(bottom = 24.dp)
                        .height(48.dp)
                ) {
                    Text(
                        text = stringResource(R.string.back_button),
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}