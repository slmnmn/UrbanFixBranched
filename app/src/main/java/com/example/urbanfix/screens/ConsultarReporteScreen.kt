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
import androidx.compose.material.icons.filled.Edit
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
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.example.urbanfix.R
import com.example.urbanfix.data.UserPreferencesManager
import com.example.urbanfix.navigation.Pantallas
import com.example.urbanfix.network.ComentarioResponse
import com.example.urbanfix.network.MiReporte
import com.example.urbanfix.ui.theme.*
import com.example.urbanfix.viewmodel.EditarReporteViewModel
import com.mapbox.geojson.Point
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.MapView
import com.mapbox.maps.Style
import com.mapbox.maps.plugin.annotation.annotations
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationOptions
import com.mapbox.maps.plugin.annotation.generated.createPointAnnotationManager
import com.mapbox.maps.plugin.gestures.gestures
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.text.SimpleDateFormat
import java.util.*

@RequiresApi(Build.VERSION_CODES.O)
fun navigateToUserProfile(
    navController: NavHostController,
    userId: Int,
    isVerified: Boolean
) {
    if (isVerified) {
        navController.navigate(Pantallas.Verperfilempresa.ruta)
    } else {
        navController.navigate(Pantallas.Verperfilusuario.crearRuta(userId))
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConsultarReporteScreen(
    navController: NavHostController,
    reportId: String = ""
) {
    val context = LocalContext.current
    val reporteIdInt = reportId.toIntOrNull() ?: -1

    val viewModel: EditarReporteViewModel = viewModel(
        factory = EditarReporteViewModel.Factory(context, reporteIdInt)
    )

    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()
    val clipboardManager = LocalClipboardManager.current

    var showCopyDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var commentToDelete by remember { mutableStateOf<ComentarioResponse?>(null) }
    var showImageGallery by remember { mutableStateOf(false) }
    var commentToEdit by remember { mutableStateOf<ComentarioResponse?>(null) }
    var showEditDialog by remember { mutableStateOf(false) }
    var showOptionsDialog by remember { mutableStateOf<Int?>(null) }
    var showEstadoDialog by remember { mutableStateOf(false) }

    val currentUserId = remember { UserPreferencesManager(context).getUserId() }
    val userRole = remember { UserPreferencesManager(context).getUserRole() }
    val isFuncionario = userRole == "funcionario"

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
            when {
                uiState.isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = BlueMain)
                    }
                }

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

                uiState.reporte != null -> {
                    val reporte = uiState.reporte!!

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(scrollState)
                            .background(Color(0xFFF1FAEE))
                            .paddingFromBaseline(bottom = 180.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(420.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(180.dp)
                                    .background(Color(0xFFE8EEF5))
                            ) {
                                val lat = reporte.latitud.toDoubleOrNull() ?: 0.0
                                val lon = reporte.longitud.toDoubleOrNull() ?: 0.0

                                //INDEPENDIZAMOS LOS MAPAS. ESTE SOLO ES EL QUE APARECE EN ConsultarReporteScreen y EditarReporteScreen
                                val reportLocationPoint = Point.fromLngLat(lon, lat)

                                // Llama al nuevo componente estático
                                ReporteDetalleMapComponent(
                                    modifier = Modifier.fillMaxSize(),
                                    reportLocation = reportLocationPoint
                                )
                            }

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
                                        Text(
                                            text = reporte.categoria_nombre ?: "Sin Categoría",
                                            fontSize = 18.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White,
                                            textAlign = TextAlign.Center,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(top = 10.dp)
                                        )

                                        Spacer(modifier = Modifier.height(12.dp))

                                        reporte.descripcion?.let {
                                            Text(
                                                text = it,
                                                fontSize = 13.sp,
                                                color = Color.White,
                                                lineHeight = 18.sp,
                                                textAlign = TextAlign.Center
                                            )
                                        }

                                        Spacer(modifier = Modifier.height(10.dp))

                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(top = 4.dp, start = 4.dp, end = 4.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
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
                                                    val code = "#${reporte.id}"
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
                                                                clipboardManager.setText(
                                                                    AnnotatedString(code)
                                                                )
                                                                showCopyDialog = true
                                                            },
                                                        tint = Color.White.copy(alpha = 0.7f)
                                                    )
                                                }
                                            }
                                            if (showCopyDialog) {
                                                CodeCopiedDialog2(onDismiss = { showCopyDialog = false })
                                            }

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
                                                    onClick = { viewModel.handleReaccion("like") },
                                                    modifier = Modifier.size(28.dp)
                                                ) {
                                                    val likeActive = reporte.current_user_reaction == "like"
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
                                                            text = reporte.apoyos_count.toString(),
                                                            fontSize = 11.sp,
                                                            color = Color.White
                                                        )
                                                    }
                                                }
                                            }

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
                                                    onClick = { viewModel.handleReaccion("dislike") },
                                                    modifier = Modifier.size(28.dp)
                                                ) {
                                                    val dislikeActive = reporte.current_user_reaction == "dislike"
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
                                                            text = reporte.desapoyos_count.toString(),
                                                            fontSize = 11.sp,
                                                            color = Color.White
                                                        )
                                                    }
                                                }
                                            }

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

                                                // NUEVO: Row para imagen de estado + ícono de edición
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.Center
                                                ) {
                                                    val imagenEstado = when (reporte.estado) {
                                                        "Nuevo" -> R.drawable.nuevo
                                                        "En proceso" -> R.drawable.proceso
                                                        "Resuelto" -> R.drawable.resuelto
                                                        else -> R.drawable.nuevo
                                                    }
                                                    Box(
                                                        modifier = Modifier
                                                            .width(85.dp)
                                                            .height(26.dp),
                                                        contentAlignment = Alignment.Center
                                                    ) {
                                                        Image(
                                                            painter = painterResource(id = imagenEstado),
                                                            contentDescription = reporte.estado,
                                                            modifier = Modifier.fillMaxSize(),
                                                            contentScale = ContentScale.Fit
                                                        )
                                                    }

                                                    // Mostrar ícono de edición solo si es funcionario
                                                    if (isFuncionario) {
                                                        Spacer(modifier = Modifier.width(4.dp))
                                                        IconButton(
                                                            onClick = { showEstadoDialog = true },
                                                            modifier = Modifier.size(24.dp)
                                                        ) {
                                                            Icon(
                                                                imageVector = Icons.Default.Edit,
                                                                contentDescription = "Editar estado",
                                                                modifier = Modifier.size(16.dp),
                                                                tint = Color.White
                                                            )
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            Box(
                                modifier = Modifier
                                    .size(120.dp)
                                    .align(Alignment.Center)
                                    .offset(y = (-70).dp)
                                    .clip(CircleShape)
                                    .background(Color.White, CircleShape)
                                    .padding(4.dp)
                                    .clickable { showImageGallery = true },
                                contentAlignment = Alignment.Center
                            ) {
                                AsyncImage(
                                    model = reporte.img_prueba_1,
                                    contentDescription = stringResource(R.string.report_photo),
                                    placeholder = painterResource(id = R.drawable.prueba_circulo),
                                    error = painterResource(id = R.drawable.alerta_no_nada),
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
                                ).filter { it.isNotBlank() }
                                if (images.isNotEmpty()) {
                                    ImageGalleryDialog3(
                                        images = images,
                                        initialIndex = 0,
                                        onDismiss = { showImageGallery = false }
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(1.dp))

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp)
                                .offset(y = (-20).dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val fechaFormateada = formatearFecha(reporte.fecha_creacion)
                            Text(
                                text = "Reportado $fechaFormateada",
                                fontSize = 10.sp,
                                color = Color(0xFF333333),
                                lineHeight = 13.sp
                            )

                            Row(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.clickable {
                                    reporte.usuario_creador_id?.let { userId ->
                                        if (reporte.creador_es_verificado == true) {
                                            navController.navigate(Pantallas.Verperfilempresa.crearRuta(userId))
                                        } else {
                                            navController.navigate(Pantallas.Verperfilusuario.crearRuta(userId, "usuario"))
                                        }
                                    }
                                }
                            ) {
                                Text(
                                    text = reporte.creador_nombre ?: "Reportado por...",
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
                                        text = reporte.creador_iniciales ?: "U",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

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

                            if (uiState.comentarios.isEmpty()) {
                                Text(
                                    text = "Aún no hay comentarios. ¡Sé el primero en comentar!",
                                    fontSize = 12.sp,
                                    color = Color.Gray,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 16.dp)
                                )
                            } else {
                                uiState.comentarios.forEach { comentario ->
                                    val esUsuarioActual = comentario.usuario_id == currentUserId
                                    CommentItem2(
                                        comment = comentario,
                                        isCurrentUser = esUsuarioActual,
                                        showMenu = showOptionsDialog == comentario.id,
                                        navController = navController,
                                        onMenuClick = {
                                            showOptionsDialog = if (showOptionsDialog == comentario.id) null else comentario.id
                                        },
                                        onEditClick = { },
                                        onDeleteClick = { }
                                    )
                                    Spacer(modifier = Modifier.height(12.dp))
                                }
                            }
                        }
                    }
                }
            }

            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
            ) {
                Column {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFB76998))
                            .padding(horizontal = 12.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = uiState.nuevoComentarioTexto,
                            onValueChange = { viewModel.onNuevoComentarioChange(it) },
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
                            onClick = { viewModel.postComentario() },
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

                    BottomNavBar(navController = navController)
                }
            }
        }
    }

    // Diálogo para cambiar estado (solo para funcionarios) Cambio logica
    if (showEstadoDialog) {
        EstadoReporteDialog(
            estadoActual = uiState.reporte?.estado ?: "Nuevo",
            onDismiss = { showEstadoDialog = false },
            onEstadoSeleccionado = { nuevoEstado ->

                viewModel.updateEstadoReporte(nuevoEstado)

                showEstadoDialog = false
            }
        )
    }

    if (showEditDialog && commentToEdit != null) {
        EditCommentDialog2(
            comment = commentToEdit!!,
            onDismiss = {
                showEditDialog = false
                commentToEdit = null
            },
            onSave = { editedText ->
                viewModel.updateComentario(commentToEdit!!.id, editedText)
                showEditDialog = false
                commentToEdit = null
            }
        )
    }

    if (showOptionsDialog != null && commentToEdit == null) {
        CommentOptionsDialog2(
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
        DeleteCommentDialog2(
            onDismiss = {
                showDeleteDialog = false
                commentToDelete = null
            },
            onConfirm = {
                viewModel.deleteComentario(commentToDelete!!.id)
                showDeleteDialog = false
                commentToDelete = null
            }
        )
    }
}

@Composable
fun EstadoReporteDialog(
    estadoActual: String,
    onDismiss: () -> Unit,
    onEstadoSeleccionado: (String) -> Unit
) {
    var estadoSeleccionado by remember { mutableStateOf(estadoActual) }

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
                    Text(
                        text = stringResource(R.string.cambiar_estado_del_reporte),
                        color = Color.Black,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp, horizontal = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = stringResource(R.string.seleccione_el_estado_que_desea_proveer_a_la_situaci_n),
                        fontSize = 15.sp,
                        fontFamily = FontFamily.SansSerif,
                        color = Color.Black,
                        fontStyle = FontStyle.Italic,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // Botones de estado
                    EstadoButton(
                        texto = stringResource(R.string.resuelto),
                        seleccionado = estadoSeleccionado == "Resuelto",
                        onClick = { estadoSeleccionado = "Resuelto" }
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    EstadoButton(
                        texto = stringResource(R.string.en_proceso),
                        seleccionado = estadoSeleccionado == "En proceso",
                        onClick = { estadoSeleccionado = "En proceso" }
                    )
                }

                // Botón Aceptar
                Button(
                    onClick = { onEstadoSeleccionado(estadoSeleccionado) },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1D3557)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 48.dp)
                        .padding(bottom = 24.dp)
                        .height(48.dp)
                ) {
                    Text(
                        text = stringResource(R.string.aceptar),
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
fun EstadoButton(
    texto: String,
    seleccionado: Boolean,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (seleccionado) Color(0xFF90BE6D) else Color(0xFFE8E8E8)
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Text(
            text = texto,
            color = if (seleccionado) Color.White else Color.Black,
            fontSize = 16.sp,
            fontWeight = if (seleccionado) FontWeight.Bold else FontWeight.Normal
        )
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun CommentItem2(
    comment: ComentarioResponse,
    isCurrentUser: Boolean,
    showMenu: Boolean,
    navController: NavHostController,
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
                        if (comment.es_verificado) Color(0xFFB76998)
                        else if (isCurrentUser) Color(0xFFA8DADC)
                        else Color(0xFFDEB6D1)
                    )
                    .clickable {
                        if (comment.es_verificado) {
                            navController.navigate(Pantallas.Verperfilempresa.crearRuta(comment.usuario_id))
                        } else {
                            navController.navigate(
                                Pantallas.Verperfilusuario.crearRuta(comment.usuario_id, "usuario")
                            )
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = comment.autor_iniciales,
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
                        text = comment.autor_nombre,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isCurrentUser) Color.White else BlackFull,
                        modifier = Modifier.clickable {
                            if (comment.es_verificado) {
                                navController.navigate(Pantallas.Verperfilempresa.crearRuta(comment.usuario_id))
                            } else {
                                navController.navigate(
                                    Pantallas.Verperfilusuario.crearRuta(comment.usuario_id, "usuario")
                                )
                            }
                        }
                    )

                    if (comment.es_verificado) {
                        Icon(
                            painter = painterResource(id = R.drawable.check),
                            contentDescription = stringResource(R.string.verified_description),
                            modifier = Modifier.size(14.dp),
                            tint = Color(0xFF00BCD4)
                        )
                    }

                    Spacer(modifier = Modifier.width(4.dp))

                    Text(
                        text = comment.fecha_creacion,
                        fontSize = 11.sp,
                        color = if (isCurrentUser) Color(0xFFCCCCCC) else Color(0xFF999999)
                    )

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
                    text = comment.texto,
                    fontSize = 12.sp,
                    color = if (isCurrentUser) Color.White else Color.Black
                )
            }
        }
    }
}

@Composable
fun CommentOptionsDialog2(
    onDismiss: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
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
fun EditCommentDialog2(
    comment: ComentarioResponse,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit
) {
    var editedText by remember { mutableStateOf(comment.texto) }

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
fun CodeCopiedDialog2(onDismiss: () -> Unit) {
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
fun DeleteCommentDialog2(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
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
fun ImageGalleryDialog3(
    images: List<String>,
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
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1.3f)
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    AsyncImage(
                        model = images[currentIndex],
                        contentDescription = stringResource(R.string.image_description),
                        placeholder = painterResource(id = R.drawable.prueba_circulo),
                        error = painterResource(id = R.drawable.alerta_no_nada),
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(16.dp)),
                        contentScale = ContentScale.Crop
                    )
                }

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
@Composable
fun ReporteDetalleMapComponent(
    modifier: Modifier = Modifier,
    reportLocation: Point
) {
    AndroidView(
        factory = { context ->
            val mapView = MapView(context)
            val mapboxMap = mapView.getMapboxMap()

            // 1. Deshabilita TODOS los gestos del mapa
            // Esto es crucial para que funcione bien dentro de un Column con scroll
            mapView.gestures.apply {
                scrollEnabled = false
                rotateEnabled = false
                pitchEnabled = false
            }

            // 2. Centra la cámara en la ubicación del reporte
            mapboxMap.setCamera(
                CameraOptions.Builder()
                    .center(reportLocation)
                    .zoom(15.0) // Un zoom más cercano
                    .build()
            )

            // 3. Carga el estilo
            mapboxMap.loadStyleUri(Style.MAPBOX_STREETS) { style ->

                // 4. Añade un marcador (anotación) en la ubicación
                val annotationApi = mapView.annotations
                val pointAnnotationManager = annotationApi.createPointAnnotationManager()

                val pointAnnotationOptions = PointAnnotationOptions()
                    .withPoint(reportLocation)
                // Usará el marcador rojo por defecto de Mapbox

                pointAnnotationManager.create(pointAnnotationOptions)
            }

            mapView // Devuelve el MapView
        },
        update = { mapView ->
            // 5. Se asegura de que la cámara se actualice si el Point cambia
            val mapboxMap = mapView.getMapboxMap()
            mapboxMap.setCamera(
                CameraOptions.Builder()
                    .center(reportLocation)
                    .zoom(15.0)
                    .build()
            )

            // 6. Limpia marcadores viejos y añade el nuevo
            val annotationApi = mapView.annotations
            val pointAnnotationManager = annotationApi.createPointAnnotationManager()
            pointAnnotationManager.deleteAll() // Limpia

            val pointAnnotationOptions = PointAnnotationOptions()
                .withPoint(reportLocation)
            pointAnnotationManager.create(pointAnnotationOptions) // Crea el nuevo
        },
        modifier = modifier
    )
}