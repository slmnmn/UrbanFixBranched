package com.example.urbanfix.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.urbanfix.R
import com.example.urbanfix.navigation.Pantallas
import com.example.urbanfix.viewmodel.RegEmpresaViewModel
import com.example.urbanfix.viewmodel.RegState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegEmpresaScreen(
    navController: NavHostController,
    regViewModel: RegEmpresaViewModel = viewModel()
) {
    val emailInstitucional by regViewModel.email.collectAsState()
    val nombres by regViewModel.nombres.collectAsState()
    val apellidos by regViewModel.apellidos.collectAsState()
    val password by regViewModel.password.collectAsState()
    val confirmPassword by regViewModel.confirmPassword.collectAsState()
    val regState by regViewModel.regState.collectAsState()

    var empresaSeleccionada by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }

    val empresas = listOf(
        stringResource(R.string.uaesp_unidad_administrativa_especial_de_servicios_p_blicos) to 1,
        stringResource(R.string.empresa_de_acueducto_y_alcantarillado_de_bogot_eaab) to 2,
        stringResource(R.string.unidad_de_mantenimiento_vial_umv) to 3,
        stringResource(R.string.instituto_de_desarrollo_urbano_idu) to 4,
        stringResource(R.string.bomberos_de_bogot) to 5
    )

    val scrollState = rememberScrollState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF043157))
    ) {
        Image(
            painter = painterResource(id = R.drawable.registro_normal),
            contentDescription = stringResource(R.string.fondo),
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.82f)
                .padding(horizontal = 24.dp)
                .align(Alignment.TopCenter)
                .offset(y = 100.dp)
                .zIndex(1f),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFE0F5E1)),
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
                Text(text = stringResource(R.string.reg_strate_ahora), fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    stringResource(R.string.e_mail_institucional), fontSize = 12.sp, color = Color(0xFF888888), modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 4.dp, bottom = 4.dp))
                OutlinedTextField(
                    value = emailInstitucional,
                    onValueChange = { regViewModel.email.value = it },
                    placeholder = { Text(stringResource(R.string.ingresa_tu_e_mail_institucional), fontSize = 13.sp, color = Color(0xFF888888)) },
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

                Text(
                    stringResource(R.string.selecciona_la_empresa), fontSize = 12.sp, color = Color(0xFF888888), modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 4.dp, bottom = 4.dp))
                ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
                    OutlinedTextField(
                        value = empresaSeleccionada,
                        onValueChange = {},
                        readOnly = true,
                        placeholder = { Text(stringResource(R.string.selecciona_tu_organizaci_n), fontSize = 13.sp, color = Color(0xFF888888)) },
                        trailingIcon = { Icon(Icons.Default.ArrowDropDown,
                            stringResource(R.string.dropdown)
                        ) },
                        modifier = Modifier
                            .menuAnchor()
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
                        textStyle = LocalTextStyle.current.copy(fontSize = 13.sp)
                    )
                    ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        empresas.forEach { (nombre, id) ->
                            DropdownMenuItem(
                                text = { Text(text = nombre) },
                                onClick = {
                                    empresaSeleccionada = nombre
                                    regViewModel.entidadId.value = id
                                    expanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    stringResource(R.string.nombres), fontSize = 12.sp, color = Color(0xFF888888), modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 4.dp, bottom = 4.dp))
                OutlinedTextField(
                    value = nombres,
                    onValueChange = { regViewModel.nombres.value = it },
                    placeholder = { Text(stringResource(R.string.ingresa_tus_nombres), fontSize = 13.sp, color = Color(0xFF888888)) },
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

                Text(
                    stringResource(R.string.apellidos), fontSize = 12.sp, color = Color(0xFF888888), modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 4.dp, bottom = 4.dp))
                OutlinedTextField(
                    value = apellidos,
                    onValueChange = { regViewModel.apellidos.value = it },
                    placeholder = { Text(stringResource(R.string.ingresa_tus_apellidos), fontSize = 13.sp, color = Color(0xFF888888)) },
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

                Text(
                    stringResource(R.string.contrase_a), fontSize = 12.sp, color = Color(0xFF888888), modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 4.dp, bottom = 4.dp))
                OutlinedTextField(
                    value = password,
                    onValueChange = { regViewModel.password.value = it },
                    placeholder = { Text(stringResource(R.string.ingresa_tu_contrase_a), fontSize = 13.sp, color = Color(0xFF888888)) },
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
                                painter = painterResource(id = if (passwordVisible) R.drawable.watch else R.drawable.hide),
                                contentDescription = stringResource(R.string.toggle_password),
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    singleLine = true,
                    textStyle = LocalTextStyle.current.copy(fontSize = 13.sp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    stringResource(R.string.confirma_tu_contrase_a), fontSize = 12.sp, color = Color(0xFF888888), modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 4.dp, bottom = 4.dp))
                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = { regViewModel.confirmPassword.value = it },
                    placeholder = { Text(stringResource(R.string.ingresa_tu_contrase_a_nuevamente), fontSize = 12.sp, color = Color(0xFF888888)) },
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
                                painter = painterResource(id = if (confirmPasswordVisible) R.drawable.watch else R.drawable.hide),
                                contentDescription = stringResource(R.string.toggle_password),
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    singleLine = true,
                    textStyle = LocalTextStyle.current.copy(fontSize = 13.sp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = { regViewModel.registerFuncionario() },
                    modifier = Modifier
                        .fillMaxWidth(0.85f)
                        .height(50.dp)
                        .shadow(8.dp, RoundedCornerShape(24.dp)),
                    shape = RoundedCornerShape(24.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7AC8E0))
                ) {
                    if (regState is RegState.Loading) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.Black)
                    } else {
                        Text(text = stringResource(R.string.crear_cuenta), fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = stringResource(R.string.ya_tienes_una_cuenta_inicia_sesi_n),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF000000),
                    textAlign = TextAlign.Center,
                    textDecoration = androidx.compose.ui.text.style.TextDecoration.Underline,
                    modifier = Modifier.clickable { navController.navigate(Pantallas.Login.ruta) },
                )

                Spacer(modifier = Modifier.height(8.dp))

                IconButton(onClick = { navController.popBackStack() }, modifier = Modifier.size(64.dp)) {
                    Image(
                        painter = painterResource(id = R.drawable.ellipse_41),
                        contentDescription = stringResource(R.string.regresar),
                        modifier = Modifier.size(56.dp)
                    )
                }
            }
        }

        Image(
            painter = painterResource(id = R.drawable.circular_logo),
            contentDescription = stringResource(R.string.logo_urbanfix),
            modifier = Modifier
                .size(120.dp)
                .align(Alignment.TopCenter)
                .offset(y = 30.dp)
                .zIndex(2f)
        )
    }

    when (val state = regState) {
        is RegState.Error -> {
            ErrorDialogo(
                errorMessage = state.message,
                onDismiss = { regViewModel.dismissError() }
            )
        }
        is RegState.Success -> {
            SuccessDialog(
                onDismiss = {
                    navController.navigate(Pantallas.Login.ruta) {
                        popUpTo(Pantallas.Bienvenida.ruta)
                    }
                }
            )
        }
        else -> {}
    }
}