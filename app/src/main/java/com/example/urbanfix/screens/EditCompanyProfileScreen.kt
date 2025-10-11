package com.example.urbanfix.screens

import androidx.compose.foundation.Image
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
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.urbanfix.R
import com.example.urbanfix.navigation.Pantallas
import com.example.urbanfix.ui.theme.*
import com.example.urbanfix.viewmodel.CompanyProfileViewModel
import com.example.urbanfix.viewmodel.ViewModelFactory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditCompanyProfileScreen(
    navController: NavHostController,
) {
    val context = LocalContext.current
    val viewModel: CompanyProfileViewModel = viewModel(factory = ViewModelFactory(context))
    val name by viewModel.companyName.collectAsState()
    val password by viewModel.password.collectAsState()
    val confirmPassword by viewModel.confirmPassword.collectAsState()
    val errorMessage by viewModel.editError.collectAsState()

    var passwordVisible by remember { mutableStateOf(false) }

    DisposableEffect(Unit) {
        onDispose {
            viewModel.clearEditError()
        }
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
                            text = stringResource(id = R.string.edit_company_profile_title),
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

            // La sección de la imagen es idéntica
            Box(contentAlignment = Alignment.BottomEnd) {
                Image(
                    painter = painterResource(id = R.drawable.circular_logo),
                    contentDescription = stringResource(id = R.string.logo_content_description),
                    modifier = Modifier.size(180.dp).clip(CircleShape).background(WhiteFull)
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

            Spacer(modifier = Modifier.height(20.dp))


            // Título para el nombre del funcionario
            Text("Nombre del Funcionario", style = MaterialTheme.typography.titleMedium, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(
                value = name,
                onValueChange = viewModel::onNameChange,
                label = { Text(stringResource(id = R.string.funcionario_name_label)) },
                isError = errorMessage?.contains("funcionario") == true,
                supportingText = {
                    val currentError = errorMessage

                    if (currentError?.contains("funcionario") == true) {
                        Text(text = currentError, color = MaterialTheme.colorScheme.error)
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Spacer(modifier = Modifier.height(16.dp))

            // Título para la contraseña
            Text("Cambiar Contraseña", style = MaterialTheme.typography.titleMedium, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(
                value = password,
                onValueChange = viewModel::onPasswordChange,
                label = { Text(stringResource(id = R.string.password_label)) },
                isError = errorMessage?.contains("contraseña") == true,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                trailingIcon = {
                    val image = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(imageVector = image, contentDescription = "Mostrar/Ocultar contraseña")
                    }
                }
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = confirmPassword,
                onValueChange = viewModel::onConfirmPasswordChange,
                label = { Text(stringResource(id = R.string.confirm_password_label)) },
                isError = errorMessage?.contains("contraseña") == true,
                supportingText = {
                    val currentError = errorMessage

                    if (currentError?.contains("contraseña") == true) {
                        Text(text = currentError, color = MaterialTheme.colorScheme.error)
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
                    viewModel.onSaveChanges {
                        navController.previousBackStackEntry?.savedStateHandle?.set("update_success", true)
                        navController.popBackStack()
                    }
                },
                shape = RoundedCornerShape(50),
                colors = ButtonDefaults.buttonColors(containerColor = AquaSoft),
                modifier = Modifier.fillMaxWidth().height(50.dp)
            ) {
                Text(stringResource(id = R.string.confirm_changes_button), color = BlackFull, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}