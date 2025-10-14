package com.example.urbanfix.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
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
import androidx.compose.ui.zIndex
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.urbanfix.R
import com.example.urbanfix.navigation.Pantallas
import com.example.urbanfix.viewmodel.RegistrationState
import com.example.urbanfix.viewmodel.RegistrationViewModel

@Composable
fun RegUsuarioScreen(
    navController: NavHostController,
    // Se obtiene la instancia del ViewModel
    registrationViewModel: RegistrationViewModel = viewModel()
) {
    // Estado de los campos
    val email by registrationViewModel.email.collectAsState()
    val nombres by registrationViewModel.nombres.collectAsState()
    val apellidos by registrationViewModel.apellidos.collectAsState()
    val password by registrationViewModel.password.collectAsState()
    val confirmPassword by registrationViewModel.confirmPassword.collectAsState()
    val registrationState by registrationViewModel.registrationState.collectAsState()

    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF043157))
    ) {
        // Imagen de fondo
        Image(
            painter = painterResource(id = R.drawable.registro_normal),
            contentDescription = stringResource(R.string.fondo),
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )


        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .align(Alignment.TopCenter)
                .offset(y = 100.dp)
                .zIndex(1f),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFFE0F5E1)
            ),
            border = BorderStroke(2.dp, Color.Black)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .padding(top = 30.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = stringResource(R.string.reg_strate_ahora),
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )

                Spacer(modifier = Modifier.height(16.dp))

                // E-mail
                Text(stringResource(R.string.e_mail), fontSize = 12.sp, color = Color(0xFF888888), modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 4.dp, bottom = 4.dp))
                OutlinedTextField(
                    value = email,
                    onValueChange = { registrationViewModel.onEmailChange(it) },
                    placeholder = { Text(stringResource(R.string.ingresa_tu_e_mail), fontSize = 13.sp, color = Color(0xFF888888)) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = OutlinedTextFieldDefaults.colors(unfocusedContainerColor = Color(0xFFD9D9D9), focusedContainerColor = Color(0xFFD9D9D9), unfocusedBorderColor = Color.Black, focusedBorderColor = Color(0xFF043157), unfocusedTextColor = Color(0xFF555555), focusedTextColor = Color(0xFF555555)),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    singleLine = true,
                    textStyle = LocalTextStyle.current.copy(fontSize = 13.sp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Nombres
                Text(stringResource(R.string.nombres), fontSize = 12.sp, color = Color(0xFF888888), modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 4.dp, bottom = 4.dp))
                OutlinedTextField(
                    value = nombres,
                    onValueChange = { registrationViewModel.onNombresChange(it) },
                    placeholder = { Text(stringResource(R.string.ingresa_nombres), fontSize = 13.sp, color = Color(0xFF888888)) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = OutlinedTextFieldDefaults.colors(unfocusedContainerColor = Color(0xFFD9D9D9), focusedContainerColor = Color(0xFFD9D9D9), unfocusedBorderColor = Color.Black, focusedBorderColor = Color(0xFF043157), unfocusedTextColor = Color(0xFF555555), focusedTextColor = Color(0xFF555555)),
                    singleLine = true,
                    textStyle = LocalTextStyle.current.copy(fontSize = 13.sp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Apellidos
                Text(
                    stringResource(R.string.apellido), fontSize = 12.sp, color = Color(0xFF888888), modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 4.dp, bottom = 4.dp))
                OutlinedTextField(
                    value = apellidos,
                    onValueChange = { registrationViewModel.onApellidosChange(it) },
                    placeholder = { Text(stringResource(R.string.ingresa_tus_apellido), fontSize = 13.sp, color = Color(0xFF888888)) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = OutlinedTextFieldDefaults.colors(unfocusedContainerColor = Color(0xFFD9D9D9), focusedContainerColor = Color(0xFFD9D9D9), unfocusedBorderColor = Color.Black, focusedBorderColor = Color(0xFF043157), unfocusedTextColor = Color(0xFF555555), focusedTextColor = Color(0xFF555555)),
                    singleLine = true,
                    textStyle = LocalTextStyle.current.copy(fontSize = 13.sp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Contraseña
                Text(
                    stringResource(R.string.contrase_), fontSize = 12.sp, color = Color(0xFF888888), modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 4.dp, bottom = 4.dp))
                OutlinedTextField(
                    value = password,
                    onValueChange = { registrationViewModel.onPasswordChange(it) },
                    placeholder = { Text(stringResource(R.string.ingresa_tu_contrase_), fontSize = 13.sp, color = Color(0xFF888888)) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = OutlinedTextFieldDefaults.colors(unfocusedContainerColor = Color(0xFFD9D9D9), focusedContainerColor = Color(0xFFD9D9D9), unfocusedBorderColor = Color.Black, focusedBorderColor = Color(0xFF043157), unfocusedTextColor = Color(0xFF555555), focusedTextColor = Color(0xFF555555)),
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Image(painter = painterResource(id = if (passwordVisible) R.drawable.watch else R.drawable.hide), contentDescription = "Toggle password visibility", modifier = Modifier.size(28.dp))
                        }
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    singleLine = true,
                    textStyle = LocalTextStyle.current.copy(fontSize = 13.sp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Confirmar Contraseña
                Text(
                    stringResource(R.string.confirma_tu_contrase_), fontSize = 12.sp, color = Color(0xFF888888), modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 4.dp, bottom = 4.dp))
                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = { registrationViewModel.onConfirmPasswordChange(it) },
                    placeholder = { Text(stringResource(R.string.ingresa_tu_contrase_a_nuevament), fontSize = 12.sp, color = Color(0xFF888888)) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = OutlinedTextFieldDefaults.colors(unfocusedContainerColor = Color(0xFFD9D9D9), focusedContainerColor = Color(0xFFD9D9D9), unfocusedBorderColor = Color.Black, focusedBorderColor = Color(0xFF043157), unfocusedTextColor = Color(0xFF555555), focusedTextColor = Color(0xFF555555)),
                    visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                            Image(painter = painterResource(id = if (confirmPasswordVisible) R.drawable.watch else R.drawable.hide), contentDescription = "Toggle password visibility", modifier = Modifier.size(28.dp))
                        }
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    singleLine = true,
                    textStyle = LocalTextStyle.current.copy(fontSize = 13.sp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Crear cuenta
                Button(
                    onClick = { registrationViewModel.registerUser() },
                    modifier = Modifier
                        .fillMaxWidth(0.85f)
                        .height(50.dp)
                        .shadow(8.dp, RoundedCornerShape(24.dp)),
                    shape = RoundedCornerShape(24.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7AC8E0))
                ) {
                    if (registrationState is RegistrationState.Loading) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.Black)
                    } else {
                        Text(text = stringResource(R.string.crear_cuent), fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))


                Text(
                    text = stringResource(R.string.ya_tienes_una_cuenta_inicia_sesi_),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF000000),
                    textAlign = TextAlign.Center,
                    textDecoration = TextDecoration.Underline,
                    modifier = Modifier.clickable { navController.navigate(Pantallas.Login.ruta) }
                )

                Spacer(modifier = Modifier.height(8.dp))


                IconButton(
                    onClick = { navController.popBackStack() },
                    modifier = Modifier.size(64.dp)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ellipse_41),
                        contentDescription = stringResource(R.string.regresa),
                        modifier = Modifier.size(56.dp)
                    )
                }
            }
        }


        Image(
            painter = painterResource(id = R.drawable.circular_logo),
            contentDescription = stringResource(R.string.logo_urbanfix),
            modifier = Modifier
                .size(120.dp)
                .align(Alignment.TopCenter)
                .offset(y = 30.dp)
                .zIndex(2f)
        )
    }

    // Diálogos basados en el estado del ViewModel
    when (val state = registrationState) {
        is RegistrationState.Error -> {
            ErrorDialogo(
                errorMessage = state.message,
                onDismiss = { registrationViewModel.dismissDialog() }
            )
        }
        is RegistrationState.Success -> {
            SuccessDialog(
                onDismiss = {
                    registrationViewModel.dismissDialog()
                    navController.navigate(Pantallas.Login.ruta) {
                        popUpTo(navController.graph.startDestinationId) { inclusive = true }
                    }
                }
            )
        }
        else -> { /* No hacer nada en los estados Idle o Loading */ }
    }
}

@Composable
fun ErrorDialogo(errorMessage: String, onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 1.dp),
            shape = RoundedCornerShape(1.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
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
                    Text(stringResource(R.string.error), color = Color.Black, fontSize = 18.sp, fontWeight = FontWeight.Bold)
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
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1D3557)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 48.dp)
                        .padding(bottom = 24.dp)
                        .height(48.dp)
                ) {
                    Text(stringResource(R.string.volver), color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun SuccessDialog(onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 1.dp),
            shape = RoundedCornerShape(1.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .background(Color(0xFFC8E6C9)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(stringResource(R.string.cuenta_creada_exitosamente), color = Color.Black, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp, horizontal = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = stringResource(R.string.ahora_puedes_iniciar_sesi_n),
                        fontSize = 15.sp,
                        fontFamily = FontFamily.SansSerif,
                        color = Color.Black,
                        fontStyle = FontStyle.Italic,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Image(
                        painter = painterResource(id = R.drawable.chulo),
                        contentDescription = stringResource(R.string.xito),
                        modifier = Modifier.size(60.dp)
                    )
                }
                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1D3557)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 48.dp)
                        .padding(bottom = 24.dp)
                        .height(48.dp)
                ) {
                    Text(stringResource(R.string.iniciar), color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}