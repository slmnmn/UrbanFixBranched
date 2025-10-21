@file:JvmName("EditProfileScreenKt")

package com.example.urbanfix.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.urbanfix.R
import com.example.urbanfix.navigation.Pantallas
import com.example.urbanfix.ui.theme.*
import com.example.urbanfix.viewmodel.ProfileViewModel
import com.example.urbanfix.viewmodel.ViewModelFactory
import androidx.compose.foundation.Image
import androidx.compose.ui.layout.ContentScale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(
    navController: NavHostController,
) {
    val context = LocalContext.current
    val viewModel: ProfileViewModel = viewModel(factory = ViewModelFactory(context))

    val role by viewModel.role.collectAsState()
    val name by viewModel.name.collectAsState()
    val password by viewModel.password.collectAsState()
    val confirmPassword by viewModel.confirmPassword.collectAsState()
    val errorMessageId by viewModel.editError.collectAsState()
    val profilePicBitmap by viewModel.profilePicBitmap.collectAsState()

    var passwordVisible by remember { mutableStateOf(false) }
    var showExitDialog by remember { mutableStateOf(false) }
    var showSaveDialog by remember { mutableStateOf(false) }

    DisposableEffect(Unit) {
        onDispose { viewModel.clearEditError() }
    }

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
                            text = if (role == "funcionario") {
                                stringResource(id = R.string.edit_company_profile_title)
                            } else {
                                stringResource(id = R.string.edit_profile_title)
                            },
                            color = WhiteFull,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                    }
                },
                navigationIcon = {
                    Box(modifier = Modifier.padding(top = 20.dp)) {
                        IconButton(onClick = { showExitDialog = true }) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = stringResource(id = R.string.back_button_content_description),
                                tint = WhiteFull
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BlueMain),
                modifier = Modifier.height(72.dp)
            )
        },
        bottomBar = {
            BottomNavBar(navController = navController)
        },
        containerColor = GrayBg
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(20.dp))

            Box(contentAlignment = Alignment.BottomEnd) {
                // Mostrar Bitmap si existe, sino logo por defecto
                if (profilePicBitmap != null) {
                    Image(
                        bitmap = profilePicBitmap!!.asImageBitmap(),
                        contentDescription = stringResource(id = R.string.profile_picture_cd),
                        modifier = Modifier
                            .size(180.dp)
                            .clip(CircleShape)
                            .background(WhiteFull),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Image(
                        painter = painterResource(id = R.drawable.circular_logo),
                        contentDescription = stringResource(id = R.string.profile_picture_cd),
                        modifier = Modifier
                            .size(180.dp)
                            .clip(CircleShape)
                            .background(WhiteFull),
                        contentScale = ContentScale.Crop
                    )
                }

                FloatingActionButton(
                    onClick = { navController.navigate(Pantallas.Fotoperfil.ruta) },
                    shape = CircleShape,
                    containerColor = RedSignOut,
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(Icons.Default.Edit, contentDescription = stringResource(id = R.string.edit_photo_cd), tint = WhiteFull)
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = if (role == "funcionario") stringResource(id = R.string.edit_profile_official_title_section) else stringResource(id = R.string.edit_profile_user_title_section),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = name,
                onValueChange = viewModel::onNameChange,
                label = {
                    Text(
                        if (role == "funcionario") {
                            stringResource(id = R.string.funcionario_name_label)
                        } else {
                            stringResource(id = R.string.full_name_label)
                        }
                    )
                },
                isError = errorMessageId == R.string.error_user_name_empty || errorMessageId == R.string.error_official_name_empty,
                supportingText = {
                    if (errorMessageId == R.string.error_user_name_empty || errorMessageId == R.string.error_official_name_empty) {
                        Text(text = stringResource(id = errorMessageId!!), color = MaterialTheme.colorScheme.error)
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = stringResource(id = R.string.edit_profile_password_section),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = password,
                onValueChange = viewModel::onPasswordChange,
                label = { Text(stringResource(id = R.string.password_label)) },
                isError = errorMessageId != null,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                trailingIcon = {
                    val image = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(imageVector = image, contentDescription = stringResource(id = R.string.toggle_password_visibility))
                    }
                }
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = confirmPassword,
                onValueChange = viewModel::onConfirmPasswordChange,
                label = { Text(stringResource(id = R.string.confirm_password_label)) },
                isError = errorMessageId == R.string.error_password_length || errorMessageId == R.string.error_password_mismatch,
                supportingText = {
                    if (errorMessageId == R.string.error_password_length || errorMessageId == R.string.error_password_mismatch) {
                        Text(text = stringResource(id = errorMessageId!!), color = MaterialTheme.colorScheme.error)
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
            )

            Spacer(modifier = Modifier.weight(1f))
            Button(
                onClick = {
                    if (viewModel.validateForSave()) {
                        showSaveDialog = true
                    }
                },
                shape = RoundedCornerShape(50),
                colors = ButtonDefaults.buttonColors(containerColor = AquaSoft),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Text(stringResource(id = R.string.confirm_changes_button), color = BlackFull, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(20.dp))
        }
    }

    if (showExitDialog) {
        ExitEditDialog(
            onDismiss = { showExitDialog = false },
            onConfirm = {
                showExitDialog = false
                navController.popBackStack()
            }
        )
    }

    if (showSaveDialog) {
        SaveChangesDialog(
            onDismiss = { showSaveDialog = false },
            onConfirm = {
                showSaveDialog = false
                viewModel.onSaveChanges {
                    navController.previousBackStackEntry?.savedStateHandle?.set("update_success", true)
                    navController.popBackStack()
                }
            }
        )
    }
}

@Composable
fun ExitEditDialog(
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
                        text = stringResource(R.string.est_s_seguro_que_quieres_salir),
                        color = Color.Black,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }

                Text(
                    text = stringResource(R.string.no_se_guardar_n_tus_cambios_si_abandonas_el_proceso),
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
fun SaveChangesDialog(
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
                        text = stringResource(R.string.quieres_guardar_tus_cambios),
                        color = Color.Black,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }

                Text(
                    text = stringResource(R.string.tus_datos_ser_n_actualizados_si_confirmas_esta_acci_n),
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