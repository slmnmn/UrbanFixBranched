package com.example.urbanfix.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.ui.zIndex
import androidx.navigation.NavHostController
import com.example.urbanfix.R
import com.example.urbanfix.navigation.Pantallas

@Composable
fun RegistroScreen(navController: NavHostController) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF043157)), // Fondo azul oscuro
        contentAlignment = Alignment.Center
    ) {
        // Imagen de fondo
        Image(
            painter = painterResource(id = R.drawable.log_back),
            contentDescription = "Bienvenido UrbanFix",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Box para superponer el logo sobre las tarjetas
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.TopCenter
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Espacio para el logo (mitad arriba)
                    Spacer(modifier = Modifier.height(64.dp))

                    // Tarjeta azul celeste - más pequeña
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
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 22.dp, vertical = 16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            // Espacio para el logo (mitad abajo) - reducido
                            Spacer(modifier = Modifier.height(40.dp))

                            // Recuadro interior gris
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(340.dp),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = Color(0xFFE2DEDE)
                                )
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(24.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Top
                                ) {
                                    Spacer(modifier = Modifier.height(50.dp))

                                    Text(
                                        text = "Antes de comenzar...",
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF1D3557)
                                    )

                                    Spacer(modifier = Modifier.height(32.dp))

                                    Text(
                                        text = "¿Quieres crear tu cuenta como funcionario de una organización?",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF1D3557),
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.padding(bottom = 32.dp)
                                    )

                                    // Botones Sí y No
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceEvenly
                                    ) {
                                        OutlinedButton(
                                            onClick = {
                                                navController.navigate(Pantallas.RegEmpresa.ruta)
                                            },
                                            modifier = Modifier
                                                .weight(1f)
                                                .padding(end = 8.dp)
                                                .height(52.dp)
                                                .shadow(6.dp, RoundedCornerShape(26.dp)),
                                            colors = ButtonDefaults.outlinedButtonColors(
                                                containerColor = Color(0xFFE0F5E1)
                                            ),
                                            border = BorderStroke(2.dp, Color(0xFF4AB7B6)),
                                            shape = RoundedCornerShape(26.dp)

                                        ) {
                                            Text(
                                                text = "Sí",
                                                color = Color(0xFF1D3557),
                                                fontSize = 16.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }

                                        OutlinedButton(
                                            onClick = {
                                                navController.navigate(Pantallas.RegUsuario.ruta)
                                            },
                                            modifier = Modifier
                                                .weight(1f)
                                                .padding(start = 8.dp)
                                                .height(52.dp)
                                                .shadow(6.dp, RoundedCornerShape(26.dp)),
                                            colors = ButtonDefaults.outlinedButtonColors(
                                                containerColor = Color(0xFFE0F5E1)
                                            ),
                                            border = BorderStroke(2.dp, Color(0xFF4AB7B6)),
                                            shape = RoundedCornerShape(26.dp)
                                        ) {
                                            Text(
                                                text = "No",
                                                color = Color(0xFF1D3557),
                                                fontSize = 16.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(20.dp))

                            // Texto inferior dentro de la tarjeta azul
                            Text(
                                text = "¿Ya tienes una cuenta? Inicia sesión",
                                fontSize = 14.sp,
                                color = Color(0xFF1D3557),
                                fontWeight = FontWeight.Bold,
                                textDecoration = TextDecoration.Underline,
                                modifier = Modifier.clickable {
                                    navController.navigate(Pantallas.Login.ruta)
                                }
                            )

                            Spacer(modifier = Modifier.height(12.dp))
                        }
                    }
                }

                // Logo circular superpuesto - más abajo
                Box(
                    modifier = Modifier
                        .offset(y = 16.dp) // Bajado un poco más
                        .size(128.dp)
                        .zIndex(10f)
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
            }
        }
    }
}