package com.example.urbanfix.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.urbanfix.R
import com.example.urbanfix.data.UserPreferencesManager
import com.example.urbanfix.navigation.Pantallas
import com.example.urbanfix.ui.theme.*

data class Apoyo(
    val id: String,
    val tipo: String,
    val imagen: Int,
    val descripcion: String,
    val estado: String,
    val tieneCorazon: Boolean = false
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MisApoyosScreen(
    navController: NavHostController
) {
    val context = LocalContext.current
    var mostrarFiltro by remember { mutableStateOf(false) }
    var tipoReporteFiltro by remember { mutableStateOf<String?>(null) }
    var estadoReporteFiltro by remember { mutableStateOf<String?>(null) }
    var apoyoSeleccionado by remember { mutableStateOf<Apoyo?>(null) }
    var mostrarDialogoEliminar by remember { mutableStateOf<Apoyo?>(null) }
    var mostrarCopiado by remember { mutableStateOf(false) }
    var mostrarImagenCompleta by remember { mutableStateOf<Int?>(null) }

    // Lista mutable de apoyos
    var apoyos by remember {
        mutableStateOf(
            listOf(
                Apoyo(
                    "2025-0001",
                    "Alumbrado Público",
                    R.drawable.huecoeje,
                    "Se reporta que en la xx el alumbrado está intermitente, específicamente al lado del xxx",
                    "Nuevo",
                    true
                ),
                Apoyo(
                    "2025-XXXX",
                    "Hueco",
                    R.drawable.huecoeje,
                    "Se reporta que en la xx hay un hueco grande, específicamente al lado del xxx",
                    "Resuelto",
                    false
                ),
                Apoyo(
                    "2025-XXXX",
                    "Alumbrado Público",
                    R.drawable.huecoeje,
                    "Se reporta que en la xx el alumbrado está intermitente, específicamente al lado del xxx",
                    "En proceso",
                    true
                ),
                Apoyo(
                    "2025-XXXX",
                    "Alumbrado Público",
                    R.drawable.huecoeje,
                    "Se reporta que en la xx el alumbrado está intermitente, específicamente al lado del xxx",
                    "Nuevo",
                    true
                )
            )
        )
    }

    // Filtrar apoyos
    val apoyosFiltrados = remember(tipoReporteFiltro, estadoReporteFiltro, apoyos) {
        apoyos.filter { apoyo ->
            val coincideTipo = tipoReporteFiltro == null || apoyo.tipo == tipoReporteFiltro
            val coincideEstado = estadoReporteFiltro == null || apoyo.estado == estadoReporteFiltro
            coincideTipo && coincideEstado
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Mis apoyos",
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
            BottomNavBarThree2(navController = navController)
        },
        containerColor = GrayBg
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(apoyosFiltrados) { apoyo ->
                ApoyoCard(
                    apoyo = apoyo,
                    onClick = { mostrarImagenCompleta = apoyo.imagen },
                    onCorazonClick = {
                        mostrarDialogoEliminar = apoyo
                    },
                    onCopiarClick = {
                        copiarAlPortapapeles2(context, apoyo.id)
                        mostrarCopiado = true
                    }
                )
            }
        }
    }

    // Diálogo de filtro
    if (mostrarFiltro) {
        FiltroApoyosDialog(
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

    // Diálogo de eliminación
    mostrarDialogoEliminar?.let { apoyo ->
        EliminarApoyoDialog(
            onDismiss = { mostrarDialogoEliminar = null },
            onConfirm = {
                apoyos = apoyos.filter { it.id != apoyo.id }
                mostrarDialogoEliminar = null
            }
        )
    }

    // Diálogo de código copiado
    if (mostrarCopiado) {
        CodigoCopiadoDialog(
            onDismiss = { mostrarCopiado = false }
        )
    }

    // Diálogo de imagen completa
    mostrarImagenCompleta?.let { imagen ->
        ImageGalleryDialog2(
            images = listOf(imagen),
            initialIndex = 0,
            onDismiss = { mostrarImagenCompleta = null }
        )
    }
}

@Composable
fun ApoyoCard(
    apoyo: Apoyo,
    onClick: () -> Unit,
    onCorazonClick: () -> Unit,
    onCopiarClick: () -> Unit
) {
    val colorEstado = when (apoyo.estado) {
        "Nuevo" -> Color(0xFFFFD700)
        "En proceso" -> Color(0xFF4AB7B6)
        "Resuelto" -> Color(0xFF4CAF50)
        else -> Color.Gray
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(140.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1D3557)),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp)
        ) {
            // Imagen
            Box(
                modifier = Modifier
                    .width(90.dp)
                    .fillMaxHeight()
            ) {
                Image(
                    painter = painterResource(id = apoyo.imagen),
                    contentDescription = apoyo.tipo,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { onClick() },
                    contentScale = ContentScale.Crop
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Contenido
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Header con título y estado
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Text(
                        text = apoyo.tipo,
                        color = WhiteFull,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f)
                    )

                    // Badge de estado
                    Box(
                        modifier = Modifier
                            .background(colorEstado, RoundedCornerShape(8.dp))
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = apoyo.estado,
                            color = Color.Black,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.width(4.dp))

                    // ID copiable
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { onCopiarClick() }
                    ) {
                        Text(
                            text = "#${apoyo.id}",
                            color = WhiteFull,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Image(
                            painter = painterResource(id = R.drawable.copiarbla),
                            contentDescription = "Copiar",
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                // Descripción
                Text(
                    text = apoyo.descripcion,
                    color = WhiteFull.copy(alpha = 0.9f),
                    fontSize = 11.sp,
                    lineHeight = 14.sp,
                    maxLines = 3,
                    modifier = Modifier.weight(1f)
                )

                // Footer con corazón
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.Bottom
                ) {
                    if (apoyo.tieneCorazon) {
                        IconButton(
                            onClick = onCorazonClick,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.icono_corazon),
                                contentDescription = "Eliminar apoyo",
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FiltroApoyosDialog(
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
                                        FiltroBoton2(
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
                                FiltroBoton2(
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
fun FiltroBoton2(
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
fun EliminarApoyoDialog(
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
                        text = "Seguro que eliminar el reporte de Mis apoyos?",
                        color = Color.Black,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }
                Text(
                    text = "No se puede deshacer; tendrás que volver a buscar el reporte y darle like otra vez.",
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
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1D3557)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 48.dp)
                        .height(48.dp)
                ) {
                    Text(
                        "Volver",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
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
                    Text(
                        "Confirmar",
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
fun CodigoCopiadoDialog(onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Box(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Se ha copiado el Código",
                        color = Color.Black,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    Text(
                        text = "Si deseas consultar el reporte posteriormente, puedes pegar este código en la barra de búsqueda del menú.",
                        fontSize = 14.sp,
                        color = Color.Black,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(bottom = 24.dp)
                    )
                }
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(8.dp)
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

@Composable
fun ImageGalleryDialog2(
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

fun copiarAlPortapapeles2(context: Context, texto: String) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val clip = ClipData.newPlainText("Código Reporte", texto)
    clipboard.setPrimaryClip(clip)
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun MisApoyosScreenPreview() {
    UrbanFixTheme {
        MisApoyosScreen(navController = rememberNavController())
    }
}

@Composable
fun BottomNavBarThree2(navController: NavHostController) {
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