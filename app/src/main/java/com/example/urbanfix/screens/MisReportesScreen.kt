package com.example.urbanfix.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import kotlinx.coroutines.launch
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.viewmodel.compose.viewModel // CAMBIO: Importación del ViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage // CAMBIO: Importación para Coil
import com.example.urbanfix.R
import com.example.urbanfix.data.UserPreferencesManager
import com.example.urbanfix.navigation.Pantallas
import com.example.urbanfix.network.MiReporte // CAMBIO: Importación del modelo de datos de la red
import com.example.urbanfix.ui.theme.*
import com.example.urbanfix.viewmodel.MisReportesViewModel // CAMBIO: Importación del ViewModel

// CAMBIO: La firma de la función ahora recibe userId de la navegación y un ViewModel.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MisReportesScreen(
    navController: NavHostController,
    userId: Int, // Recibimos el ID del usuario desde la navegación
    viewModel: MisReportesViewModel = viewModel() // Instanciamos el ViewModel
) {
    val context = LocalContext.current

    // CAMBIO: Los reportes y el estado de carga ahora provienen del ViewModel.
    val reportesFromApi by viewModel.reportes.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    // Los estados locales para la UI (diálogos y filtros) se mantienen.
    var reporteSeleccionado by remember { mutableStateOf<MiReporte?>(null) } // CAMBIO: Usa MiReporte
    var mostrarFiltro by remember { mutableStateOf(false) }
    var tipoReporteFiltro by remember { mutableStateOf<String?>(null) }
    var estadoReporteFiltro by remember { mutableStateOf<String?>(null) }
    var mostrarDialogoEliminar by remember { mutableStateOf(false) }
    var mostrarDialogoError by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    // CAMBIO: LaunchedEffect para iniciar la carga de datos cuando la pantalla se compone.
    LaunchedEffect(key1 = userId) {
        // Asegurarse de que el userId sea válido antes de hacer la llamada.
        if (userId > 0) {
            viewModel.fetchMisReportes(userId)
        }
    }

    // CAMBIO: Eliminada la lista de reportes hardcodeada.
    // val reportes = remember { listOf(...) }

    // CAMBIO: El filtro ahora se aplica sobre los reportes obtenidos de la API.
    val reportesFiltrados = remember(reportesFromApi, tipoReporteFiltro, estadoReporteFiltro) {
        reportesFromApi.filter { reporte ->
            // `reporte.nombre` de la API corresponde a `tipo` en el modelo original
            val coincideTipo = tipoReporteFiltro == null || reporte.nombre == tipoReporteFiltro
            val coincideEstado = estadoReporteFiltro == null || reporte.estado == estadoReporteFiltro
            coincideTipo && coincideEstado
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            snackbarHost = { },
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = "Mis reportes",
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
                BottomNavBarThree(navController = navController)
            },
            containerColor = GrayBg
        ) { paddingValues ->
            // CAMBIO: Muestra un indicador de carga o el contenido de la lista.
            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(reportesFiltrados) { reporte ->
                        ReporteCard( // CAMBIO: Pasa el objeto MiReporte
                            reporte = reporte,
                            context = context,
                            onClick = { reporteSeleccionado = reporte }
                        )
                    }
                }
            }

            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 80.dp, start = 16.dp, end = 16.dp)
            ) { data ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(containerColor = WhiteFull),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = data.visuals.message,
                            color = Color.Black,
                            fontSize = 14.sp,
                            fontStyle = FontStyle.Italic,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(
                            onClick = { snackbarHostState.currentSnackbarData?.dismiss() },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .background(Color(0xFFFF4B3A), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Cerrar",
                                    tint = WhiteFull,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (mostrarFiltro) {
        FiltroDialog(
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

    reporteSeleccionado?.let { reporte ->
        ReporteDialog(
            reporte = reporte,
            context = context,
            onDismiss = { reporteSeleccionado = null },

            // ▼▼▼ AQUÍ ESTÁ LA LÓGICA CORRECTA ▼▼▼
            onDetalles = {
                // 1. Navega a la pantalla de detalles usando el ID del reporte
                navController.navigate(Pantallas.EditarReporte.crearRuta(reporte.id.toString()))

                // 2. Cierra el diálogo
                reporteSeleccionado = null
            },
            // ▲▲▲ FIN DEL CAMBIO ▲▲▲

            onEliminar = {
                // Simplemente muestra la confirmación siempre.
                // La lógica de si se puede o no, la debería tener el backend.
                mostrarDialogoEliminar = true
            }
        )
    }

    if (mostrarDialogoEliminar) {
        DeleteReporteDialog(
            onDismiss = { mostrarDialogoEliminar = false },
            onConfirm = {
                reporteSeleccionado?.let { reporteParaBorrar ->
                    // llama función
                    viewModel.deleteReporte(reporteParaBorrar.id)
                }

                // cierra los diálogos y resetea el estado
                mostrarDialogoEliminar = false
                reporteSeleccionado = null

                // le dicen ok
                scope.launch {
                    snackbarHostState.showSnackbar(
                        message = "Reporte eliminado exitosamente.",
                        duration = SnackbarDuration.Short
                    )
                }
            }
        )
    }

    if (mostrarDialogoError) {
        ErrorEliminarDialog(
            onDismiss = {
                mostrarDialogoError = false
                reporteSeleccionado = null
            }
        )
    }
}

@Composable
fun FiltroDialog(
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
        "Semáforo", "Hidrante", "Alcantarilla"
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
                                        FiltroBoton(
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
                                FiltroBoton(
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
                                    containerColor = Color(0xFFB76998)
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
fun FiltroBoton(
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

// CAMBIO: ReporteCard ahora recibe un objeto `MiReporte` de la red.
@Composable
fun ReporteCard(
    reporte: MiReporte,
    context: Context,
    onClick: () -> Unit
) {
    // ... (la lógica de colores y estados se mantiene igual)
    val colores = listOf(
        Color(0xFFB76998),
        Color(0xFF4AB7B6),
        Color(0xFF1D3557)
    )
    val backgroundColor = colores[reporte.id % colores.size]
    val esColorClaro = backgroundColor == Color(0xFF4AB7B6)
    val iconoCopiar = if (esColorClaro) R.drawable.copiarneg else R.drawable.copiarbla
    val textColor = if (esColorClaro) Color.Black else WhiteFull
    val imagenEstado = when (reporte.estado) {
        "Nuevo" -> R.drawable.nuevo
        "En proceso" -> R.drawable.proceso
        "Resuelto" -> R.drawable.resuelto
        else -> R.drawable.nuevo
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(235.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            Spacer(modifier = Modifier.height(7.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 0.5.dp)
                    .height(100.dp)
            ) {
                AsyncImage(
                    model = reporte.imagen_prueba_1,
                    contentDescription = reporte.nombre,
                    modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(12.dp)),
                    contentScale = ContentScale.Crop,
                    // Solución Rápida:
                    placeholder = painterResource(id = R.drawable.ic_launcher_foreground),
                    error = painterResource(id = R.drawable.ic_launcher_foreground)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 12.dp, vertical = 0.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // ▼▼▼ CAMBIO PRINCIPAL AQUÍ ▼▼▼
                    Text(
                        text = reporte.categoria_nombre, // <-- CAMBIO
                        color = textColor,
                        fontSize = 19.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    // ▲▲▲ FIN DEL CAMBIO ▲▲▲

                    Spacer(modifier = Modifier.width(8.dp))

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
                }

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "# ${reporte.id}",
                        color = textColor,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium
                    )

                    IconButton(
                        onClick = {
                            copiarAlPortapapeles(context, reporte.id.toString())
                        },
                        modifier = Modifier
                            .size(38.dp)
                            .padding(0.dp)
                    ) {
                        Image(
                            painter = painterResource(id = iconoCopiar),
                            contentDescription = "Copiar ID",
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Enviado el ${reporte.fecha_creacion}",
                    color = textColor.copy(alpha = 0.9f),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Normal
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = reporte.direccion,
                    color = textColor.copy(alpha = 0.9f),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Normal,
                    maxLines = 1
                )
            }
        }
    }
}

// CAMBIO: ReporteDialog ahora recibe un objeto `MiReporte` de la red.
@Composable
fun ReporteDialog(
    reporte: MiReporte,
    context: Context,
    onDismiss: () -> Unit,
    onDetalles: () -> Unit,
    onEliminar: () -> Unit
) {
    val colores = listOf(
        Color(0xFFB76998),
        Color(0xFF4AB7B6),
        Color(0xFF1D3557)
    )

    val backgroundColor = colores[reporte.id % colores.size]
    val esColorClaro = backgroundColor == Color(0xFF4AB7B6)
    val iconoCopiar = if (esColorClaro) R.drawable.copiarneg else R.drawable.copiarbla
    val textColor = if (esColorClaro) Color.Black else WhiteFull

    val imagenEstado = when (reporte.estado) {
        "Nuevo" -> R.drawable.nuevo
        "En proceso" -> R.drawable.proceso
        "Resuelto" -> R.drawable.resuelto
        else -> R.drawable.nuevo
    }

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
                    .width(280.dp)
                    .clickable(enabled = false) { },
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = backgroundColor),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Box {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        // Imagen
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(180.dp)
                        ) {
                            AsyncImage(
                                model = reporte.imagen_prueba_1,
                                contentDescription = reporte.nombre,
                                modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(12.dp)),
                                contentScale = ContentScale.Crop,
                                // Solución Rápida:
                                placeholder = painterResource(id = R.drawable.ic_launcher_foreground),
                                error = painterResource(id = R.drawable.ic_launcher_foreground)
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Tipo de reporte y estado
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // ▼▼▼ CAMBIO PRINCIPAL AQUÍ ▼▼▼
                            Text(
                                text = reporte.categoria_nombre, // Reemplazamos reporte.nombre
                                color = textColor,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                // El diálogo tiene espacio, así que no necesitamos
                                // maxLines o ellipsis, mostrará el nombre completo.
                            )
                            // ▲▲▲ FIN DEL CAMBIO ▲▲▲

                            Box(
                                modifier = Modifier
                                    .width(90.dp)
                                    .height(28.dp),
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

                        Spacer(modifier = Modifier.height(8.dp))

                        // ID con botón copiar
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "# ${reporte.id}",
                                color = textColor,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium
                            )

                            IconButton(
                                onClick = {
                                    copiarAlPortapapeles(context, reporte.id.toString())
                                },
                                modifier = Modifier.size(42.dp)
                            ) {
                                Image(
                                    painter = painterResource(id = iconoCopiar),
                                    contentDescription = "Copiar ID",
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        // Fecha
                        Text(
                            text = "Enviado el ${reporte.fecha_creacion}",
                            color = textColor.copy(alpha = 0.9f),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Normal
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        // Dirección
                        Text(
                            text = reporte.direccion,
                            color = textColor.copy(alpha = 0.9f),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Normal
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Botones
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = onDetalles,
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = WhiteFull
                                ),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = "Detalles",
                                    color = Color.Black,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Button(
                                onClick = onEliminar,
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFFFF3B30)
                                ),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = "Eliminar",
                                    color = WhiteFull,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    // Botón X en la esquina superior derecha
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(8.dp)
                            .size(32.dp)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.salirconreport),
                            contentDescription = "Cerrar",
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
            }
        }
    }
}

fun copiarAlPortapapeles(context: Context, texto: String) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val clip = ClipData.newPlainText("ID Reporte", texto)
    clipboard.setPrimaryClip(clip)
    Toast.makeText(context, "ID copiado: $texto", Toast.LENGTH_SHORT).show()
}

@Composable
fun DeleteReporteDialog(onDismiss: () -> Unit, onConfirm: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 1.dp),
            shape = RoundedCornerShape(1.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier.fillMaxWidth().height(48.dp).background(Color(0xFFFF4B3A)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "¿Estás seguro de que quieres eliminar este reporte?",
                        color = Color.Black, fontSize = 18.sp, fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center, modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }
                Text(
                    text = "Esta acción es PERMANENTE y no se puede deshacer. Se borrarán todos tus datos relacionados de forma definitiva.",
                    fontSize = 15.sp, fontFamily = FontFamily.SansSerif, color = Color.Black,
                    fontStyle = FontStyle.Italic, textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp, horizontal = 24.dp)
                )
                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1D3557)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 48.dp).height(48.dp)
                ) {
                    Text("Cancelar", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = onConfirm,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF4B3A)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 48.dp).padding(bottom = 24.dp).height(48.dp)
                ) {
                    Text("Confirmar", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun ErrorEliminarDialog(onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 1.dp),
            shape = RoundedCornerShape(1.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier.fillMaxWidth().height(48.dp).background(Color(0xFFFF4B3A)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Error",
                        color = Color.Black, fontSize = 18.sp, fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                }
                Text(
                    text = "No puedes borrar este reporte ya que se encuentra en proceso actualmente.",
                    fontSize = 15.sp, fontFamily = FontFamily.SansSerif, color = Color.Black,
                    fontStyle = FontStyle.Italic, textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp, horizontal = 24.dp)
                )
                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1D3557)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 48.dp).padding(bottom = 24.dp).height(48.dp)
                ) {
                    Text("Volver", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun MisReportesScreenPreview() {
    UrbanFixTheme {
        // CAMBIO: Pasa un userId de ejemplo para la preview.
        MisReportesScreen(navController = rememberNavController(), userId = 1)
    }
}

@Composable
fun BottomNavBarThree(navController: NavHostController) {
    val context = LocalContext.current
    val userPreferencesManager = remember { UserPreferencesManager(context) }
    val userRole = remember { userPreferencesManager.getUserRole() }

    NavigationBar(
        containerColor = BlueMain,
        modifier = Modifier
            .fillMaxWidth()
            .height(70.dp)
    ) {
        NavigationBarItem(
            selected = true,
            onClick = { navController.navigate(Pantallas.Home.ruta)},
            icon = {
                Image(
                    painter = painterResource(id = R.drawable.menu),
                    contentDescription = stringResource(R.string.nav_menu),
                    modifier = Modifier
                        .size(30.dp)
                        .padding(top = 5.dp)
                )
            },
            label = {
                Text(
                    stringResource(R.string.nav_menu),
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

        NavigationBarItem(
            selected = false,
            onClick = { },
            icon = {
                Image(
                    painter = painterResource(id = R.drawable.notificaciones),
                    contentDescription = stringResource(R.string.nav_notifications),
                    modifier = Modifier.size(26.dp)
                )
            },
            label = {
                Text(
                    stringResource(R.string.nav_notifications),
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

        NavigationBarItem(
            selected = false,
            onClick = {
                navController.navigate(Pantallas.Perfil.ruta)
            },
            icon = {
                Image(
                    painter = painterResource(id = R.drawable.miperfil),
                    contentDescription = stringResource(R.string.nav_profile),
                    modifier = Modifier.size(26.dp)
                )
            },
            label = {
                Text(
                    stringResource(R.string.nav_profile),
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
    }
}