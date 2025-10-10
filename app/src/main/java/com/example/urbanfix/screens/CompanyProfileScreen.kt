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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.urbanfix.R
import com.example.urbanfix.navigation.Pantallas
import com.example.urbanfix.ui.theme.*
import com.example.urbanfix.viewmodel.CompanyProfileViewModel
import com.example.urbanfix.viewmodel.CompanyProfileState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompanyProfileScreen(
    navController: NavHostController,
    viewModel: CompanyProfileViewModel
) {
    val profileState by viewModel.profileState.collectAsState()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(stringResource(id = R.string.company_profile_title), color = WhiteFull) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(id = R.string.back_button_content_description), tint = WhiteFull)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = BlueMain)
            )
        },
        bottomBar = {
            BottomNavBar(navController = navController)
        },
        containerColor = GrayBg
    ) { paddingValues ->
        when (val state = profileState) {
            is CompanyProfileState.Loading -> {
                Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = BlueMain)
                }
            }
            is CompanyProfileState.Success -> {
                CompanyProfileContent(
                    paddingValues = paddingValues,
                    companyName = state.companyName,
                    userEmail = state.userEmail,
                    onLogoutClick = { /* TODO */ },
                    onEditClick = { /* TODO */ }
                )
            }
            else -> {}
        }
    }
}

@Composable
private fun CompanyProfileContent(
    paddingValues: PaddingValues,
    companyName: String,
    userEmail: String,
    onLogoutClick: () -> Unit,
    onEditClick: () -> Unit
) {
    val verifiedColor = Color(0xFF00BFFF)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(20.dp))

        // --- FOTO DE PERFIL ---
        Box(contentAlignment = Alignment.BottomEnd) {
            Image(
                painter = painterResource(id = R.drawable.circular_logo),
                contentDescription = stringResource(id = R.string.profile_picture_cd),
                modifier = Modifier.size(180.dp).clip(CircleShape).background(WhiteFull)
            )
            FloatingActionButton(
                onClick = { /* Acción para editar foto */ },
                shape = CircleShape,
                containerColor = RedSignOut,
                modifier = Modifier.size(40.dp)
            ) {
                Icon(Icons.Default.Edit, contentDescription = stringResource(id = R.string.edit_photo_cd), tint = WhiteFull)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // --- NUEVO: Nombre de la empresa y badge ---
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = companyName, fontSize = 24.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.width(8.dp))
            Icon(Icons.Filled.CheckCircle, contentDescription = "Verificado", tint = verifiedColor)
        }

        Spacer(modifier = Modifier.height(24.dp))

        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = PurpleMain),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(vertical = 8.dp, horizontal = 24.dp)) {
                UserInfoRow(icon = Icons.Default.Person, label = stringResource(id = R.string.full_name_label), value = "Nombres y apellidos del usuario") // Placeholder
                Divider(color = WhiteFull.copy(alpha = 0.2f), modifier = Modifier.padding(horizontal = 16.dp))
                UserInfoRow(icon = Icons.Default.Email, label = stringResource(id = R.string.profile_email_label), value = userEmail)
                Divider(color = WhiteFull.copy(alpha = 0.2f), modifier = Modifier.padding(horizontal = 16.dp))
                UserInfoRow(icon = Icons.Default.Domain, label = stringResource(id = R.string.account_type_label), value = stringResource(id = R.string.company_account_type)) // <-- CAMBIO
                Divider(color = WhiteFull.copy(alpha = 0.2f), modifier = Modifier.padding(horizontal = 16.dp))
                UserInfoRow(icon = Icons.Default.DateRange, label = stringResource(id = R.string.registration_date_label), value = stringResource(id = R.string.registration_date_value))
                Divider(color = WhiteFull.copy(alpha = 0.2f), modifier = Modifier.padding(horizontal = 16.dp))

                Row(modifier = Modifier.padding(vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Spacer(modifier = Modifier.width(48.dp))
                    Text(stringResource(id = R.string.verified_profile), color = WhiteFull, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Spacer(modifier = Modifier.weight(1f))
                    Icon(Icons.Filled.CheckCircle, contentDescription = "Verificado", tint = verifiedColor)
                }
            }
        }

        Spacer(modifier = Modifier.height(30.dp))

        // --- BOTONES ---
        Button(
            onClick = onEditClick,
            shape = RoundedCornerShape(50),
            colors = ButtonDefaults.buttonColors(containerColor = AquaSoft),
            modifier = Modifier.fillMaxWidth().height(50.dp)
        ) {
            Text(stringResource(id = R.string.edit_profile_button), color = BlackFull, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = onLogoutClick,
            shape = RoundedCornerShape(50),
            colors = ButtonDefaults.buttonColors(containerColor = RedSignOut),
            modifier = Modifier.fillMaxWidth().height(50.dp)
        ) {
            Text(stringResource(id = R.string.logout_button), color = WhiteFull, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(16.dp))

        TextButton(onClick = { /* Acción para eliminar cuenta */ }) {
            Text(stringResource(id = R.string.delete_account_button), color = RedSignOut)
        }
    }
}

@Composable
private fun UserInfoRow(icon: ImageVector, label: String, value: String) {
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
