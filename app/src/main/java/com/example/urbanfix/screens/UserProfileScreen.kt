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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.urbanfix.R
import com.example.urbanfix.navigation.Pantallas
import com.example.urbanfix.ui.theme.*
import com.example.urbanfix.viewmodel.UserProfileState
import com.example.urbanfix.viewmodel.UserProfileViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserProfileScreen(
    navController: NavHostController,
    viewModel: UserProfileViewModel
) {
    val userProfileState by viewModel.userProfileState.collectAsState()
    val navigateToLogin by viewModel.navigateToLogin.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val successMessage = stringResource(id = R.string.changes_saved_successfully)

    val updateResult = navController.currentBackStackEntry
        ?.savedStateHandle?.get<Boolean>("update_success")
    LaunchedEffect(updateResult) {
        if (updateResult == true) {
            snackbarHostState.showSnackbar(successMessage)
            navController.currentBackStackEntry?.savedStateHandle?.remove<Boolean>("update_success")
        }
    }

    LaunchedEffect(navigateToLogin) {
        if (navigateToLogin) {
            navController.navigate(Pantallas.Login.ruta) {
                popUpTo(0) { inclusive = true }
            }
            viewModel.onNavigateToLoginHandled()
        }
    }

    Scaffold(
        topBar = { /* ... (código de la TopAppBar no cambia) ... */ },
        bottomBar = { /* ... (código de la BottomNavBar no cambia) ... */ },
        containerColor = GrayBg
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize()) {
            when (val state = userProfileState) {
                is UserProfileState.Loading -> { /* ... (código de Loading no cambia) ... */ }
                is UserProfileState.Success -> {
                    ProfileContent(
                        paddingValues = paddingValues,
                        userName = state.userName,
                        userEmail = state.userEmail,
                        onLogoutClick = { viewModel.logoutUser() },
                        onEditClick = {
                            navController.navigate(Pantallas.EditProfile.ruta)
                        },
                        navController = navController
                    )
                }
                is UserProfileState.Error -> { /* ... (código de Error no cambia) ... */ }
                else -> {}
            }
            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier.align(Alignment.TopCenter)
            ) { data ->
                Card(
                    modifier = Modifier.fillMaxWidth().padding(12.dp),
                    shape = RoundedCornerShape(8.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                    colors = CardDefaults.cardColors(containerColor = WhiteFull)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Close, contentDescription = stringResource(id = R.string.close_button_cd))
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(data.visuals.message, fontWeight = FontWeight.Medium)
                    }
                }
            }
        }
    }
}

@Composable
private fun ProfileContent(
    paddingValues: PaddingValues,
    userName: String,
    userEmail: String,
    onLogoutClick: () -> Unit,
    onEditClick: () -> Unit,
    navController: NavHostController
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
                painter = painterResource(id = R.drawable.circular_logo),
                contentDescription = stringResource(id = R.string.profile_picture_cd),
                modifier = Modifier
                    .size(180.dp)
                    .clip(CircleShape)
                    .background(WhiteFull)
            )
            FloatingActionButton(
                onClick = { navController.navigate(Pantallas.Fotoperfil.ruta)  },
                shape = CircleShape,
                containerColor = RedSignOut,
                modifier = Modifier.size(40.dp)
            ) {
                Icon(Icons.Default.Edit, contentDescription = stringResource(id = R.string.edit_photo_cd), tint = WhiteFull)
            }
        }

        Spacer(modifier = Modifier.height(30.dp))

        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = PurpleMain),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(vertical = 8.dp, horizontal = 24.dp)) {
                UserInfoRow(icon = Icons.Default.Person, label = stringResource(id = R.string.full_name_label), value = userName)
                Divider(color = WhiteFull.copy(alpha = 0.2f), modifier = Modifier.padding(horizontal = 16.dp))
                UserInfoRow(icon = Icons.Default.Email, label = stringResource(id = R.string.recover_password_email_label), value = userEmail)
                Divider(color = WhiteFull.copy(alpha = 0.2f), modifier = Modifier.padding(horizontal = 16.dp))
                UserInfoRow(icon = Icons.Default.Domain, label = stringResource(id = R.string.account_type_label), value = stringResource(id = R.string.account_type_value))
                Divider(color = WhiteFull.copy(alpha = 0.2f), modifier = Modifier.padding(horizontal = 16.dp))
                UserInfoRow(icon = Icons.Default.DateRange, label = stringResource(id = R.string.registration_date_label), value = stringResource(id = R.string.registration_date_value))
            }
        }

        Spacer(modifier = Modifier.height(30.dp))

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