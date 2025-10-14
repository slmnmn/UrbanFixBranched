package com.example.urbanfix.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.urbanfix.R
import com.example.urbanfix.navigation.Pantallas
import com.example.urbanfix.ui.theme.*
import com.example.urbanfix.viewmodel.ProfileState
import com.example.urbanfix.viewmodel.ProfileViewModel
import com.example.urbanfix.viewmodel.ViewModelFactory
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    navController: NavHostController
) {
    val context = LocalContext.current
    val viewModel: ProfileViewModel = viewModel(factory = ViewModelFactory(context))
    val profileState by viewModel.profileState.collectAsState()
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

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 35.dp, end = 48.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                // Lógica para cambiar el título según el rol
                                text = when (val state = profileState) {
                                    is ProfileState.Success -> {
                                        if (state.role == "funcionario") {
                                            stringResource(id = R.string.company_profile_title)
                                        } else {
                                            stringResource(id = R.string.profile_title)
                                        }
                                    }
                                    else -> stringResource(id = R.string.profile_title)
                                },
                                color = WhiteFull,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center
                            )
                        }
                    },
                    navigationIcon = {
                        Box(modifier = Modifier.padding(top = 20.dp)) {
                            IconButton(onClick = { navController.popBackStack() }) {
                                Icon(
                                    Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = stringResource(id = R.string.back_button_content_description),
                                    tint = WhiteFull
                                )
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = BlueMain
                    ),
                    modifier = Modifier.height(72.dp)
                )
            },
            bottomBar = {
                BottomNavBar(navController = navController)
            },
            containerColor = GrayBg
        ) { paddingValues ->
            when (val state = profileState) {
                is ProfileState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = BlueMain)
                    }
                }
                is ProfileState.Success -> {
                    if (state.role == "funcionario") {
                        CompanyProfileContent(
                            paddingValues = paddingValues,
                            companyName = state.companyName ?: "",
                            personalName = state.personalName ?: "",
                            userEmail = state.userEmail,
                            registrationDate = state.registrationDate,
                            onLogoutClick = { viewModel.logout() },
                            onEditClick = { navController.navigate(Pantallas.EditProfile.ruta) },
                            navController = navController
                        )
                    } else {
                        UserProfileContent(
                            navController = navController,
                            paddingValues = paddingValues,
                            userName = state.userName ?: "",
                            userEmail = state.userEmail,
                            registrationDate = state.registrationDate,
                            onLogoutClick = { viewModel.logout() },
                            onEditClick = { navController.navigate(Pantallas.EditProfile.ruta) }
                        )
                    }
                }
                is ProfileState.Error -> {
                    Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                        Text(
                            text = stringResource(id = state.messageId),
                            color = Color.Red,
                            fontSize = 16.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    }
                }
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 16.dp)
        ) { data ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFE0F5E1)),
                border = BorderStroke(1.dp, Color.Black)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.CheckCircle, contentDescription = "Éxito", tint = Color(0xFF2E7D32))
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = data.visuals.message,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                }
            }
        }
    }
}

private fun formatDisplayDate(isoDate: String): String {
    return try {
        // Define el formato de entrada (lo que envía el servidor)
        val inputFormatter = DateTimeFormatter.ISO_DATE_TIME
        // Parsea el texto a un objeto de fecha y hora
        val dateTime = LocalDateTime.parse(isoDate, inputFormatter)
        // Define el formato de salida que quieres (dd/MM/yyyy)
        val outputFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
        // Formatea la fecha al nuevo estilo
        dateTime.format(outputFormatter)
    } catch (e: DateTimeParseException) {
        // Si la fecha no se puede "traducir", muestra el texto original o un guion
        isoDate.split("T").firstOrNull() ?: "-"
    }
}
@Composable
private fun CompanyProfileContent(
    paddingValues: PaddingValues,
    companyName: String,
    personalName: String,
    userEmail: String,
    registrationDate: String,
    onLogoutClick: () -> Unit,
    onEditClick: () -> Unit,
    navController: NavHostController
) {
    val verifiedColor = Color(0xFF00BFFF)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(20.dp))

        Box(contentAlignment = Alignment.BottomEnd) {
            Image(
                painter = painterResource(id = R.drawable.circular_logo),
                contentDescription = "Logo",
                modifier = Modifier.size(180.dp)
            )
            FloatingActionButton(
                onClick = { navController.navigate(Pantallas.Fotoperfil.ruta)},
                shape = CircleShape,
                containerColor = RedSignOut,
                modifier = Modifier.size(40.dp)
            ) {
                Icon(Icons.Default.Edit, contentDescription = stringResource(id = R.string.edit_photo_cd), tint = WhiteFull)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = companyName,
                modifier = Modifier.weight(1f, fill = false),
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                lineHeight = 26.sp
            )

            Spacer(modifier = Modifier.width(8.dp))
            Icon(
                Icons.Filled.CheckCircle,
                contentDescription = "Verificado",
                tint = BlueMain
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor=Color(0xFF663251)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(vertical = 8.dp, horizontal = 24.dp)) {
                UserInfoRow(icon = Icons.Default.Person, label = stringResource(id = R.string.funcionario_name_label), value = personalName)
                Divider(color = WhiteFull.copy(alpha = 0.2f), modifier = Modifier.padding(horizontal = 16.dp))
                UserInfoRow(icon = Icons.Default.Email, label = stringResource(id = R.string.profile_email_label), value = userEmail)
                Divider(color = WhiteFull.copy(alpha = 0.2f), modifier = Modifier.padding(horizontal = 16.dp))
                UserInfoRow(icon = Icons.Default.Domain, label = stringResource(id = R.string.account_type_label), value = stringResource(id = R.string.company_account_type))
                Divider(color = WhiteFull.copy(alpha = 0.2f), modifier = Modifier.padding(horizontal = 16.dp))
                UserInfoRow(
                    icon = Icons.Default.DateRange,
                    label = stringResource(id = R.string.registration_date_label),
                    value = formatDisplayDate(registrationDate)
                )
                Divider(color = WhiteFull.copy(alpha = 0.2f), modifier = Modifier.padding(horizontal = 16.dp))

                Row(
                    modifier = Modifier.padding(vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Spacer(modifier = Modifier.width(60.dp))
                    Text(
                        stringResource(R.string.verified_profile),
                        color = WhiteFull,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        modifier = Modifier.padding(start = 30.dp, top = 10.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        Icons.Filled.CheckCircle,
                        contentDescription = stringResource(R.string.verified_profile),
                        tint = verifiedColor,
                        modifier = Modifier.padding(start = 4.dp, top = 10.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(30.dp))
        Spacer(modifier = Modifier.weight(1f))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = onEditClick,
                shape = RoundedCornerShape(50),
                colors = ButtonDefaults.buttonColors(containerColor = AquaSoft),
                modifier = Modifier.weight(1f).defaultMinSize(minHeight = 50.dp)
            ) {
                Text(stringResource(id = R.string.edit_profile_button), color = BlackFull, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
            Button(
                onClick = onLogoutClick,
                shape = RoundedCornerShape(50),
                colors = ButtonDefaults.buttonColors(containerColor = RedSignOut),
                modifier = Modifier.weight(1f).defaultMinSize(minHeight = 50.dp)
            ) {
                Text(stringResource(id = R.string.logout_button), color = WhiteFull, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        TextButton(onClick = { /* Acción para eliminar cuenta */ }) {
            Text(stringResource(id = R.string.delete_account_button), color = RedSignOut)
        }
        Spacer(modifier = Modifier.height(8.dp))
    }
}

@Composable
private fun UserProfileContent(
    navController: NavHostController,
    paddingValues: PaddingValues,
    userName: String,
    userEmail: String,
    registrationDate: String,
    onLogoutClick: () -> Unit,
    onEditClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .verticalScroll(rememberScrollState())
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
                onClick = { navController.navigate(Pantallas.Fotoperfil.ruta) },
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
            colors = CardDefaults.cardColors(containerColor=Color(0xFF663251)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(vertical = 8.dp, horizontal = 24.dp)) {
                UserInfoRow(icon = Icons.Default.Person, label = stringResource(id = R.string.full_name_label), value = userName)
                Divider(color = WhiteFull.copy(alpha = 0.2f), modifier = Modifier.padding(horizontal = 16.dp))
                UserInfoRow(icon = Icons.Default.Email, label = stringResource(id = R.string.profile_email_label), value = userEmail)
                Divider(color = WhiteFull.copy(alpha = 0.2f), modifier = Modifier.padding(horizontal = 16.dp))
                UserInfoRow(icon = Icons.Default.Domain, label = stringResource(id = R.string.account_type_label), value = stringResource(id = R.string.account_type_value))
                Divider(color = WhiteFull.copy(alpha = 0.2f), modifier = Modifier.padding(horizontal = 16.dp))
                UserInfoRow(
                    icon = Icons.Default.DateRange,
                    label = stringResource(id = R.string.registration_date_label),
                    value = formatDisplayDate(registrationDate)
                )
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
