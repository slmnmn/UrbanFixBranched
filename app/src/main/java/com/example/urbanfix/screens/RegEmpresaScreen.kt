package com.example.urbanfix.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import kotlin.OptIn
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.zIndex
import androidx.navigation.NavHostController
import com.example.urbanfix.R
import com.example.urbanfix.navigation.Pantallas

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegEmpresaScreen(navController: NavHostController) {
    var emailInstitucional by remember { mutableStateOf("") }
    var empresaSeleccionada by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }
    var nombres by remember { mutableStateOf("") }
    var apellidos by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }
    var showErrorDialog by remember { mutableStateOf(false) }
    var showSuccessDialog by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    val empresas = listOf(
        "UAESP (Unidad Administrativa Especial de Servicios Públicos)",
        "Empresa de Acueducto y Alcantarillado de Bogotá (EAAB)",
        "Unidad de Mantenimiento Vial (UMV)",
        "Instituto de Desarrollo Urbano (IDU)",
        "Bomberos de Bogotá"
    )

    val scrollState = rememberScrollState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF043157))
    ) {
        // Imagen de fondo
        Image(
            painter = painterResource(id = R.drawable.registro_normal),
            contentDescription = "Fondo",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        // Card contenedor del formulario
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.82f)
                .padding(horizontal = 24.dp)
                .align(Alignment.TopCenter)
                .offset(y = 100.dp)
                .zIndex(1f),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFFE0F5E1)
            ),
            border = androidx.compose.foundation.BorderStroke(2.dp, Color.Black)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(scrollState)
                    .padding(20.dp)
                    .padding(top = 30.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Regístrate ahora",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Campo E-mail institucional
                Text(
                    text = "E-mail institucional",
                    fontSize = 12.sp,
                    color = Color(0xFF888888),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 4.dp, bottom = 4.dp)
                )
                OutlinedTextField(
                    value = emailInstitucional,
                    onValueChange = { emailInstitucional = it },
                    placeholder = { Text("Ingresa tu e-mail institucional", fontSize = 13.sp, color = Color(0xFF888888)) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedContainerColor = Color(0xFFD9D9D9),
                        focusedContainerColor = Color(0xFFD9D9D9),
                        unfocusedBorderColor = Color.Black,
                        focusedBorderColor = Color(0xFF043157),
                        unfocusedTextColor = Color(0xFF555555),
                        focusedTextColor = Color(0xFF555555)
                    ),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    singleLine = true,
                    textStyle = LocalTextStyle.current.copy(fontSize = 13.sp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Campo Selecciona la empresa
                Text(
                    text = "Selecciona la empresa a la que perteneces",
                    fontSize = 12.sp,
                    color = Color(0xFF888888),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 4.dp, bottom = 4.dp)
                )
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = empresaSeleccionada,
                        onValueChange = {},
                        readOnly = true,
                        placeholder = { Text("Selecciona tu organización", fontSize = 13.sp, color = Color(0xFF888888)) },
                        trailingIcon = {
                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = "Dropdown",
                                tint = Color(0xFF555555)
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .menuAnchor(),
                        shape = RoundedCornerShape(24.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedContainerColor = Color(0xFFD9D9D9),
                            focusedContainerColor = Color(0xFFD9D9D9),
                            unfocusedBorderColor = Color.Black,
                            focusedBorderColor = Color(0xFF043157),
                            unfocusedTextColor = Color(0xFF555555),
                            focusedTextColor = Color(0xFF555555)
                        ),
                        textStyle = LocalTextStyle.current.copy(fontSize = 13.sp)
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false },
                        modifier = Modifier.background(Color(0xFFD9D9D9))
                    ) {
                        empresas.forEach { empresa ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = empresa,
                                        fontSize = 13.sp,
                                        color = Color(0xFF555555)
                                    )
                                },
                                onClick = {
                                    empresaSeleccionada = empresa
                                    expanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Campo Nombres
                Text(
                    text = "Nombres",
                    fontSize = 12.sp,
                    color = Color(0xFF888888),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 4.dp, bottom = 4.dp)
                )
                OutlinedTextField(
                    value = nombres,
                    onValueChange = { nombres = it },
                    placeholder = { Text("Ingresa tus nombres", fontSize = 13.sp, color = Color(0xFF888888)) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedContainerColor = Color(0xFFD9D9D9),
                        focusedContainerColor = Color(0xFFD9D9D9),
                        unfocusedBorderColor = Color.Black,
                        focusedBorderColor = Color(0xFF043157),
                        unfocusedTextColor = Color(0xFF555555),
                        focusedTextColor = Color(0xFF555555)
                    ),
                    singleLine = true,
                    textStyle = LocalTextStyle.current.copy(fontSize = 13.sp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Campo Apellidos
                Text(
                    text = "Apellidos",
                    fontSize = 12.sp,
                    color = Color(0xFF888888),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 4.dp, bottom = 4.dp)
                )
                OutlinedTextField(
                    value = apellidos,
                    onValueChange = { apellidos = it },
                    placeholder = { Text("Ingresa tus apellidos", fontSize = 13.sp, color = Color(0xFF888888)) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedContainerColor = Color(0xFFD9D9D9),
                        focusedContainerColor = Color(0xFFD9D9D9),
                        unfocusedBorderColor = Color.Black,
                        focusedBorderColor = Color(0xFF043157),
                        unfocusedTextColor = Color(0xFF555555),
                        focusedTextColor = Color(0xFF555555)
                    ),
                    singleLine = true,
                    textStyle = LocalTextStyle.current.copy(fontSize = 13.sp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Campo Contraseña
                Text(
                    text = "Contraseña",
                    fontSize = 12.sp,
                    color = Color(0xFF888888),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 4.dp, bottom = 4.dp)
                )
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    placeholder = { Text("Ingresa tu contraseña", fontSize = 13.sp, color = Color(0xFF888888)) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedContainerColor = Color(0xFFD9D9D9),
                        focusedContainerColor = Color(0xFFD9D9D9),
                        unfocusedBorderColor = Color.Black,
                        focusedBorderColor = Color(0xFF043157),
                        unfocusedTextColor = Color(0xFF555555),
                        focusedTextColor = Color(0xFF555555)
                    ),
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Image(
                                painter = painterResource(
                                    id = if (passwordVisible) R.drawable.watch else R.drawable.hide
                                ),
                                contentDescription = "Toggle password visibility",
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    singleLine = true,
                    textStyle = LocalTextStyle.current.copy(fontSize = 13.sp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Campo Confirmar Contraseña
                Text(
                    text = "Confirma tu contraseña",
                    fontSize = 12.sp,
                    color = Color(0xFF888888),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 4.dp, bottom = 4.dp)
                )
                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it },
                    placeholder = { Text("Ingresa tu contraseña nuevamente", fontSize = 12.sp, color = Color(0xFF888888)) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedContainerColor = Color(0xFFD9D9D9),
                        focusedContainerColor = Color(0xFFD9D9D9),
                        unfocusedBorderColor = Color.Black,
                        focusedBorderColor = Color(0xFF043157),
                        unfocusedTextColor = Color(0xFF555555),
                        focusedTextColor = Color(0xFF555555)
                    ),
                    visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                            Image(
                                painter = painterResource(
                                    id = if (confirmPasswordVisible) R.drawable.watch else R.drawable.hide
                                ),
                                contentDescription = "Toggle password visibility",
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    singleLine = true,
                    textStyle = LocalTextStyle.current.copy(fontSize = 13.sp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Botón Crear cuenta
                Button(
                    onClick = {
                        when {
                            emailInstitucional.isBlank() || empresaSeleccionada.isBlank() ||
                                    nombres.isBlank() || apellidos.isBlank() ||
                                    password.isBlank() || confirmPassword.isBlank() -> {
                                errorMessage = "Debes completar todos los campos"
                                showErrorDialog = true
                            }
                            password != confirmPassword -> {
                                errorMessage = "Verifica tu contraseña y confirmación."
                                showErrorDialog = true
                            }
                            else -> {
                                // Todos los campos están llenos y las contraseñas coinciden
                                showSuccessDialog = true
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth(0.85f)
                        .height(50.dp)
                        .shadow(8.dp, RoundedCornerShape(24.dp)),
                    shape = RoundedCornerShape(24.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF7AC8E0)
                    )
                ) {
                    Text(
                        text = "Crear cuenta",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Texto de inicio de sesión - clicable
                Text(
                    text = "¿Ya tienes una cuenta? Inicia sesión",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF000000),
                    textAlign = TextAlign.Center,
                    textDecoration = androidx.compose.ui.text.style.TextDecoration.Underline,
                    modifier = Modifier.clickable {
                        navController.navigate(Pantallas.Login.ruta)
                    }
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Botón de regresar
                IconButton(
                    onClick = { navController.popBackStack() },
                    modifier = Modifier.size(64.dp)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ellipse_41),
                        contentDescription = "Regresar",
                        modifier = Modifier.size(56.dp)
                    )
                }
            }
        }

        // Logo en la parte superior - sobre el card con zIndex mayor
        Image(
            painter = painterResource(id = R.drawable.circular_logo),
            contentDescription = "Logo UrbanFix",
            modifier = Modifier
                .size(120.dp)
                .align(Alignment.TopCenter)
                .offset(y = 30.dp)
                .zIndex(2f)
        )
    }

    if (showErrorDialog) {
        ErrorDialogo(
            errorMessage = errorMessage,
            onDismiss = { showErrorDialog = false }
        )
    }

    if (showSuccessDialog) {
        SuccessDialog(
            onDismiss = {
                showSuccessDialog = false
                navController.navigate(Pantallas.Login.ruta)
            }
        )
    }
}

