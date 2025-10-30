package com.example.urbanfix.screens

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import coil.compose.AsyncImage // <-- ¡IMPORTANTE! Para cargar imágenes de S3
import com.example.urbanfix.R
import com.example.urbanfix.data.UserPreferencesManager // <-- Necesaria para el Factory
import com.example.urbanfix.navigation.Pantallas // <-- Asegúrate de que esta importación sea correcta
import com.example.urbanfix.network.ComentarioResponse
import com.example.urbanfix.network.MiReporte // <-- Asegúrate de que este sea tu data class
import com.example.urbanfix.ui.theme.*
import com.example.urbanfix.viewmodel.EditarReporteViewModel
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

// El 'data class Comment' local se elimina ya que usaremos ComentarioResponse

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditarReporteScreen(
    navController: NavHostController,
    reportId: String = "" // El ID se recibe como String de la navegación
) {
    val context = LocalContext.current

    // Convertimos el ID a Int. Si falla, usamos -1
    val reporteIdInt = reportId.toIntOrNull() ?: -1

    // Inicializamos el ViewModel con su Factory
    val viewModel: EditarReporteViewModel = viewModel(
        factory = EditarReporteViewModel.Factory(context, reporteIdInt)
    )

    // Obtenemos el estado completo de la UI desde el ViewModel
    val uiState by viewModel.uiState.collectAsState()

    val scrollState = rememberScrollState()
    val clipboardManager = LocalClipboardManager.current

    // Estados para controlar los diálogos
    var showCopyDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var commentToDelete by remember { mutableStateOf<ComentarioResponse?>(null) }
    var showImageGallery by remember { mutableStateOf(false) }
    var commentToEdit by remember { mutableStateOf<ComentarioResponse?>(null) }
    var showEditDialog by remember { mutableStateOf(false) }
    var showOptionsDialog by remember { mutableStateOf<Int?>(null) } // Almacena el ID del comentario

    // Obtenemos el ID del usuario actual para la lógica de la UI (saber si un comentario es nuestro)
    val currentUserId = remember { UserPreferencesManager(context).getUserId() }

    Scaffold(
        topBar = {
            // Tu TopAppBar (sin cambios)
            TopAppBar(
                title = {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 35.dp, end = 42.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = stringResource(R.string.report_details_title),
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
        containerColor = Color(0xFFF1FAEE)
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {

            // --- Manejo de Estados (Carga, Error, Éxito) ---
            when {
                // --- ESTADO DE CARGA ---
                uiState.isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = BlueMain)
                    }
                }

                // --- ESTADO DE ERROR ---
                uiState.error != null -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = uiState.error!!,
                            color = Color.Red,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }

                // --- ESTADO DE ÉXITO ---
                uiState.reporte != null -> {
                    val reporte = uiState.reporte!! // Tenemos un reporte, ¡a dibujar!

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(scrollState)
                            .background(Color(0xFFF1FAEE))
                            .paddingFromBaseline(bottom = 180.dp) // Deja espacio para el BottomBar y el campo de comentario
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(420.dp)
                        ) {
                            // --- Mapa ---
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(180.dp)
                                    .background(Color(0xFFE8EEF5))
                            ) {
                                // TODO: Pasar la latitud y longitud del reporte al mapa
                                val lat = reporte.latitud.toDoubleOrNull() ?: 0.0
                                val lon = reporte.longitud.toDoubleOrNull() ?: 0.0
                                MapboxMapComponent(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .nestedScroll(connection = object : NestedScrollConnection {}),
                                    hasPermission = true,
                                    selectedLocation = null, // Debería ser LatLng(lat, lon)
                                    onMapReady = {},
                                    onLocationSelected = {}
                                )
                            }

                            // --- Card de Información ---
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .align(Alignment.BottomCenter)
                                    .padding(horizontal = 16.dp)
                                    .offset(y = (-30).dp),
                                shape = RoundedCornerShape(16.dp),
                                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF1D3557))
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp)
                                ) {
                                    Column(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        // --- Título (Categoría) ---
                                        Text(
                                            text = reporte.categoria_nombre ?: "Sin Categoría", // DATO REAL
                                            fontSize = 18.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White,
                                            textAlign = TextAlign.Center,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(top = 10.dp)
                                        )

                                        Spacer(modifier = Modifier.height(12.dp))

                                        // --- Descripción ---
                                        reporte.descripcion?.let {
                                            Text(
                                                text = it, // DATO REAL
                                                fontSize = 13.sp,
                                                color = Color.White,
                                                lineHeight = 18.sp,
                                                textAlign = TextAlign.Center
                                            )
                                        }

                                        Spacer(modifier = Modifier.height(10.dp))

                                        // --- Fila de Acciones (ID, Like, Dislike, Estado) ---
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(top = 4.dp, start = 4.dp, end = 4.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            // --- ID del Reporte ---
                                            Column(
                                                horizontalAlignment = Alignment.CenterHorizontally,
                                                modifier = Modifier.offset(y = -2.dp)
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .width(100.dp)
                                                        .height(2.dp)
                                                        .background(Color.White.copy(alpha = 0.6f))
                                                )
                                                Spacer(modifier = Modifier.height(6.dp))
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                                ) {
                                                    val code = "#${reporte.id}" // DATO REAL
                                                    Text(
                                                        text = code,
                                                        fontSize = 12.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = Color.White
                                                    )
                                                    Icon(
                                                        painter = painterResource(id = R.drawable.copiar_code),
                                                        contentDescription = stringResource(R.string.copy_code),
                                                        modifier = Modifier
                                                            .size(18.dp)
                                                            .clickable {
                                                                clipboardManager.setText(AnnotatedString(code))
                                                                showCopyDialog = true
                                                            },
                                                        tint = Color.White.copy(alpha = 0.7f)
                                                    )
                                                }
                                            }
                                            if (showCopyDialog) {
                                                CodeCopiedDialog(onDismiss = { showCopyDialog = false })
                                            }

                                            // --- Botón de Like ---
                                            Column(
                                                horizontalAlignment = Alignment.CenterHorizontally
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .width(40.dp)
                                                        .height(2.dp)
                                                        .background(Color.White.copy(alpha = 0.6f))
                                                )
                                                Spacer(modifier = Modifier.height(6.dp))
                                                IconButton(
                                                    onClick = { viewModel.handleReaccion("like") }, // ACCIÓN REAL
                                                    modifier = Modifier.size(28.dp)
                                                ) {
                                                    val likeActive = reporte.current_user_reaction == "like" // DATO REAL
                                                    Row(
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                                                    ) {
                                                        Icon(
                                                            painter = painterResource(
                                                                id = if (likeActive) R.drawable.like_relleno else R.drawable.sin_like
                                                            ),
                                                            contentDescription = stringResource(R.string.like_button),
                                                            modifier = Modifier.size(16.dp),
                                                            tint = if (likeActive) Color.White else Color.White.copy(
                                                                alpha = 0.5f
                                                            )
                                                        )
                                                        Text(
                                                            text = reporte.apoyos_count.toString(), // DATO REAL
                                                            fontSize = 11.sp,
                                                            color = Color.White
                                                        )
                                                    }
                                                }
                                            }

                                            // --- Botón de Dislike ---
                                            Column(
                                                horizontalAlignment = Alignment.CenterHorizontally
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .width(40.dp)
                                                        .height(2.dp)
                                                        .background(Color.White.copy(alpha = 0.6f))
                                                )
                                                Spacer(modifier = Modifier.height(6.dp))
                                                IconButton(
                                                    onClick = { viewModel.handleReaccion("dislike") }, // ACCIÓN REAL
                                                    modifier = Modifier.size(28.dp)
                                                ) {
                                                    val dislikeActive = reporte.current_user_reaction == "dislike" // DATO REAL
                                                    Row(
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                                                    ) {
                                                        Icon(
                                                            painter = painterResource(
                                                                id = if (dislikeActive) R.drawable.dislike_relleno else R.drawable.no_like
                                                            ),
                                                            contentDescription = stringResource(R.string.dislike_button),
                                                            modifier = Modifier.size(16.dp),
                                                            tint = if (dislikeActive) Color.White else Color.White.copy(
                                                                alpha = 0.3f
                                                            )
                                                        )
                                                        Text(
                                                            text = reporte.desapoyos_count.toString(), // DATO REAL
                                                            fontSize = 11.sp,
                                                            color = Color.White
                                                        )
                                                    }
                                                }
                                            }

                                            // --- Estado del Reporte ---
                                            Column(
                                                horizontalAlignment = Alignment.CenterHorizontally,
                                                modifier = Modifier.offset(y = 4.dp)
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .width(90.dp)
                                                        .height(2.dp)
                                                        .background(Color.White.copy(alpha = 0.6f))
                                                )
                                                Spacer(modifier = Modifier.height(8.dp))
                                                // Lógica de color y texto de estado
                                                val (statusColor, statusText) = when (reporte.estado) { // DATO REAL
                                                    "Nuevo" -> Color(0xFF4AB7B6) to "Nuevo"
                                                    "En proceso" -> Color(0xFFFFB800) to "En Proceso"
                                                    "Resuelto" -> Color(0xFF90BE6D) to "Resuelto"
                                                    else -> Color.Gray to (reporte.estado ?: "N/A")
                                                }
                                                Text(
                                                    text = statusText,
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color.Black,
                                                    modifier = Modifier
                                                        .background(
                                                            statusColor,
                                                            RoundedCornerShape(6.dp)
                                                        )
                                                        .padding(
                                                            start = 10.dp,
                                                            top = 4.dp,
                                                            end = 10.dp,
                                                            bottom = 6.dp
                                                        )
                                                )
                                            }
                                        }
                                    }

                                    // --- Botón de Editar ---
                                    Button(
                                        onClick = {
                                            navController.navigate("reportar/${reporte.categoria_nombre}")
                                        }, // Lógica original
                                        modifier = Modifier
                                            .align(Alignment.TopEnd)
                                            .height(32.dp),
                                        shape = RoundedCornerShape(6.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = Color(0xFFCCCCCC)
                                        ),
                                        contentPadding = PaddingValues(
                                            horizontal = 12.dp,
                                            vertical = 4.dp
                                        )
                                    ) {
                                        Text(
                                            text = stringResource(R.string.edit_button),
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.Black
                                        )
                                    }
                                }
                            }

                            // --- Imagen Principal del Reporte (Círculo) ---
                            Box(
                                modifier = Modifier
                                    .size(120.dp)
                                    .align(Alignment.Center)
                                    .offset(y = (-50).dp)
                                    .clip(CircleShape)
                                    .background(Color.White, CircleShape)
                                    .padding(4.dp)
                                    .clickable { showImageGallery = true },
                                contentAlignment = Alignment.Center
                            ) {
                                AsyncImage(
                                    model = reporte.img_prueba_1, // DATO REAL (S3 URL)
                                    contentDescription = stringResource(R.string.report_photo),
                                    placeholder = painterResource(id = R.drawable.prueba_circulo), // Placeholder
                                    error = painterResource(id = R.drawable.alerta_no_nada), // Imagen en caso de error
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(CircleShape),
                                    contentScale = ContentScale.Crop
                                )
                            }

                            if (showImageGallery) {
                                val images = listOfNotNull(
                                    reporte.img_prueba_1,
                                    reporte.img_prueba_2
                                ).filter { it.isNotBlank() } // DATO REAL
                                if (images.isNotEmpty()) {
                                    ImageGalleryDialog(
                                        images = images, // Pasa la lista de URLs (String)
                                        initialIndex = 0,
                                        onDismiss = { showImageGallery = false }
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(1.dp))

                        // --- Información del Creador ---
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp)
                                .offset(y = (-20).dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Reportado ${reporte.fecha_creacion}\"", // DATO REAL
                                fontSize = 10.sp,
                                color = Color(0xFF333333),
                                lineHeight = 13.sp
                            )

                            Row(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = reporte.creador_nombre ?: "Reportado por...", // DATO REAL (parcial)
                                    fontSize = 10.sp,
                                    color = Color(0xFF333333),
                                    textDecoration = TextDecoration.Underline
                                )
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFFB76998)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = reporte.creador_iniciales ?: "U", // DATO REAL (parcial)
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // --- Sección de Comentarios ---
                        Column(
                            modifier = Modifier.padding(horizontal = 16.dp)
                        ) {
                            Text(
                                text = stringResource(R.string.comments_label),
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = BlackFull
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            // Lista de comentarios dinámica
                            if (uiState.comentarios.isEmpty()) {
                                Text(
                                    text = "Aún no hay comentarios. ¡Sé el primero en comentar!",
                                    fontSize = 12.sp,
                                    color = Color.Gray,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp)
                                )
                            } else {
                                uiState.comentarios.forEach { comentario ->
                                    val esUsuarioActual = comentario.usuario_id == currentUserId
                                    CommentItem(
                                        comment = comentario,
                                        isCurrentUser = esUsuarioActual,
                                        showMenu = showOptionsDialog == comentario.id,
                                        onMenuClick = {
                                            showOptionsDialog = if (showOptionsDialog == comentario.id) null else comentario.id
                                        },
                                        onEditClick = { /* La lógica se maneja en el diálogo de opciones */ },
                                        onDeleteClick = { /* La lógica se maneja en el diálogo de opciones */ }
                                    )
                                    Spacer(modifier = Modifier.height(12.dp))
                                }
                            }
                        }
                    }
                }
            }

            // --- Caja para Escribir Comentario y Bottom Nav (Fijos abajo) ---
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
            ) {
                Column {
                    // --- Campo de texto para nuevo comentario ---
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFB76998))
                            .padding(horizontal = 12.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = uiState.nuevoComentarioTexto, // Conectado al ViewModel
                            onValueChange = { viewModel.onNuevoComentarioChange(it) }, // Conectado al ViewModel
                            placeholder = {
                                Text(
                                    text = stringResource(R.string.add_comment_placeholder),
                                    fontSize = 12.sp,
                                    color = Color(0xFF999999)
                                )
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(56.dp)
                                .padding(vertical = 2.dp),
                            shape = RoundedCornerShape(24.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                unfocusedContainerColor = Color.White,
                                focusedContainerColor = Color.White,
                                unfocusedBorderColor = Color.Transparent,
                                focusedBorderColor = Color.Transparent
                            ),
                            singleLine = true
                        )

                        IconButton(
                            onClick = { viewModel.postComentario() }, // Conectado al ViewModel
                            modifier = Modifier
                                .size(56.dp)
                                .background(
                                    if (uiState.nuevoComentarioTexto.isNotBlank()) Color(0xFF8B4A6F) else Color(
                                        0xFFB76998
                                    ),
                                    CircleShape
                                ),
                            enabled = uiState.nuevoComentarioTexto.isNotBlank()
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.icono_enviar_comentario),
                                contentDescription = stringResource(R.string.send_comment),
                                modifier = Modifier.size(28.dp),
                                tint = Color.White
                            )
                        }
                    }

                    // --- Tu Bottom Nav Bar ---
                    BottomNavBar(navController = navController)
                }
            }
        }
    }

    // --- DIÁLOGOS (Conectados al ViewModel y al Estado) ---

    if (showEditDialog && commentToEdit != null) {
        EditCommentDialog(
            comment = commentToEdit!!, // Pasa el ComentarioResponse
            onDismiss = {
                showEditDialog = false
                commentToEdit = null
            },
            onSave = { editedText ->
                viewModel.updateComentario(commentToEdit!!.id, editedText) // ACCIÓN REAL
                showEditDialog = false
                commentToEdit = null
            }
        )
    }

    if (showOptionsDialog != null && commentToEdit == null) {
        CommentOptionsDialog(
            onDismiss = { showOptionsDialog = null },
            onEdit = {
                val comment = uiState.comentarios.find { it.id == showOptionsDialog }
                if (comment != null) {
                    commentToEdit = comment
                    showEditDialog = true
                    showOptionsDialog = null
                }
            },
            onDelete = {
                commentToDelete = uiState.comentarios.find { it.id == showOptionsDialog }
                showDeleteDialog = true
                showOptionsDialog = null
            }
        )
    }

    if (showDeleteDialog && commentToDelete != null) {
        DeleteCommentDialog(
            onDismiss = {
                showDeleteDialog = false
                commentToDelete = null
            },
            onConfirm = {
                viewModel.deleteComentario(commentToDelete!!.id) // ACCIÓN REAL
                showDeleteDialog = false
                commentToDelete = null
            }
        )
    }
}

// --- COMPOSABLES HIJOS MODIFICADOS ---

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun CommentItem(
    comment: ComentarioResponse, // Recibe el modelo de red
    isCurrentUser: Boolean,
    showMenu: Boolean,
    onMenuClick: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = if (isCurrentUser) Color(0xFF2C3E50) else Color.Transparent,
                shape = RoundedCornerShape(12.dp)
            )
            .padding(
                horizontal = if (isCurrentUser) 12.dp else 0.dp,
                vertical = if (isCurrentUser) 10.dp else 0.dp
            )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(
                        // Lógica de color original
                        if (comment.es_verificado) Color(0xFFB76998)
                        else if (isCurrentUser) Color(0xFFA8DADC)
                        else Color(0xFFDEB6D1)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = comment.autor_iniciales, // DATO REAL
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isCurrentUser && !comment.es_verificado) Color.Black else Color.White
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = comment.autor_nombre, // DATO REAL
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isCurrentUser) Color.White else BlackFull
                    )

                    if (comment.es_verificado) { // DATO REAL
                        Icon(
                            painter = painterResource(id = R.drawable.check),
                            contentDescription = stringResource(R.string.verified_description),
                            modifier = Modifier.size(14.dp),
                            tint = Color(0xFF00BCD4)
                        )
                    }

                    Spacer(modifier = Modifier.width(4.dp))

                    Text(
                        text = comment.fecha_creacion, // DATO REAL
                        fontSize = 11.sp,
                        color = if (isCurrentUser) Color(0xFFCCCCCC) else Color(0xFF999999)
                    )

                    Spacer(modifier = Modifier.weight(1f)) // Empuja el icono al final

                    if (isCurrentUser) {
                        IconButton(
                            onClick = onMenuClick,
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.edit_comentario),
                                contentDescription = stringResource(R.string.options_description),
                                modifier = Modifier.size(16.dp),
                                tint = Color.White
                            )
                        }
                    }
                }
                Text(
                    text = comment.texto, // DATO REAL
                    fontSize = 12.sp,
                    color = if (isCurrentUser) Color.White else Color.Black
                )
            }
        }
    }
}

// --- DIÁLOGOS (Tu código original, pero usando los nuevos modelos de datos) ---

@Composable
fun CommentOptionsDialog(
    onDismiss: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    // Este Composable no necesita cambios, tu lógica original está perfecta.
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(3.dp)
                .shadow(
                    elevation = 12.dp,
                    shape = RoundedCornerShape(2.dp),
                    clip = false
                ),
            shape = RoundedCornerShape(2.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFFFF)),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                // Header
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFE0F5E1))
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(R.string.comment_options_title),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                Divider(color = Color(0xFFE0E0E0), thickness = 1.dp)
                // Opción Editar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onEdit() }
                        .padding(horizontal = 20.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.icono_edit_coment),
                        contentDescription = stringResource(R.string.edit_description),
                        modifier = Modifier
                            .size(28.dp)
                            .background(Color(0xFFE0F5E1), RoundedCornerShape(8.dp))
                            .padding(4.dp),
                        tint = Color(0xFF000000)
                    )
                    Text(
                        text = stringResource(R.string.edit_option),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.Black
                    )
                }
                Divider(color = Color(0xFFE0E0E0), thickness = 1.dp)
                // Opción Eliminar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onDelete() }
                        .padding(horizontal = 20.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.icono_elim_coment),
                        contentDescription = stringResource(R.string.delete_description),
                        modifier = Modifier
                            .size(28.dp)
                            .background(Color(0xFFE0F5E1), RoundedCornerShape(8.dp))
                            .padding(4.dp),
                        tint = Color(0xFF000000)
                    )
                    Text(
                        text = stringResource(R.string.delete_option),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF000000)
                    )
                }
                Divider(color = Color(0xFFE0E0E0), thickness = 1.dp)
                // Botón Volver
                Button(
                    onClick = onDismiss,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1D3557))
                ) {
                    Text(
                        text = stringResource(R.string.back_button),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                }
            }
        }
    }
}

@Composable
fun EditCommentDialog(
    comment: ComentarioResponse, // Recibe el ComentarioResponse
    onDismiss: () -> Unit,
    onSave: (String) -> Unit
) {
    var editedText by remember { mutableStateOf(comment.texto) } // Inicializa con el texto actual

    // El resto de tu Composable EditCommentDialog es idéntico
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            shape = RoundedCornerShape(20.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF3FAF3))
        ) {
            Column(
                modifier = Modifier
                    .padding(horizontal = 20.dp, vertical = 24.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                Text(
                    text = stringResource(R.string.edit_comment_title),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF2F4F4F),
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
                OutlinedTextField(
                    value = editedText,
                    onValueChange = { editedText = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(2.dp, shape = RoundedCornerShape(12.dp)),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedContainerColor = Color.White,
                        focusedContainerColor = Color.White,
                        unfocusedBorderColor = Color(0xFFE0E0E0),
                        focusedBorderColor = Color(0xFF4F7D94),
                        cursorColor = Color(0xFF4F7D94)
                    ),
                    textStyle = LocalTextStyle.current.copy(fontSize = 14.sp, color = Color.Black),
                    placeholder = {
                        Text(
                            text = stringResource(R.string.comment_placeholder),
                            fontSize = 14.sp,
                            color = Color(0xFF999999)
                        )
                    },
                    minLines = 3
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, Color(0xFFB0BEC5)),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = Color.Gray,
                            contentColor = Color(0xFFFFFFFF)
                        )
                    ) {
                        Text(stringResource(R.string.cancel_button))
                    }
                    Button(
                        onClick = { onSave(editedText) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF4F7D94),
                            contentColor = Color.White,
                            disabledContainerColor = Color(0xFFB0BEC5)
                        ),
                        enabled = editedText.isNotBlank()
                    ) {
                        Text(stringResource(R.string.save_button))
                    }
                }
            }
        }
    }
}

@Composable
fun CodeCopiedDialog(onDismiss: () -> Unit) {
    // Este Composable no necesita cambios, tu lógica original está perfecta.
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .background(Color(0xFF90BE6D)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(R.string.code_copied_title),
                        color = Color.Black,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Text(
                    text = stringResource(R.string.code_copied_message),
                    fontSize = 15.sp,
                    fontFamily = FontFamily.SansSerif,
                    color = Color.Black,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 28.dp, horizontal = 24.dp)
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
                        text = stringResource(R.string.accept_button),
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun DeleteCommentDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    // Este Composable no necesita cambios, tu lógica original está perfecta.
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp)
                        .background(Color(0xFFE63946)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(R.string.delete_comment_title),
                        color = Color.Black,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }
                Text(
                    text = stringResource(R.string.delete_warning),
                    fontSize = 15.sp,
                    fontFamily = FontFamily.SansSerif,
                    color = Color.Black,
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 28.dp, horizontal = 24.dp)
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                        .padding(bottom = 24.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF1D3557)
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.back_button),
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Button(
                        onClick = onConfirm,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFE63946)
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.delete_button),
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

@Composable
fun ImageGalleryDialog(
    images: List<String>, // AHORA RECIBE UNA LISTA DE URLs (String)
    initialIndex: Int = 0,
    onDismiss: () -> Unit
) {
    var currentIndex by remember { mutableStateOf(initialIndex) }
    var isLeftPressed by remember { mutableStateOf(false) }
    var isRightPressed by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .background(Color.White, RoundedCornerShape(24.dp))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
            ) {
                // Contenedor de la imagen
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1.3f)
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    // --- CAMBIO PRINCIPAL: USA AsyncImage ---
                    AsyncImage(
                        model = images[currentIndex], // Carga la URL
                        contentDescription = stringResource(R.string.image_description),
                        placeholder = painterResource(id = R.drawable.prueba_circulo),
                        error = painterResource(id = R.drawable.alerta_no_nada),
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(16.dp)),
                        contentScale = ContentScale.Crop
                    )
                }

                // Botón Volver
                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF1D3557)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 16.dp)
                        .height(48.dp)
                ) {
                    Text(
                        text = stringResource(R.string.back_button),
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Flechas de navegación (solo si hay más de una imagen)
            if (images.size > 1) {
                IconButton(
                    onClick = {
                        currentIndex = if (currentIndex > 0) currentIndex - 1 else images.size - 1
                    },
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .padding(start = 8.dp)
                        .size(56.dp)
                        .offset(y = (-40).dp)
                        .background(
                            color = if (isLeftPressed) {
                                Color.Black.copy(alpha = 0.7f)
                            } else {
                                Color.Black.copy(alpha = 0.3f)
                            },
                            shape = CircleShape
                        )
                        .pointerInput(Unit) {
                            detectTapGestures(
                                onPress = {
                                    isLeftPressed = true
                                    tryAwaitRelease()
                                    isLeftPressed = false
                                }
                            )
                        }
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.previous_description),
                        modifier = Modifier.size(32.dp),
                        tint = Color.White
                    )
                }
                IconButton(
                    onClick = {
                        currentIndex = if (currentIndex < images.size - 1) currentIndex + 1 else 0
                    },
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .padding(end = 8.dp)
                        .offset(y = (-38).dp)
                        .size(56.dp)
                        .background(
                            color = if (isRightPressed) {
                                Color.Black.copy(alpha = 0.7f)
                            } else {
                                Color.Black.copy(alpha = 0.3f)
                            },
                            shape = CircleShape
                        )
                        .pointerInput(Unit) {
                            detectTapGestures(
                                onPress = {
                                    isRightPressed = true
                                    tryAwaitRelease()
                                    isRightPressed = false
                                }
                            )
                        }
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = stringResource(R.string.next_description),
                        modifier = Modifier.size(32.dp),
                        tint = Color.White
                    )
                }
            }
        }
    }
}