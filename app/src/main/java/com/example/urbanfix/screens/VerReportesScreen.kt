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

data class ReportePublico(
    val id: String,
    val tipo: String,
    val imagen: Int,
    val descripcion: String,
    val estado: String,
    val direccion: String,
    val colorIndex: Int,
    val iconoCategoria: Int
)

// Función para obtener el ícono de categoría según el tipo
fun obtenerIconoCategoria(tipo: String): Int {
    return when (tipo) {
        "Alcantarilla" -> R.drawable.ciralcantarilla
        "Semaforo" -> R.drawable.cirsemaforo
        "Alumbrado" -> R.drawable.ciralumbrado
        "Basura" -> R.drawable.cirbasura
        "Hueco" -> R.drawable.circarro
        "Hidrante" -> R.drawable.cirhidrante
        else -> R.drawable.circarro
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
    var reporteSeleccionado by remember { mutableStateOf<ReportePublico?>(null) }

    // Estados para likes y dislikes (mapa con ID del reporte como clave)
    var likesMap by remember { mutableStateOf(mapOf<String, Int>()) }
    var dislikesMap by remember { mutableStateOf(mapOf<String, Int>()) }
    var commentsMap by remember { mutableStateOf(mapOf<String, Int>()) }
    var likeStatusMap by remember { mutableStateOf(mapOf<String, Boolean?>()) } // null = ninguno, true = like, false = dislike

    // Lista de reportes estáticos
    val reportes = remember {
        listOf(
            ReportePublico(
                "2025-00006",
                "Alumbrado",
                R.drawable.prueba_circulo,
                "Se reporta que en la xx el alumbrado está intermitente, específicamente al lado del xxx",
                "En proceso",
                "Carrera 12 #30c 47 Sur",
                0,
                obtenerIconoCategoria("Alumbrado")
            ),
            ReportePublico(
                "2025-00005",
                "Hueco",
                R.drawable.prueba_circulo,
                "Se reporta que en la xx hay un hueco grande, específicamente al lado del xxx",
                "Nuevo",
                "Calle 45 #12-34",
                1,
                obtenerIconoCategoria("Hueco")
            ),
            ReportePublico(
                "2025-00004",
                "Alumbrado",
                R.drawable.prueba_circulo,
                "Se reporta que en la xx el alumbrado está intermitente, específicamente al lado del xxx",
                "Nuevo",
                "Avenida 68 #23-45",
                2,
                obtenerIconoCategoria("Alumbrado")
            ),
            ReportePublico(
                "2025-00003",
                "Alumbrado",
                R.drawable.prueba_circulo,
                "Se reporta que en la xx el alumbrado está intermitente, específicamente al lado del xxx",
                "Resuelto",
                "Carrera 7 #100-50",
                3,
                obtenerIconoCategoria("Alumbrado")
            )
        )
    }

    // Inicializar contadores
    LaunchedEffect(Unit) {
        reportes.forEach { reporte ->
            likesMap = likesMap + (reporte.id to 12)
            dislikesMap = dislikesMap + (reporte.id to 1)
            commentsMap = commentsMap + (reporte.id to 5)
            likeStatusMap = likeStatusMap + (reporte.id to null)
        }
    }

    // Filtrar reportes
    val reportesFiltrados = remember(tipoReporteFiltro, estadoReporteFiltro, reportes) {
        reportes.filter { reporte ->
            val coincideTipo = tipoReporteFiltro == null || reporte.tipo == tipoReporteFiltro
            val coincideEstado = estadoReporteFiltro == null || reporte.estado == estadoReporteFiltro
            coincideTipo && coincideEstado
        }
    }

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
                        text = "No se encontraron reportes con los filtros aplicados",
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
                items(reportesFiltrados) { reporte ->
                    ReportePublicoCard(
                        reporte = reporte,
                        context = context,
                        likes = likesMap[reporte.id] ?: 0,
                        dislikes = dislikesMap[reporte.id] ?: 0,
                        comments = commentsMap[reporte.id] ?: 0,
                        likeStatus = likeStatusMap[reporte.id],
                        onLikeClick = {
                            val currentStatus = likeStatusMap[reporte.id]
                            if (currentStatus == true) {
                                // Ya tiene like, lo quitamos
                                likesMap = likesMap + (reporte.id to (likesMap[reporte.id]!! - 1))
                                likeStatusMap = likeStatusMap + (reporte.id to null)
                            } else {
                                // Agregar like
                                if (currentStatus == false) {
                                    // Tenía dislike, lo quitamos
                                    dislikesMap = dislikesMap + (reporte.id to (dislikesMap[reporte.id]!! - 1))
                                }
                                likesMap = likesMap + (reporte.id to (likesMap[reporte.id]!! + 1))
                                likeStatusMap = likeStatusMap + (reporte.id to true)
                            }
                        },
                        onDislikeClick = {
                            val currentStatus = likeStatusMap[reporte.id]
                            if (currentStatus == false) {
                                // Ya tiene dislike, lo quitamos
                                dislikesMap = dislikesMap + (reporte.id to (dislikesMap[reporte.id]!! - 1))
                                likeStatusMap = likeStatusMap + (reporte.id to null)
                            } else {
                                // Agregar dislike
                                if (currentStatus == true) {
                                    // Tenía like, lo quitamos
                                    likesMap = likesMap + (reporte.id to (likesMap[reporte.id]!! - 1))
                                }
                                dislikesMap = dislikesMap + (reporte.id to (dislikesMap[reporte.id]!! + 1))
                                likeStatusMap = likeStatusMap + (reporte.id to false)
                            }
                        },
                        onCommentClick = {
                            Toast.makeText(context, "Comentarios de ${reporte.id}", Toast.LENGTH_SHORT).show()
                        },
                        onImageClick = {
                            reporteSeleccionado = reporte
                        }
                    )
                }
            }
        }
    }

    // Diálogo de filtro
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

    // Diálogo de imagen completa
    reporteSeleccionado?.let { reporte ->
        ImageGalleryDialogReporte(
            images = listOf(reporte.imagen),
            initialIndex = 0,
            onDismiss = { reporteSeleccionado = null }
        )
    }
}

@Composable
fun ReportePublicoCard(
    reporte: ReportePublico,
    context: Context,
    likes: Int,
    dislikes: Int,
    comments: Int,
    likeStatus: Boolean?,
    onLikeClick: () -> Unit,
    onDislikeClick: () -> Unit,
    onCommentClick: () -> Unit,
    onImageClick: () -> Unit
) {
    val colores = listOf(
        Color(0xFF663251),
        Color(0xFF4AB7B6),
        Color(0xFF1D3557),
        Color(0xFF663251)
    )

    val backgroundColor = colores[reporte.colorIndex % colores.size]
    val esColorClaro = reporte.colorIndex == 1
    val textColor = if (esColorClaro) Color.Black else WhiteFull
    val iconoCopiar = if (esColorClaro) R.drawable.copiarneg else R.drawable.copiarbla

    val imagenEstado = when (reporte.estado) {
        "Nuevo" -> R.drawable.nuevo
        "En proceso" -> R.drawable.proceso
        "Resuelto" -> R.drawable.resuelto
        else -> R.drawable.nuevo
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(155.dp),
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
                        text = reporte.tipo,
                        color = textColor,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    // ID con botón copiar
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable {
                            copiarAlPortapapeles(context, reporte.id)
                        }
                    ) {
                        Text(
                            text = "# ${reporte.id}",
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
                        text = reporte.descripcion,
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
                    Image(
                        painter = painterResource(id = reporte.imagen),
                        contentDescription = reporte.tipo,
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { onImageClick() },
                        contentScale = ContentScale.Crop
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Fila inferior: Ícono categoría, likes, dislikes, comentarios y estado
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
                        painter = painterResource(id = reporte.iconoCategoria),
                        contentDescription = reporte.tipo,
                        modifier = Modifier.size(28.dp)
                    )

                    // Like
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { onLikeClick() }
                    ) {
                        Image(
                            painter = painterResource(
                                id = if (likeStatus == true) R.drawable.like_relleno else R.drawable.sin_like
                            ),
                            contentDescription = "Like",
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = likes.toString(),
                            color = textColor,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    // Dislike
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { onDislikeClick() }
                    ) {
                        Image(
                            painter = painterResource(
                                id = if (likeStatus == false) R.drawable.dislike_relleno else R.drawable.no_like
                            ),
                            contentDescription = "Dislike",
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = dislikes.toString(),
                            color = textColor,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    // Comentarios
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
                            text = comments.toString(),
                            color = textColor,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
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
                .clickable { onDismiss() },
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

@Composable
fun ImageGalleryDialogReporte(
    images: List<Int>,
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
                    Image(
                        painter = painterResource(id = images[currentIndex]),
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