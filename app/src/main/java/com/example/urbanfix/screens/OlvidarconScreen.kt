package com.example.urbanfix.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.urbanfix.R
import com.example.urbanfix.ui.theme.BlueSoft
import com.example.urbanfix.ui.theme.WhiteFull
import com.example.urbanfix.viewmodel.ForgotPasswordState
import com.example.urbanfix.viewmodel.OlvidarconViewModel

val LightGreenCard = Color(0xFFE0F2E9)
val DialogErrorRed = Color(0xFFE63946)
val DialogSuccessGreen = Color(0xFF90BE6D)

@Composable
fun OlvidarconScreen(navController: NavHostController) {
    val viewModel: OlvidarconViewModel = viewModel()
    val state by viewModel.state.collectAsState()
    val email by viewModel.email.collectAsState()
    val code by viewModel.verificationCode.collectAsState()

    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.registro_normal),
            contentDescription = "Fondo de ciudad",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        // Contenido principal que cambia según el estado
        when (state) {
            is ForgotPasswordState.EnterEmail, is ForgotPasswordState.Loading, is ForgotPasswordState.EmailSentSuccess, is ForgotPasswordState.Error -> {
                EmailEntryContent(
                    navController = navController,
                    email = email,
                    onEmailChange = viewModel::onEmailChange,
                    onSendCodeClick = viewModel::sendRecoveryCode,
                    isLoading = state is ForgotPasswordState.Loading
                )
            }
            is ForgotPasswordState.EnterCode -> {
                CodeEntryContent(
                    navController = navController,
                    code = code,
                    onCodeChange = viewModel::onCodeChange,
                    onAccessClick = viewModel::verifyCode,
                    isLoading = state is ForgotPasswordState.Loading
                )
            }
        }

        // Muestra los diálogos sobre el contenido
        when (val currentState = state) {
            is ForgotPasswordState.EmailSentSuccess -> {
                MessageDialog(
                    title = "Envío exitoso",
                    message = "Código enviado, revisa tu e-mail",
                    buttonText = "Siguiente",
                    headerColor = DialogSuccessGreen,
                    onDismiss = { viewModel.dismissDialog() }
                )
            }
            is ForgotPasswordState.Error -> {
                MessageDialog(
                    title = "Error",
                    message = currentState.message,
                    buttonText = "Volver",
                    headerColor = DialogErrorRed,
                    onDismiss = { viewModel.dismissDialog() }
                )
            }
            else -> {}
        }
    }
}

// Vista para pedir el E-mail
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EmailEntryContent(
    navController: NavHostController,
    email: String,
    onEmailChange: (String) -> Unit,
    onSendCodeClick: () -> Unit,
    isLoading: Boolean
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(contentAlignment = Alignment.TopCenter) {
            Card(
                shape = RoundedCornerShape(40.dp),
                colors = CardDefaults.cardColors(containerColor = LightGreenCard),
                modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp).padding(top = 60.dp)
            ) {
                Column(
                    modifier = Modifier.padding(top = 80.dp, start = 24.dp, end = 24.dp, bottom = 32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(text = "Recupera tu contraseña", fontSize = 28.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, lineHeight = 32.sp)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(text = "Escribe tu correo para enviarte un código que te dé acceso a la app.", fontSize = 14.sp, textAlign = TextAlign.Center, color = Color.Gray)
                    Spacer(modifier = Modifier.height(32.dp))
                    Text(text = "E-mail", fontSize = 14.sp, fontWeight = FontWeight.Medium, modifier = Modifier.fillMaxWidth())
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = email,
                        onValueChange = onEmailChange,
                        placeholder = { Text("Ingresa tu e-mail", color = Color.Gray) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = Color.Gray,
                            focusedBorderColor = BlueSoft,
                            unfocusedContainerColor = WhiteFull,
                            focusedContainerColor = WhiteFull
                        )
                    )
                    Spacer(modifier = Modifier.height(32.dp))
                    Button(
                        onClick = onSendCodeClick,
                        shape = RoundedCornerShape(50),
                        colors = ButtonDefaults.buttonColors(containerColor = BlueSoft),
                        modifier = Modifier.fillMaxWidth().height(50.dp)
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp), color = WhiteFull, strokeWidth = 2.dp)
                        } else {
                            Text("Enviar código", color = WhiteFull, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
            Box(
                modifier = Modifier.size(120.dp).clip(CircleShape).background(WhiteFull).shadow(elevation = 8.dp, shape = CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Image(painter = painterResource(id = R.drawable.circular_logo), contentDescription = "Logo UrbanFix", modifier = Modifier.size(110.dp))
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
        IconButton(
            onClick = { navController.popBackStack() },
            modifier = Modifier.size(50.dp).clip(CircleShape).background(LightGreenCard)
        ) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver", tint = Color.Black)
        }
    }
}

// Vista para pedir el Código
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CodeEntryContent(
    navController: NavHostController,
    code: String,
    onCodeChange: (String) -> Unit,
    onAccessClick: () -> Unit,
    isLoading: Boolean
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(contentAlignment = Alignment.TopCenter) {
            Card(
                shape = RoundedCornerShape(40.dp),
                colors = CardDefaults.cardColors(containerColor = LightGreenCard),
                modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp).padding(top = 60.dp)
            ) {
                Column(
                    modifier = Modifier.padding(top = 80.dp, start = 24.dp, end = 24.dp, bottom = 32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(text = "Recupera tu contraseña", fontSize = 28.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, lineHeight = 32.sp)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(text = "Escribe el código que se envió al correo que proporcionaste.", fontSize = 14.sp, textAlign = TextAlign.Center, color = Color.Gray)
                    Spacer(modifier = Modifier.height(32.dp))
                    Text(text = "Código de acceso", fontSize = 14.sp, fontWeight = FontWeight.Medium, modifier = Modifier.fillMaxWidth())
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = code,
                        onValueChange = onCodeChange,
                        placeholder = { Text("Escribe el código", color = Color.Gray) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = Color.Gray,
                            focusedBorderColor = BlueSoft,
                            unfocusedContainerColor = WhiteFull,
                            focusedContainerColor = WhiteFull
                        )
                    )
                    Spacer(modifier = Modifier.height(32.dp))
                    Button(
                        onClick = onAccessClick,
                        shape = RoundedCornerShape(50),
                        colors = ButtonDefaults.buttonColors(containerColor = BlueSoft),
                        modifier = Modifier.fillMaxWidth().height(50.dp)
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp), color = WhiteFull, strokeWidth = 2.dp)
                        } else {
                            Text("Acceder", color = WhiteFull, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
            Box(
                modifier = Modifier.size(120.dp).clip(CircleShape).background(WhiteFull).shadow(elevation = 8.dp, shape = CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Image(painter = painterResource(id = R.drawable.circular_logo), contentDescription = "Logo UrbanFix", modifier = Modifier.size(110.dp))
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
        IconButton(
            onClick = { navController.popBackStack() },
            modifier = Modifier.size(50.dp).clip(CircleShape).background(LightGreenCard)
        ) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver", tint = Color.Black)
        }
    }
}


// Diálogo genérico para mostrar mensajes de éxito o error
@Composable
fun MessageDialog(
    title: String,
    message: String,
    buttonText: String,
    headerColor: Color,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = WhiteFull)) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier.fillMaxWidth().background(headerColor).padding(vertical = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = title, color = WhiteFull, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                }
                Text(
                    text = message,
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 24.dp),
                    textAlign = TextAlign.Center,
                    fontSize = 16.sp
                )
                Button(
                    onClick = onDismiss,
                    shape = RoundedCornerShape(50),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1D3557)),
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 48.dp).padding(bottom = 24.dp).height(48.dp)
                ) {
                    Text(text = buttonText, color = WhiteFull, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}