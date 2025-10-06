package com.example.urbanfix.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.urbanfix.R
import com.example.urbanfix.navigation.Pantallas
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation

@Composable
fun LoginScreen(navController: NavController) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var rememberMe by remember { mutableStateOf(false) }
    var passwordVisible by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        // Fondo con imagen
        Image(
            painter = painterResource(id = R.drawable.log_back),
            contentDescription = "Fondo Login UrbanFix",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(215.dp))

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(
                        elevation = 20.dp,
                        shape = RoundedCornerShape(32.dp),
                        clip = false
                    ),
                shape = RoundedCornerShape(32.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFA8DADC)
                )
            ) {
                Column(
                    modifier = Modifier.padding(22.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Logo circular con borde blanco
                    Box(
                        modifier = Modifier
                            .size(128.dp)
                            .background(Color.White, CircleShape)
                            .padding(2.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.circular_logo),
                            contentDescription = "Logo UrbanFix",
                            modifier = Modifier
                                .size(130.dp)
                                .clip(CircleShape)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Por favor ingresa tus credenciales",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = Color(0xFF1D3557),
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // Label E-mail
                    Text(
                        text = "E-mail",
                        fontSize = 14.sp,
                        color = Color(0xFF658384),
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 4.dp, bottom = 4.dp)
                    )

                    // Campo email
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        placeholder = { Text("Ingresa tu e-mail", fontSize = 13.sp, color = Color.Gray) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .border(
                                width = 1.5.dp,
                                color = Color(0xFF1D3557).copy(alpha = 0.8f),
                                shape = RoundedCornerShape(26.dp)
                            ),
                        shape = RoundedCornerShape(26.dp),
                        singleLine = true,
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color(0xFFE8E8E8),
                            unfocusedContainerColor = Color(0xFFE8E8E8),
                            disabledContainerColor = Color(0xFFE8E8E8),
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            disabledIndicatorColor = Color.Transparent
                        )
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Label Contraseña
                    Text(
                        text = "Contraseña",
                        fontSize = 14.sp,
                        color = Color(0xFF658384),
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 4.dp, bottom = 4.dp)
                    )

                    // Campo contraseña con ojito
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        placeholder = { Text("Ingresa tu contraseña", fontSize = 13.sp, color = Color.Gray) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .border(
                                width = 1.5.dp,
                                color = Color(0xFF1D3557).copy(alpha = 0.8f),
                                shape = RoundedCornerShape(26.dp)
                            ),
                        shape = RoundedCornerShape(26.dp),
                        singleLine = true,
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            val image = if (passwordVisible) R.drawable.watch else R.drawable.hide
                            val description =
                                if (passwordVisible) "Ocultar contraseña" else "Mostrar contraseña"

                            Image(
                                painter = painterResource(id = image),
                                contentDescription = description,
                                modifier = Modifier
                                    .size(20.dp)
                                    .clickable { passwordVisible = !passwordVisible }
                            )
                        },
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color(0xFFE8E8E8),
                            unfocusedContainerColor = Color(0xFFE8E8E8),
                            disabledContainerColor = Color(0xFFE8E8E8),
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            disabledIndicatorColor = Color.Transparent
                        )
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Checkbox circular + olvidar contraseña
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
                            // Checkbox circular personalizado
                            Box(
                                modifier = Modifier
                                    .size(20.dp)
                                    .border(
                                        width = 2.dp,
                                        color = if (rememberMe) Color(0xFFE63946) else Color(0xFF1D3557),
                                        shape = CircleShape
                                    )
                                    .background(
                                        color = if (rememberMe) Color(0xFFE8E8E8) else Color(0xFFE8E8E8),
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
                                text = "Recordarme",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1D3557)
                            )
                        }

                        Text(
                            text = "¿Olvidaste tu contraseña?",
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

                    // Botón iniciar sesión
                    Button(
                        onClick = { navController.navigate(Pantallas.Home.ruta) },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF1FAEE)),
                        shape = RoundedCornerShape(26.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top= 10.dp, bottom = 10.dp, start = 50.dp, end = 50.dp)
                            .height(52.dp)
                            .shadow(8.dp, RoundedCornerShape(26.dp))
                            .border(
                                width = 2.dp,
                                color = Color(0xFF4AB7B6),
                                shape = RoundedCornerShape(26.dp)
                            )
                    ) {
                        Text(
                            text = "Iniciar sesión",
                            color = Color(0xFF1D3557),
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Registro
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "¿No tienes una cuenta? ",
                            color = Color(0xFF1D3557),
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Text(
                            text = "Regístrate",
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
    }
}