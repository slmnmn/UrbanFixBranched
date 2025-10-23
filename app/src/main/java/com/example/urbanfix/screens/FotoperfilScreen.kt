package com.example.urbanfix.screens

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory // Importante para convertir Uri a Bitmap
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close // Para la X del Snackbar (opcional)
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.urbanfix.R
import com.example.urbanfix.data.UserPreferencesManager
import com.example.urbanfix.navigation.Pantallas
import com.example.urbanfix.viewmodel.ProfileViewModel
import com.example.urbanfix.viewmodel.ViewModelFactory
import com.example.urbanfix.viewmodel.PhotoUploadState
import java.io.File

val BlueProfile = Color(0xFF1D3557)
val RedProfile = Color(0xFFE63946)

fun uriToBitmap(context: android.content.Context, uri: Uri): Bitmap? {
    return try {
        val inputStream = context.contentResolver.openInputStream(uri)
        BitmapFactory.decodeStream(inputStream).also { inputStream?.close() }
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FotoperfilScreen(navController: NavHostController) {
    val context = LocalContext.current

    // Usa la Factory para compartir la instancia con otras pantallas si es necesario
    val viewModel: ProfileViewModel = viewModel(factory = ViewModelFactory(context))

    // --- 2. LEE ESTADOS DEL VIEWMODEL ---
    val imageBitmap by viewModel.profilePicBitmap.collectAsState() // Imagen actual
    val uploadState by viewModel.photoUploadState.collectAsState() // Estado de subida (Idle, Loading, Success, Error)

    // Estados locales solo para UI (sin cambios)
    var showDeleteDialog by remember { mutableStateOf(false) }
    var tempImageUri by remember { mutableStateOf<Uri?>(null) }
    val snackbarHostState = remember { SnackbarHostState() } // Para mostrar errores

    // --- 3. MANEJA NAVEGACIÓN Y ERRORES BASADO EN EL ESTADO DE SUBIDA ---
    LaunchedEffect(uploadState) {
        when (val state = uploadState) { // Renombramos a 'state' para claridad
            is PhotoUploadState.Success -> {
                // Éxito: Muestra mensaje, notifica pantalla anterior y vuelve
                snackbarHostState.showSnackbar(context.getString(R.string.photo_upload_success)) // Necesitas string "photo_upload_success"
                navController.previousBackStackEntry?.savedStateHandle?.set("update_success", true)
                navController.popBackStack()
                viewModel.resetPhotoUploadState() // Importante: Reinicia el estado en el VM
            }
            is PhotoUploadState.Error -> {
                // Error: Muestra el mensaje de error en un Snackbar
                snackbarHostState.showSnackbar("Error: ${state.message}")
                // Opcional: Reiniciar estado aquí si quieres que el usuario pueda reintentar
                // viewModel.resetPhotoUploadState()
            }
            else -> { /* Idle (Listo) o Loading (Cargando) - No hagas nada aquí */ }
        }
    }


    // --- 4. ACTUALIZA LOS LAUNCHERS PARA LLAMAR AL VIEWMODEL ---
    // Launcher galería
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            val bitmap = uriToBitmap(context, it)
            bitmap?.let { bm ->
                viewModel.actualizarFotoDePerfil(bm) // <-- Llama al ViewModel
            }
        }
    }

    // Launcher cámara
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && tempImageUri != null) {
            val bitmap = uriToBitmap(context, tempImageUri!!)
            bitmap?.let { bm ->
                // (Opcional: añadir rotación aquí si es necesario)
                viewModel.actualizarFotoDePerfil(bm) // <-- Llama al ViewModel
            }
        }
    }

    // Launcher permisos (Sin cambios)
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            val photoFile = File(context.cacheDir, "profile_${System.currentTimeMillis()}.jpg")
            val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", photoFile)
            tempImageUri = uri
            cameraLauncher.launch(uri)
        }
    }

    // --- UI (ESTRUCTURA GENERAL SIN CAMBIOS) ---
    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }, // Añade el host para SnackBar
        topBar = {
            TopAppBar(
                title = {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 35.dp, end = 42.dp), // Tu padding original
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = stringResource(R.string.profile_photo_title),
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                    }
                },
                navigationIcon = {
                    Box(modifier = Modifier.padding(top = 20.dp)) { // Tu padding original
                        IconButton(onClick = { navController.popBackStack() }) { // Acción sin cambios
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = stringResource(R.string.back_button_content_description),
                                tint = Color.White
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF457B9D)), // Tu color original
                modifier = Modifier.height(72.dp) // Tu altura original
            )
        },
        bottomBar = {
            BottomNavBar(navController = navController) // Sin cambios
        },
        containerColor = Color.White // Fondo blanco (como lo tenías)
    ) { paddingValues ->

        // --- Box para superponer el indicador de carga ---
        Box(modifier = Modifier.fillMaxSize()) {

            // --- Tu columna principal (sin cambios estructurales) ---
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues) // Padding del Scaffold
                    .padding(24.dp), // Tu padding interno
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Top
            ) {
                Spacer(modifier = Modifier.height(40.dp)) // Tu Spacer

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight(), // Tu modificador
                    shape = RoundedCornerShape(32.dp), // Tu shape
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F4F8)), // Tu color
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp) // Tu elevación
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp), // Tu padding
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // --- 5. ACTUALIZA LA IMAGEN PARA LEER DEL VIEWMODEL ---
                        Box(
                            modifier = Modifier
                                .size(180.dp) // Tu tamaño
                                .clip(CircleShape) // Tu shape
                                .background(Color(0xFFB0BEC5)), // Tu fondo por defecto
                            contentAlignment = Alignment.Center
                        ) {
                            if (imageBitmap != null) {
                                Image(
                                    bitmap = imageBitmap!!.asImageBitmap(), // <-- LEE DEL VIEWMODEL
                                    contentDescription = stringResource(R.string.profile_photo_description),
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop // Tu escalado
                                )
                            } else {
                                Image(
                                    painter = painterResource(id = R.drawable.sin_foto), // Tu imagen por defecto
                                    contentDescription = stringResource(R.string.profile_photo_description),
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Fit // Tu escalado
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(32.dp)) // Tu Spacer

                        // --- Botones (Estética sin cambios, solo lógica de onClick) ---
                        // Botón Tomar Foto
                        Button(
                            onClick = {
                                when (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA)) {
                                    PackageManager.PERMISSION_GRANTED -> {
                                        val photoFile = File(context.cacheDir, "profile_${System.currentTimeMillis()}.jpg")
                                        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", photoFile)
                                        tempImageUri = uri
                                        cameraLauncher.launch(uri)
                                    }
                                    else -> cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                                }
                            },
                            modifier = Modifier.fillMaxWidth().height(56.dp), // Tus modificadores
                            shape = RoundedCornerShape(16.dp), // Tu shape
                            colors = ButtonDefaults.buttonColors(containerColor = BlueProfile) // Tu color
                        ) {
                            Text(
                                text = stringResource(R.string.take_photo),
                                color = Color.White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp)) // Tu Spacer

                        // Botón Galería
                        Button(
                            onClick = { galleryLauncher.launch("image/*") }, // Llama al launcher modificado
                            modifier = Modifier.fillMaxWidth().height(56.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = BlueProfile)
                        ) {
                            Text(
                                text = stringResource(R.string.select_from_gallery),
                                color = Color.White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp)) // Tu Spacer

                        // Botón Eliminar
                        Button(
                            onClick = { if (imageBitmap != null) showDeleteDialog = true }, // Lógica sin cambios aquí
                            modifier = Modifier.fillMaxWidth().height(56.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = BlueProfile),
                            enabled = imageBitmap != null // Habilitado si hay foto (leído del VM)
                        ) {
                            Text(
                                text = stringResource(R.string.delete_photo),
                                color = Color.White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp)) // Tu Spacer

                        // Botón Cancelar
                        Button(
                            onClick = { navController.popBackStack() }, // Acción sin cambios
                            modifier = Modifier.fillMaxWidth().height(56.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = RedProfile) // Tu color
                        ) {
                            Text(
                                text = stringResource(R.string.cancel_button),
                                color = Color.White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    } // Fin Column interna de Card
                } // Fin Card
            } // Fin Column principal

            // --- 6. MUESTRA INDICADOR DE CARGA SI ESTÁ SUBIENDO ---
            if (uploadState == PhotoUploadState.Loading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        // Fondo semi-transparente opcional para mejor feedback
                        .background(Color.Black.copy(alpha = 0.3f))
                        .padding(paddingValues), // Para respetar Scaffold
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = BlueProfile) // Tu color
                }
            }

        } // Fin Box principal (para superponer loader)
    } // Fin Scaffold

    // --- 7. ACTUALIZA EL DIÁLOGO DE BORRADO PARA LLAMAR AL VIEWMODEL ---
    if (showDeleteDialog) {
        DeletePhotoConfirmationDialog(
            onConfirm = {
                showDeleteDialog = false
                viewModel.eliminarFotoDePerfil() // <-- LLAMA AL VIEWMODEL
                // La navegación/feedback ahora se maneja en LaunchedEffect
            },
            onDismiss = { showDeleteDialog = false } // Sin cambios
        )
    }
}

// --- DIÁLOGO DE CONFIRMACIÓN (SIN CAMBIOS) ---
@Composable
fun DeletePhotoConfirmationDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 1.dp),
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
                        .background(Color(0xFFFFB74D)), // Tu color
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(R.string.delete_photo_confirmation_title),
                        color = Color.Black,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Text(
                    text = stringResource(R.string.delete_photo_confirmation_message),
                    fontSize = 15.sp,
                    fontFamily = FontFamily.SansSerif,
                    color = Color.Black,
                    fontStyle = FontStyle.Italic,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp, horizontal = 24.dp) // Tu padding
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp) // Tu padding
                        .padding(bottom = 24.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp) // Tu espaciado
                ) {
                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1D3557)), // Tu color
                        shape = RoundedCornerShape(12.dp), // Tu shape
                        modifier = Modifier.weight(1f).height(48.dp) // Tus modificadores
                    ) {
                        Text(
                            text = stringResource(R.string.no_button),
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Button(
                        onClick = onConfirm, // La lógica está fuera, aquí solo se llama
                        colors = ButtonDefaults.buttonColors(containerColor = RedProfile), // Tu color
                        shape = RoundedCornerShape(12.dp), // Tu shape
                        modifier = Modifier.weight(1f).height(48.dp) // Tus modificadores
                    ) {
                        Text(
                            text = stringResource(R.string.yes_button),
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}