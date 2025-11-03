package com.example.urbanfix.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.navigation.NavHostController
import com.example.urbanfix.R
import com.example.urbanfix.data.UserPreferencesManager
import com.example.urbanfix.navigation.Pantallas
import com.example.urbanfix.ui.theme.*

// --- IMPORTACIONES AÑADIDAS ---
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage // Importante para cargar imágenes de URL
import com.example.urbanfix.data.ReportesRepository
import com.example.urbanfix.network.MiReporte // Usamos el modelo de red
import com.example.urbanfix.network.RetrofitInstance
import com.example.urbanfix.viewmodel.ReportesUiState
import com.example.urbanfix.viewmodel.VerReportesViewModel
import com.example.urbanfix.viewmodel.VerReportesViewModelFactory
import java.util.Locale

// --- Data class ReportePublico ELIMINADA ---
// Ya no usamos la data class estática

// --- FUNCIONES HELPER PARA UI ---

// Función para obtener el ícono de categoría según el nombre
fun obtenerIconoCategoria(categoriaNombre: String?): Int {
    return when (categoriaNombre) {
        "Alcantarilla" -> R.drawable.ciralcantarilla
        "Semaforo" -> R.drawable.cirsemaforo
        "Alumbrado" -> R.drawable.ciralumbrado
        "Basura" -> R.drawable.cirbasura
        "Hueco" -> R.drawable.circarro
        "Hidrante" -> R.drawable.cirhidrante
        else -> R.drawable.circarro // Default
    }
}

// Función para obtener el color de fondo según el nombre
fun obtenerColorCategoria(categoriaNombre: String?): Color {
    return when (categoriaNombre) {
        "Alumbrado" -> Color(0xFF663251)
        "Hueco" -> Color(0xFF4AB7B6)
        "Semaforo" -> Color(0xFF1D3557)
        "Basura" -> Color(0xFFE63946)
        "Alcantarilla" -> Color(0xFF663251)
        "Hidrante" -> Color(0xFF457B9D)
        else -> Color(0xFF663251)
    }
}

// Función para saber si el color de fondo es claro
fun esColorClaro(categoriaNombre: String?): Boolean {
    return when (categoriaNombre) {
        "Hueco" -> true
        else -> false
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VerReportesScreen(
    navController: NavHostController
) {
    val context = LocalContext.current
    var mostrarFiltro by remember { mutableStateOf(false) }
    var tipoReporteFiltro by remember { mutableStateOf<String?>(null) }
    var estadoReporteFiltro by remember { mutableStateOf<String?>(null) }
    var reporteSeleccionado by remember { mutableStateOf<MiReporte?>(null) }

    // --- OBTENER DATOS DEL USUARIO ---
    val userPreferencesManager = remember { UserPreferencesManager(context) }
    val userId = remember { userPreferencesManager.getUserId().let { if (it == -1) null else it } }
    val userRole = remember { userPreferencesManager.getUserRole() }

    // --- INYECTAR VIEWMODEL ---
    val viewModel: VerReportesViewModel = viewModel(
        factory = VerReportesViewModelFactory(
            repository = ReportesRepository(RetrofitInstance.api),
            userId = userId,
            userRole = userRole
        )
    )

    // --- RECOLECTAR ESTADO DE LA UI ---
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Reportes",
                        color = WhiteFull,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 35.dp)
                    )
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
                actions = {
                    Box(modifier = Modifier.padding(top = 20.dp, end = 8.dp)) {
                        IconButton(onClick = { mostrarFiltro = true }) {
                            Image(
                                painter = painterResource(id = R.drawable.filtrar),
                                contentDescription = "Filtrar",
                                modifier = Modifier.size(24.dp)
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
            BottomNavBarThreee(navController = navController)
        },
        containerColor = GrayBg
    ) { paddingValues ->

        // --- MANEJAR LOS ESTADOS DE LA UI ---
        when (val state = uiState) {
            is ReportesUiState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = BlueMain)
                }
            }
            is ReportesUiState.Error -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Error al cargar los reportes.",
                            color = Color.Gray,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(onClick = { viewModel.fetchReportes() }) {
                            Text("Reintentar")
                        }
                    }
                }
            }
            is ReportesUiState.Success -> {
                // Filtramos la lista del ViewModel
                val reportesFiltrados = remember(tipoReporteFiltro, estadoReporteFiltro, state.reportes) {
                    state.reportes.filter { reporte ->
                        val coincideTipo = tipoReporteFiltro == null || reporte.categoria_nombre == tipoReporteFiltro
                        val coincideEstado = estadoReporteFiltro == null || reporte.estado == estadoReporteFiltro
                        coincideTipo && coincideEstado
                    }
                }

                if (reportesFiltrados.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier.padding(32.dp)
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.alerta_no_nada),
                                contentDescription = stringResource(R.string.no_reportes),
                                modifier = Modifier.size(120.dp)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = if (tipoReporteFiltro == null && estadoReporteFiltro == null) "No hay reportes públicos disponibles"
                                else "No se encontraron reportes con los filtros aplicados",
                                fontSize = 14.sp,
                                color = Color.Gray,
                                textAlign = TextAlign.Center,
                                lineHeight = 20.sp
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues)
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(reportesFiltrados, key = { it.id }) { reporte ->
                            ReportePublicoCard(
                                reporte = reporte, // Pasamos el modelo MiReporte
                                context = context,
                                onLikeClick = {
                                    viewModel.handleReaccion(reporte, "like")
                                },
                                onDislikeClick = {
                                    viewModel.handleReaccion(reporte, "dislike")
                                },
                                onCommentClick = {
                                    // Navegar a la pantalla de detalles/comentarios
                                    navController.navigate(Pantallas.EditarReporte.crearRuta(reporte.id.toString()))
                                },
                                onImageClick = {
                                    reporteSeleccionado = reporte
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    // Diálogo de filtro (No cambia)
    if (mostrarFiltro) {
        FiltroReportesPublicosDialog(
            tipoSeleccionado = tipoReporteFiltro,
            estadoSeleccionado = estadoReporteFiltro,
            onDismiss = { mostrarFiltro = false },
            onAplicar = { tipo, estado ->
                tipoReporteFiltro = tipo
                estadoReporteFiltro = estado
                mostrarFiltro = false
            },
            onRestablecer = {
                tipoReporteFiltro = null
                estadoReporteFiltro = null
            }
        )
    }

    // Diálogo de imagen completa (Modificado para usar URL)
    reporteSeleccionado?.let { reporte ->
        ImageGalleryDialogReporte(
            // Ahora pasamos las URLs de las imágenes
            images = listOfNotNull(reporte.img_prueba_1, reporte.img_prueba_2?.takeIf { it.isNotEmpty() }),
            initialIndex = 0,
            onDismiss = { reporteSeleccionado = null }
        )
    }
}

@Composable
fun ReportePublicoCard(
    reporte: MiReporte, // <-- CAMBIO: Usa el modelo de la API
    context: Context,
    onLikeClick: () -> Unit,
    onDislikeClick: () -> Unit,
    onCommentClick: () -> Unit,
    onImageClick: () -> Unit
) {
    // --- LÓGICA DE UI AHORA BASADA EN EL REPORTE ---
    val backgroundColor = obtenerColorCategoria(reporte.categoria_nombre)
    val esColorClaro = esColorClaro(reporte.categoria_nombre)
    val textColor = if (esColorClaro) Color.Black else WhiteFull
    val iconoCopiar = if (esColorClaro) R.drawable.copiarneg else R.drawable.copiarbla

    val imagenEstado = when (reporte.estado) {
        "Nuevo" -> R.drawable.nuevo
        "En proceso" -> R.drawable.proceso
        "Resuelto" -> R.drawable.resuelto
        else -> R.drawable.nuevo
    }

    // Formatear el ID como en la maqueta
    val formattedId = "# 2025-${reporte.id.toString().padStart(5, '0')}"

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(155.dp)
            .clickable { onCommentClick() }, // Hacer toda la tarjeta clickeable
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Contenido principal
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(start = 12.dp, end = 12.dp, top = 10.dp)
            ) {
                // Columna izquierda: categoría, ID, descripción
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    // Categoría
                    Text(
                        text = reporte.categoria_nombre?.replaceFirstChar {
                            if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString()
                        } ?: "Reporte",
                        color = textColor,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    // ID con botón copiar
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .clickable(
                                indication = null,
                                interactionSource = remember { MutableInteractionSource() }
                            ) {
                                copiarAlPortapapeles(context, formattedId)
                            }
                            .padding(vertical = 2.dp)
                    ) {
                        Text(
                            text = formattedId, // ID formateado
                            color = textColor,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Image(
                            painter = painterResource(id = iconoCopiar),
                            contentDescription = "Copiar ID",
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // Descripción
                    Text(
                        text = reporte.descripcion ?: "Sin descripción.",
                        color = textColor.copy(alpha = 0.9f),
                        fontSize = 12.sp,
                        lineHeight = 14.sp,
                        maxLines = 4,
                        textAlign = TextAlign.Justify
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                // Imagen a la derecha
                Box(
                    modifier = Modifier
                        .width(100.dp)
                        .height(105.dp)
                ) {
                    // --- CAMBIO: Usar AsyncImage (Coil) ---
                    AsyncImage(
                        model = reporte.img_prueba_1, // Carga la URL
                        contentDescription = reporte.categoria_nombre,
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(8.dp))
                            .clickable(
                                // Detener la propagación del clic de la tarjeta
                                indication = null,
                                interactionSource = remember { MutableInteractionSource() }
                            ) {
                                onImageClick()
                            },
                        contentScale = ContentScale.Crop
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Fila inferior: Ícono categoría, likes, dislikes y estado
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 12.dp, end = 12.dp, bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Ícono de categoría
                    Image(
                        painter = painterResource(id = obtenerIconoCategoria(reporte.categoria_nombre)),
                        contentDescription = reporte.categoria_nombre,
                        modifier = Modifier.size(28.dp)
                    )

                    // --- LÓGICA DE REACCIÓN ACTUALIZADA ---
                    val isLiked = reporte.current_user_reaction == "like"
                    val isDisliked = reporte.current_user_reaction == "dislike"

                    // Like
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { onLikeClick() }
                            .padding(horizontal = 4.dp, vertical = 2.dp)
                    ) {
                        Image(
                            painter = painterResource(
                                id = if (isLiked) R.drawable.like_relleno else R.drawable.sin_like
                            ),
                            contentDescription = "Like",
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = reporte.apoyos_count.toString(),
                            color = textColor,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    // Dislike
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { onDislikeClick() }
                            .padding(horizontal = 4.dp, vertical = 2.dp)
                    ) {
                        Image(
                            painter = painterResource(
                                id = if (isDisliked) R.drawable.dislike_relleno else R.drawable.no_like
                            ),
                            contentDescription = "Dislike",
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = reporte.desapoyos_count.toString(),
                            color = textColor,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    // --- SECCIÓN DE COMENTARIOS ---
                    // NOTA: Tu API (GET /reportes) y tu modelo (MiReporte)
                    // no incluyen un contador de comentarios.
                    // Si lo añades en el futuro, puedes descomentar esto.
                    /*
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { onCommentClick() }
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.comentario),
                            contentDescription = "Comentarios",
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = reporte.comentarios_count.toString(), // <-- Necesitarías este campo
                            color = textColor,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    */
                }

                // Imagen de estado
                Box(
                    modifier = Modifier
                        .width(75.dp)
                        .height(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = imagenEstado),
                        contentDescription = reporte.estado,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Fit
                    )
                }
            }
        }
    }
}

@Composable
fun FiltroReportesPublicosDialog(
    tipoSeleccionado: String?,
    estadoSeleccionado: String?,
    onDismiss: () -> Unit,
    onAplicar: (String?, String?) -> Unit,
    onRestablecer: () -> Unit
) {
    var tipoTemporal by remember { mutableStateOf(tipoSeleccionado) }
    var estadoTemporal by remember { mutableStateOf(estadoSeleccionado) }

    val tiposReporte = listOf(
        "Hueco", "Alumbrado", "Basura",
        "Semaforo", "Hidrante", "Alcantarilla"
    )

    val estadosReporte = listOf("Nuevo", "Resuelto", "En proceso")

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.6f))
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }
                ) { onDismiss() },
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier
                    .width(320.dp)
                    .clickable(enabled = false) { },
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = WhiteFull),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Box {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp)
                    ) {
                        Text(
                            text = "Filtrar Reportes",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp)
                        )

                        Text(
                            text = "Tipo de Reporte",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.Black,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            tiposReporte.chunked(3).forEach { fila ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    fila.forEach { tipo ->
                                        FiltroBotonReporte(
                                            texto = tipo,
                                            seleccionado = tipoTemporal == tipo,
                                            onClick = {
                                                tipoTemporal = if (tipoTemporal == tipo) null else tipo
                                            },
                                            modifier = Modifier.weight(1f)
                                        )
                                    }
                                    repeat(3 - fila.size) {
                                        Spacer(modifier = Modifier.weight(1f))
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        Text(
                            text = "Estado del Reporte",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.Black,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            estadosReporte.forEach { estado ->
                                FiltroBotonReporte(
                                    texto = estado,
                                    seleccionado = estadoTemporal == estado,
                                    onClick = {
                                        estadoTemporal = if (estadoTemporal == estado) null else estado
                                    },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Button(
                                onClick = {
                                    tipoTemporal = null
                                    estadoTemporal = null
                                    onRestablecer()
                                },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF663251)
                                ),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = "Restablecer",
                                    color = WhiteFull,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Button(
                                onClick = {
                                    onAplicar(tipoTemporal, estadoTemporal)
                                },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF4AB7B6)
                                ),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = "Aplicar",
                                    color = Color.Black,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(4.dp)
                            .size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Cerrar",
                            tint = Color.Black,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun FiltroBotonReporte(
    texto: String,
    seleccionado: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(36.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (seleccionado) Color(0xFFFF8F0C) else Color(0xFFE8E8E8)
        ),
        shape = RoundedCornerShape(8.dp),
        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = texto,
            color = if (seleccionado) WhiteFull else Color.Black,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center,
            maxLines = 2,
            lineHeight = 12.sp
        )
    }
}

// --- ImageGalleryDialogReporte MODIFICADO ---
@Composable
fun ImageGalleryDialogReporte(
    images: List<String>, // <-- CAMBIO: Acepta lista de Strings (URLs)
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
                    // --- CAMBIO: Usar AsyncImage (Coil) ---
                    AsyncImage(
                        model = images[currentIndex], // Carga la URL
                        contentDescription = "Imagen del reporte",
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
                        text = "Volver",
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
                        contentDescription = "Anterior",
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
                        contentDescription = "Siguiente",
                        modifier = Modifier.size(32.dp),
                        tint = Color.White
                    )
                }
            }
        }
    }
}

// --- COMPONENTES SIN CAMBIOS ---

@Composable
fun BottomNavBarThreee(navController: NavHostController) {
    val context = LocalContext.current
    val userPreferencesManager = remember { UserPreferencesManager(context) }
    val userRole = remember { userPreferencesManager.getUserRole() }

    NavigationBar(
        containerColor = BlueMain,
        modifier = Modifier
            .fillMaxWidth()
            .height(70.dp)
    ) {
        Spacer(modifier = Modifier.width(3.dp))
        NavigationBarItem(
            selected = false,
            onClick = {
                val userId = userPreferencesManager.getUserId()
                if (userId != -1) {
                    navController.navigate(Pantallas.MisReportes.crearRuta(userId))
                } else {
                    Toast.makeText(context, "Debes iniciar sesión para ver tus reportes", Toast.LENGTH_SHORT).show()
                }
            },
            icon = {
                Image(
                    painter = painterResource(id = R.drawable.misreportes),
                    contentDescription = stringResource(R.string.nav_my_reports),
                    modifier = Modifier.size(26.dp)
                )
            },
            label = {
                Text(
                    stringResource(R.string.nav_my_reports),
                    color = WhiteFull,
                    fontSize = 11.sp
                )
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = WhiteFull,
                selectedTextColor = WhiteFull,
                unselectedIconColor = WhiteFull.copy(alpha = 0.6f),
                unselectedTextColor = WhiteFull.copy(alpha = 0.6f),
                indicatorColor = Color.Transparent
            )
        )
        Spacer(modifier = Modifier.width(3.dp))
        NavigationBarItem(
            selected = false,
            onClick = { navController.navigate(Pantallas.MisApoyos.ruta) },
            icon = {
                Image(
                    painter = painterResource(id = R.drawable.heart),
                    contentDescription = "Mis apoyos",
                    modifier = Modifier
                        .size(30.dp)
                        .padding(top = 5.dp)
                )
            },
            label = {
                Text(
                    "Mis apoyos",
                    color = WhiteFull,
                    fontSize = 11.sp
                )
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = WhiteFull,
                selectedTextColor = WhiteFull,
                unselectedIconColor = WhiteFull.copy(alpha = 0.6f),
                unselectedTextColor = WhiteFull.copy(alpha = 0.6f),
                indicatorColor = Color.Transparent
            )
        )
        Spacer(modifier = Modifier.width(3.dp))
        NavigationBarItem(
            selected = false,
            onClick = { navController.navigate(Pantallas.MisDenuncias.ruta) },
            icon = {
                Image(
                    painter = painterResource(id = R.drawable.bandera),
                    contentDescription = "Mis denuncias",
                    modifier = Modifier.size(26.dp),
                )
            },
            label = {
                Text(
                    "Mis denuncias",
                    color = WhiteFull,
                    fontSize = 10.5.sp
                )
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = WhiteFull,
                selectedTextColor = WhiteFull,
                unselectedIconColor = WhiteFull.copy(alpha = 0.6f),
                unselectedTextColor = WhiteFull.copy(alpha = 0.6f),
                indicatorColor = Color.Transparent
            )
        )
        Spacer(modifier = Modifier.width(3.dp))
    }
}

// --- FUNCIONES HELPER SIN CAMBIOS ---