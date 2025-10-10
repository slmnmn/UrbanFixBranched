package com.example.urbanfix.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.urbanfix.R
import com.example.urbanfix.ui.theme.*
import com.example.urbanfix.viewmodel.CompanyProfileState
import com.example.urbanfix.viewmodel.CompanyProfileViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VerperfilusuarioScreen(
    navController: NavHostController,
    viewModel: CompanyProfileViewModel = viewModel()
) {
    val profileState by viewModel.profileState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 35.dp, end = 42.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = stringResource(R.string.perfil_consulta),
                            color = Color.White,
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
                                contentDescription = stringResource(R.string.back_button_content_description),
                                tint = Color.White
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
            is CompanyProfileState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = BlueMain)
                }
            }

            is CompanyProfileState.Success -> {
                UsuarioProfileContent(
                    paddingValues = paddingValues,
                    userName = state.companyName,
                    userEmail = state.userEmail
                )
            }

            else -> {}
        }
    }
}

@Composable
private fun UsuarioProfileContent(
    paddingValues: PaddingValues,
    userName: String,
    userEmail: String
) {
    val context = LocalContext.current
    var showEmailCopiedDialog by remember { mutableStateOf(false) }

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
                contentDescription = stringResource(R.string.profile_picture_cd),
                modifier = Modifier
                    .size(180.dp)
                    .clip(CircleShape)
                    .background(WhiteFull)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // --- NOMBRE DE USUARIO ---
        Text(
            text = userName,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )

        Spacer(modifier = Modifier.height(24.dp))

        // --- CARD DE INFORMACIÓN ---
        Card(
            shape = RoundedCornerShape(40.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF663251)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(vertical = 8.dp, horizontal = 24.dp)) {
                UserInfoRowUsuario(
                    icon = Icons.Default.Person,
                    label = stringResource(R.string.full_name_label),
                    value = "Nombres y apellidos del usuario"
                )

                Divider(color = WhiteFull.copy(alpha = 0.2f))

                // --- EMAIL ---
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .padding(vertical = 12.dp)
                        .fillMaxWidth()
                ) {
                    Icon(
                        Icons.Default.Email,
                        contentDescription = stringResource(R.string.profile_email_label),
                        tint = WhiteFull,
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            stringResource(R.string.profile_email_label),
                            color = WhiteFull,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        Text(
                            userEmail,
                            color = WhiteFull.copy(alpha = 0.8f),
                            fontSize = 14.sp,
                            textDecoration = TextDecoration.Underline
                        )
                    }
                }

                Divider(color = WhiteFull.copy(alpha = 0.2f))

                // --- TIPO DE CUENTA ---
                UserInfoRowUsuario(
                    icon = Icons.Default.AccountCircle,
                    label = stringResource(R.string.account_type_label),
                    value = stringResource(R.string.user_account_type)
                )

                Divider(color = WhiteFull.copy(alpha = 0.2f))

                // --- FECHA DE REGISTRO ---
                UserInfoRowUsuario(
                    icon = Icons.Default.DateRange,
                    label = stringResource(R.string.registration_date_label),
                    value = stringResource(R.string.registration_date_value)
                )
            }
        }

        Spacer(modifier = Modifier.height(30.dp))

        // --- BOTÓN COPIAR CORREO ---
        Button(
            onClick = {
                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val clip = ClipData.newPlainText("email", userEmail)
                clipboard.setPrimaryClip(clip)
                showEmailCopiedDialog = true
            },
            shape = RoundedCornerShape(50),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFA8DADC)),
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    stringResource(R.string.contact_button),
                    color = BlackFull,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = 12.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Image(
                    painter = painterResource(id = R.drawable.icono_contactar),
                    contentDescription = stringResource(R.string.contact_icon_description),
                    modifier = Modifier.size(32.dp)
                )
            }
        }
    }

    if (showEmailCopiedDialog) {
        EmailCopiedDialogUsuario(onDismiss = { showEmailCopiedDialog = false })
    }
}

@Composable
private fun UserInfoRowUsuario(icon: ImageVector, label: String, value: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .padding(vertical = 12.dp)
            .fillMaxWidth()
    ) {
        Icon(
            icon,
            contentDescription = label,
            tint = WhiteFull,
            modifier = Modifier.size(32.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(
                label,
                color = WhiteFull,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
            Text(
                value,
                color = WhiteFull.copy(alpha = 0.8f),
                fontSize = 14.sp
            )
        }
    }
}

@Composable
fun EmailCopiedDialogUsuario(onDismiss: () -> Unit) {
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
                        .background(Color(0xFF90BE6D)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(R.string.email_copied_title),
                        color = Color.Black,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Text(
                    text = stringResource(R.string.email_copied_message),
                    fontSize = 15.sp,
                    fontFamily = FontFamily.SansSerif,
                    color = Color.Black,
                    fontStyle = FontStyle.Italic,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp, horizontal = 24.dp)
                )

                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF1D3557)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 48.dp)
                        .padding(bottom = 24.dp)
                        .height(48.dp)
                ) {
                    Text(
                        text = stringResource(R.string.ok_button),
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
