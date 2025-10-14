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
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.urbanfix.R
import com.example.urbanfix.navigation.Pantallas
import com.example.urbanfix.ui.theme.*
import com.example.urbanfix.viewmodel.ProfileState
import com.example.urbanfix.viewmodel.ProfileViewModel
import com.example.urbanfix.viewmodel.ViewModelFactory
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    navController: NavHostController
) {
    val context = LocalContext.current
    val viewModel: ProfileViewModel = viewModel(factory = ViewModelFactory(context))
    val profileState by viewModel.profileState.collectAsState()
    val navigateToLogin by viewModel.navigateToLogin.collectAsState()

    // Variables de estado para los diálogos
    var showLogoutDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showDeleteSuccessDialog by remember { mutableStateOf(false) }

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
                    Box(modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues), contentAlignment = Alignment.Center) {
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
                            onLogoutClick = { showLogoutDialog = true },
                            onEditClick = { navController.navigate(Pantallas.EditProfile.ruta) },
                            onDeleteClick = { showDeleteDialog = true },
                            navController = navController
                        )
                    } else {
                        UserProfileContent(
                            navController = navController,
                            paddingValues = paddingValues,
                            userName = state.userName ?: "",
                            userEmail = state.userEmail,
                            registrationDate = state.registrationDate,
                            onLogoutClick = { showLogoutDialog = true },
                            onEditClick = { navController.navigate(Pantallas.EditProfile.ruta) },
                            onDeleteClick = { showDeleteDialog = true }
                        )
                    }
                }
                is ProfileState.Error -> {
                    Box(modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues), contentAlignment = Alignment.Center) {
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
                    Icon(Icons.Default.CheckCircle, contentDescription = stringResource(R.string.exito), tint = Color(0xFF2E7D32))
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

    // Diálogo de confirmación de cierre de sesión
    if (showLogoutDialog) {
        LogoutDialog(
            onDismiss = { showLogoutDialog = false },
            onConfirm = {
                showLogoutDialog = false
                viewModel.logout()
            }
        )
    }

    // Diálogo de confirmación de eliminación de cuenta
    if (showDeleteDialog) {
        DeleteDialog(
            onDismiss = { showDeleteDialog = false },
            onConfirm = {
                showDeleteDialog = false
                showDeleteSuccessDialog = true
            }
        )
    }

    // Diálogo de éxito de eliminación
    if (showDeleteSuccessDialog) {
        SuccessDialogo(
            onDismiss = {
                showDeleteSuccessDialog = false
                navController.navigate(Pantallas.Login.ruta) {
                    popUpTo(0) { inclusive = true }
                }
            }
        )
    }
}

private fun formatDisplayDate(isoDate: String): String {
    return try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
        val outputFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val date = inputFormat.parse(isoDate)
        date?.let { outputFormat.format(it) } ?: isoDate.split("T").firstOrNull() ?: "-"
    } catch (e: Exception) {
        try {
            val parts = isoDate.split("T")
            if (parts.isNotEmpty()) {
                val dateParts = parts[0].split("-")
                if (dateParts.size == 3) {
                    "${dateParts[2]}/${dateParts[1]}/${dateParts[0]}"
                } else "-"
            } else "-"
        } catch (ex: Exception) {
            "-"
        }
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
    onDeleteClick: () -> Unit,
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
                contentDescription = stringResource(R.string.logo),
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
                contentDescription = stringResource(R.string.verificado),
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
                modifier = Modifier
                    .weight(1f)
                    .defaultMinSize(minHeight = 50.dp)
            ) {
                Text(stringResource(id = R.string.edit_profile_button), color = BlackFull, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
            Button(
                onClick = onLogoutClick,
                shape = RoundedCornerShape(50),
                colors = ButtonDefaults.buttonColors(containerColor = RedSignOut),
                modifier = Modifier
                    .weight(1f)
                    .defaultMinSize(minHeight = 50.dp)
            ) {
                Text(stringResource(id = R.string.logout_button), color = WhiteFull, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        TextButton(onClick = onDeleteClick) {
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
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
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
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        ) {
            Text(stringResource(id = R.string.edit_profile_button), color = BlackFull, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = onLogoutClick,
            shape = RoundedCornerShape(50),
            colors = ButtonDefaults.buttonColors(containerColor = RedSignOut),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        ) {
            Text(stringResource(id = R.string.logout_button), color = WhiteFull, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(16.dp))

        TextButton(onClick = onDeleteClick) {
            Text(stringResource(id = R.string.delete_account_button), color = RedSignOut)
        }
    }
}

@Composable
private fun UserInfoRow(icon: ImageVector, label: String, value: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .padding(vertical = 12.dp)
            .fillMaxWidth()
    ) {
        Icon(icon, contentDescription = label, tint = WhiteFull, modifier = Modifier.size(32.dp))
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(label, color = WhiteFull, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Text(value, color = WhiteFull.copy(alpha = 0.8f), fontSize = 14.sp)
        }
    }
}

// DIÁLOGOS DE CONFIRMACIÓN

@Composable
fun LogoutDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
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
                    Text(
                        text = stringResource(R.string.est_s_seguro_de_que_quieres_cerrar_sesi_n),
                        color = Color.Black,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }

                Text(
                    text = stringResource(R.string.podr_s_volver_a_iniciar_sesi_n_en_cualquier_momento),
                    fontSize = 15.sp,
                    fontFamily = androidx.compose.ui.text.font.FontFamily.SansSerif,
                    color = Color.Black,
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
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
                        .height(48.dp)
                ) {
                    Text(stringResource(R.string.cancelar), color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = onConfirm,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF4B3A)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 48.dp)
                        .padding(bottom = 24.dp)
                        .height(48.dp)
                ) {
                    Text(stringResource(R.string.confirmar), color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun DeleteDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
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
                    Text(
                        text = stringResource(R.string.est_s_seguro_de_que_quieres_eliminar_tu_cuenta),
                        color = Color.Black,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }

                Text(
                    text = stringResource(R.string.esta_acci_n_es_permanente_y_no_se_puede_deshacer_se_borrar_n_todos_tus_datos_de_forma_definitiva),
                    fontSize = 15.sp,
                    fontFamily = androidx.compose.ui.text.font.FontFamily.SansSerif,
                    color = Color.Black,
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
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
                        .height(48.dp)
                ) {
                    Text(stringResource(R.string.cancelar), color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = onConfirm,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF4B3A)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 48.dp)
                        .padding(bottom = 24.dp)
                        .height(48.dp)
                ) {
                    Text(stringResource(R.string.confirmar), color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun SuccessDialogo(
    onDismiss: () -> Unit
) {
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
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = stringResource(R.string.cuenta_eliminada_con_xito),
                            color = Color.Black,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = stringResource(R.string.exito),
                            tint = Color(0xFF2E7D32),
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                Text(
                    text = stringResource(R.string.hemos_eliminado_tu_cuenta_y_todos_tus_datos_de_urban_fix_de_forma_permanente_lamentamos_verte_partir_y_te_agradecemos_por_haber_sido_parte_de_nuestra_comunidad),
                    fontSize = 15.sp,
                    fontFamily = androidx.compose.ui.text.font.FontFamily.SansSerif,
                    color = Color.Black,
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
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
                    Text(stringResource(R.string.salir), color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}