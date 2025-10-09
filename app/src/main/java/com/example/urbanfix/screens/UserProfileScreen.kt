package com.example.urbanfix.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.urbanfix.R
import com.example.urbanfix.navigation.Pantallas
import com.example.urbanfix.ui.theme.*
import com.example.urbanfix.viewmodel.UserProfileState
import com.example.urbanfix.viewmodel.UserProfileViewModel

// Se define un color rojo para los botones, ya que no existía en tu tema
val RedSignOut = Color(0xFFE63946)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserProfileScreen(navController: NavHostController) {
    val viewModel: UserProfileViewModel = viewModel()
    val userProfileState by viewModel.userProfileState.collectAsState()
    val navigateToLogin by viewModel.navigateToLogin.collectAsState()

    LaunchedEffect(navigateToLogin) {
        if (navigateToLogin) {
            navController.navigate(Pantallas.Login.ruta) {
                popUpTo(0) { inclusive = true }
            }
            viewModel.onNavigateToLoginHandled()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Perfil de Usuario", color = WhiteFull) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver", tint = WhiteFull)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BlueMain)
            )
        },
        bottomBar = {
            BottomNavBar(navController = navController)
        },
        containerColor = GrayBg
    ) { paddingValues ->
        when (val state = userProfileState) {
            is UserProfileState.Loading -> {
                Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = BlueMain)
                }
            }
            is UserProfileState.Success -> {
                ProfileContent(
                    paddingValues = paddingValues,
                    userName = state.userName,
                    userEmail = state.userEmail,
                    onLogoutClick = { viewModel.logoutUser() }
                )
            }
            is UserProfileState.Error -> {
                Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                    Text(text = state.message)
                }
            }
            else -> {}
        }
    }
}

@Composable
private fun ProfileContent(
    paddingValues: PaddingValues,
    userName: String,
    userEmail: String,
    onLogoutClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(20.dp))

        Box(contentAlignment = Alignment.BottomEnd) {
            Image(
                painter = painterResource(id = R.drawable.circular_logo), // Usando tu logo como placeholder
                contentDescription = "Foto de Perfil",
                modifier = Modifier
                    .size(150.dp)
                    .clip(CircleShape)
                    .background(WhiteFull)
            )
            FloatingActionButton(
                onClick = { /* Acción para editar foto */ },
                shape = CircleShape,
                containerColor = RedSignOut,
                modifier = Modifier.size(40.dp)
            ) {
                Icon(Icons.Default.Edit, contentDescription = "Editar foto", tint = WhiteFull)
            }
        }

        Spacer(modifier = Modifier.height(30.dp))

        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = PurpleMain),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(vertical = 8.dp, horizontal = 24.dp)) {
                UserInfoRow(icon = Icons.Default.Person, label = "Nombre Completo", value = userName)
                Divider(color = WhiteFull.copy(alpha = 0.2f), modifier = Modifier.padding(horizontal = 16.dp))
                UserInfoRow(icon = Icons.Default.Email, label = "Correo Electrónico", value = userEmail)
                Divider(color = WhiteFull.copy(alpha = 0.2f), modifier = Modifier.padding(horizontal = 16.dp))
                UserInfoRow(icon = Icons.Default.Domain, label = "Tipo de Cuenta", value = "Personal")
                Divider(color = WhiteFull.copy(alpha = 0.2f), modifier = Modifier.padding(horizontal = 16.dp))
                UserInfoRow(icon = Icons.Default.DateRange, label = "Fecha de registro", value = "27/04/2024")
            }
        }

        Spacer(modifier = Modifier.height(30.dp))

        Button(
            onClick = { /* Acción para editar perfil */ },
            shape = RoundedCornerShape(50),
            colors = ButtonDefaults.buttonColors(containerColor = AquaSoft),
            modifier = Modifier.fillMaxWidth().height(50.dp)
        ) {
            Text("Editar Perfil", color = BlackFull, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = onLogoutClick,
            shape = RoundedCornerShape(50),
            colors = ButtonDefaults.buttonColors(containerColor = RedSignOut),
            modifier = Modifier.fillMaxWidth().height(50.dp)
        ) {
            Text("Cerrar Sesión", color = WhiteFull, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(16.dp))

        TextButton(onClick = { /* Acción para eliminar cuenta */ }) {
            Text("Eliminar Cuenta Permanentemente", color = RedSignOut)
        }
    }
}

// ESTA ES LA CORRECCIÓN MÁS IMPORTANTE
@Composable
private fun UserInfoRow(icon: ImageVector, label: String, value: String) {
    // Se usa un Row para alinear el ícono a la izquierda y el texto a la derecha
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 12.dp).fillMaxWidth()
    ) {
        Icon(icon, contentDescription = label, tint = WhiteFull, modifier = Modifier.size(32.dp))
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(label, color = WhiteFull, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Text(value, color = WhiteFull.copy(alpha = 0.8f), fontSize = 14.sp)
        }
    }
}