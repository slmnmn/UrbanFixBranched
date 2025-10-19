package com.example.urbanfix.screens

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.navigation.NavHostController
import com.example.urbanfix.R
import com.example.urbanfix.ui.theme.*
import com.example.urbanfix.data.ReportDataHolder
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import android.media.ExifInterface

private fun rotateImageIfNeeded(bitmap: Bitmap, uri: Uri, context: android.content.Context): Bitmap {
    return try {
        val inputStream = context.contentResolver.openInputStream(uri)
        val exif = ExifInterface(inputStream!!)
        val orientation = exif.getAttributeInt(
            ExifInterface.TAG_ORIENTATION,
            ExifInterface.ORIENTATION_NORMAL
        )
        inputStream.close()

        val matrix = android.graphics.Matrix()
        when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> matrix.postRotate(90f)
            ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(180f)
            ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(270f)
        }

        android.graphics.Bitmap.createBitmap(
            bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true
        )
    } catch (e: Exception) {
        bitmap
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportarDosScreen(
    navController: NavHostController,
    reportType: String = "huecos"
) {
    // Obtener datos guardados
    val eventAddress = ReportDataHolder.eventAddress
    val referencePoint = ReportDataHolder.referencePoint
    var photos by remember { mutableStateOf(ReportDataHolder.photos) }
    val savedLocation = ReportDataHolder.selectedLocation

    val context = LocalContext.current
    val scrollState = rememberScrollState()
    val coroutineScope = rememberCoroutineScope()

    // Estados
    var selectedSubtype by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }
    var description by remember { mutableStateOf("") }
    var isGeneratingDescription by remember { mutableStateOf(false) }
    var expandedImageIndex by remember { mutableStateOf<Int?>(null) }
    var showExitDialog by remember { mutableStateOf(false) }
    var showIncompleteFieldsDialog by remember { mutableStateOf(false) }
    var showMaxPhotosDialog by remember { mutableStateOf(false) }
    var showPhotoOptionsDialog by remember { mutableStateOf(false) }
    var tempImageUri by remember { mutableStateOf<Uri?>(null) }

    // Launcher para galería
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            if (photos.size >= 2) {
                showMaxPhotosDialog = true
            } else {
                try {
                    val inputStream = context.contentResolver.openInputStream(it)
                    var bitmap = BitmapFactory.decodeStream(inputStream)
                    bitmap = rotateImageIfNeeded(bitmap, it, context)
                    photos = photos + bitmap
                    inputStream?.close()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    // Launcher para cámara
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && tempImageUri != null) {
            if (photos.size >= 2) {
                showMaxPhotosDialog = true
            } else {
                try {
                    val inputStream = context.contentResolver.openInputStream(tempImageUri!!)
                    var bitmap = BitmapFactory.decodeStream(inputStream)
                    bitmap = rotateImageIfNeeded(bitmap, tempImageUri!!, context)
                    photos = photos + bitmap
                    inputStream?.close()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    // Launcher para permisos de cámara
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            val photoFile = File(context.cacheDir, "report_photo_${System.currentTimeMillis()}.jpg")
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                photoFile
            )
            tempImageUri = uri
            cameraLauncher.launch(uri)
        }
    }

    // Limpiar datos cuando se destruya la pantalla
    DisposableEffect(Unit) {
        onDispose {
            ReportDataHolder.clearData()
        }
    }

    // Obtener el nombre del tipo de reporte
    val reportTypeName = when (reportType) {
        "huecos" -> stringResource(R.string.report_type_potholes)
        "alumbrado" -> stringResource(R.string.report_type_lighting)
        "basura" -> stringResource(R.string.report_type_trash)
        "semaforo" -> stringResource(R.string.report_type_traffic_light)
        "hidrante" -> stringResource(R.string.report_type_hydrant)
        "alcantarilla" -> stringResource(R.string.report_type_sewer)
        else -> stringResource(R.string.report_type_potholes)
    }

    // Obtener subtipos según el tipo de reporte
    val subtypes = getSubtypesForReportType(reportType)

    // Función para generar descripción
    suspend fun generateDescription(regenerate: Boolean = false) {
        isGeneratingDescription = true
        delay(2000) // Simular tiempo de generación

        description = if (!regenerate) {
            "Se reporta que en $eventAddress, se observa $selectedSubtype. Usar como punto de referencia: $referencePoint."
        } else {
            "En $eventAddress, hay presencia de $selectedSubtype. Punto de referencia: $referencePoint."
        }

        isGeneratingDescription = false
    }

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
                            text = stringResource(R.string.report_title, reportTypeName),
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                    }
                },
                navigationIcon = {
                    Box(modifier = Modifier.padding(top = 20.dp)) {
                        IconButton(onClick = {
                            if (selectedSubtype.isNotEmpty() || description.isNotEmpty()) {
                                showExitDialog = true
                            } else {
                                navController.popBackStack()
                            }
                        }) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = stringResource(R.string.back_button_content_description),
                                tint = WhiteFull
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color(0xFFF1FAEE))
                .verticalScroll(scrollState)
                .padding(16.dp)
        ) {
            // Mapa (solo lectura)
            Card(
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                MapboxMapComponent(
                    modifier = Modifier
                        .fillMaxSize()
                        .nestedScroll(connection = object : NestedScrollConnection {}),
                    hasPermission = true,
                    selectedLocation = savedLocation,
                    onMapReady = {},
                    onLocationSelected = {} // No hacer nada, solo lectura
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Tipo de evento (Obligatorio)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 4.dp)
            ) {
                Text(
                    text = stringResource(R.string.event_type_label),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = BlackFull
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = stringResource(R.string.event_type_required),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFE63946)
                )
            }

            // Lista desplegable
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = selectedSubtype,
                    onValueChange = {},
                    readOnly = true,
                    placeholder = {
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                stringResource(R.string.select_event_type),
                                fontSize = 14.sp,
                                color = Color.Gray,
                                textAlign = TextAlign.Center
                            )
                        }
                    },
                    trailingIcon = {
                        Icon(
                            Icons.Default.ArrowDropDown,
                            contentDescription = null,
                            tint = Color.Gray
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor()
                        .shadow(6.dp, RoundedCornerShape(12.dp))
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedContainerColor = Color(0xFFE2DEDE),
                        focusedContainerColor = WhiteFull,
                        unfocusedBorderColor = Color.LightGray,
                        focusedBorderColor = BlueMain,
                        disabledContainerColor = Color(0xFFE2DEDE),
                        disabledBorderColor = Color.LightGray
                    ),
                    singleLine = true,
                    textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Center)
                )

                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                    modifier = Modifier
                        .background(Color.White)
                        .exposedDropdownSize()
                ) {
                    subtypes.forEach { subtype ->
                        DropdownMenuItem(
                            text = {
                                Box(
                                    modifier = Modifier.fillMaxWidth(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = subtype,
                                        fontSize = 14.sp,
                                        color = BlackFull,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            },
                            onClick = {
                                selectedSubtype = subtype
                                expanded = false
                            },
                            modifier = Modifier.background(Color.White)
                        )
                        if (subtype != subtypes.last()) {
                            HorizontalDivider(color = Color.LightGray, thickness = 1.dp)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Fotografías de evidencia
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 4.dp)
            ) {
                Text(
                    text = stringResource(R.string.attach_photos),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = BlackFull
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = stringResource(R.string.max_photos),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFE63946)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Mostrar fotos y botón añadir - CENTRADO
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                photos.forEachIndexed { index, bitmap ->
                    Box(
                        modifier = Modifier
                            .size(110.dp)
                            .padding(4.dp)
                            .clickable { expandedImageIndex = index }
                    ) {
                        Image(
                            bitmap = bitmap.asImageBitmap(),
                            contentDescription = "Foto ${index + 1}",
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(RoundedCornerShape(12.dp))
                                .border(2.dp, Color.LightGray, RoundedCornerShape(12.dp)),
                            contentScale = ContentScale.Crop
                        )
                        IconButton(
                            onClick = {
                                photos = photos.filterIndexed { i, _ -> i != index }
                            },
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .size(28.dp)
                                .background(Color.Transparent, RoundedCornerShape(14.dp))
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.eliminar_foto),
                                contentDescription = "Eliminar foto",
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }

                // Botón para añadir más fotos (si hay menos de 2)
                if (photos.size < 2) {
                    Card(
                        modifier = Modifier
                            .size(110.dp)
                            .padding(4.dp)
                            .clickable { showPhotoOptionsDialog = true },
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFFB76998)
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "+",
                                fontSize = 48.sp,
                                color = Color.White,
                                fontWeight = FontWeight.Light
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Generar Descripción con IA - Título y botones en la misma fila
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Texto del título
                Text(
                    text = stringResource(R.string.generate_description_ia),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = BlackFull,
                    modifier = Modifier.weight(1f)
                )

                // Botones al lado derecho
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Botón Generar IA
                    Card(
                        modifier = Modifier
                            .size(50.dp)
                            .clickable {
                                if (selectedSubtype.isNotEmpty() && photos.isNotEmpty()) {
                                    coroutineScope.launch {
                                        generateDescription(regenerate = false)
                                    }
                                }
                            },
                        shape = RoundedCornerShape(8.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFFB76998)
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.gen_ia),
                                contentDescription = stringResource(R.string.generate_with_ia),
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }

                    // Botón Regenerar
                    Card(
                        modifier = Modifier
                            .size(50.dp)
                            .clickable {
                                if (description.isNotEmpty()) {
                                    coroutineScope.launch {
                                        generateDescription(regenerate = true)
                                    }
                                }
                            },
                        shape = RoundedCornerShape(8.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFFB76998)
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.regen_ia),
                                contentDescription = stringResource(R.string.regenerate_description),
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Spacer(modifier = Modifier.height(12.dp))

            // Cuadro de descripción - MÁS PEQUEÑO
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .background(Color(0xFFE2DEDE), RoundedCornerShape(12.dp))
                    .padding(12.dp),
                contentAlignment = if (isGeneratingDescription) Alignment.Center else Alignment.TopStart
            ) {
                if (isGeneratingDescription) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator(
                            color = Color(0xFFB76998),
                            modifier = Modifier.size(35.dp),
                            strokeWidth = 3.dp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = stringResource(R.string.generating_description),
                            fontSize = 13.sp,
                            color = Color.Gray,
                            fontStyle = FontStyle.Italic
                        )
                    }
                } else {
                    Text(
                        text = description.ifEmpty { stringResource(R.string.description_placeholder) },
                        fontSize = 13.sp,
                        color = if (description.isEmpty()) Color.Gray else BlackFull,
                        fontStyle = if (description.isEmpty()) FontStyle.Italic else FontStyle.Normal,
                        lineHeight = 18.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Botón Crear Reporte
            Button(
                onClick = {
                    if (selectedSubtype.isEmpty()) {
                        showIncompleteFieldsDialog = true
                    } else {
                        // Aquí iría la lógica para crear el reporte
                        ReportDataHolder.clearData()
                        navController.popBackStack()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF1D3557)
                )
            ) {
                Text(
                    text = stringResource(R.string.create_report_button),
                    color = WhiteFull,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }

    // Diálogo para elegir foto
    if (showPhotoOptionsDialog) {
        PhotoOptionsDialog(
            onCamera = {
                showPhotoOptionsDialog = false
                when (PackageManager.PERMISSION_GRANTED) {
                    ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.CAMERA
                    ) -> {
                        val photoFile = File(context.cacheDir, "report_photo_${System.currentTimeMillis()}.jpg")
                        val uri = FileProvider.getUriForFile(
                            context,
                            "${context.packageName}.fileprovider",
                            photoFile
                        )
                        tempImageUri = uri
                        cameraLauncher.launch(uri)
                    }
                    else -> {
                        cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                    }
                }
            },
            onGallery = {
                showPhotoOptionsDialog = false
                galleryLauncher.launch("image/*")
            },
            onDismiss = {
                showPhotoOptionsDialog = false
            }
        )
    }

    // Otros diálogos
    if (showExitDialog) {
        ExitConfirmationDialog(
            onConfirm = {
                showExitDialog = false
                ReportDataHolder.clearData()
                navController.popBackStack()
            },
            onDismiss = {
                showExitDialog = false
            }
        )
    }

    if (showIncompleteFieldsDialog) {
        IncompleteFieldsDialog(
            onDismiss = { showIncompleteFieldsDialog = false }
        )
    }

    if (showMaxPhotosDialog) {
        MaxPhotosErrorDialog(
            onDismiss = { showMaxPhotosDialog = false }
        )
    }

    if (expandedImageIndex != null && expandedImageIndex!! < photos.size) {
        ImagePreviewDialog(
            bitmap = photos[expandedImageIndex!!],
            onDismiss = { expandedImageIndex = null }
        )
    }
}

@Composable
fun PhotoOptionsDialog(
    onCamera: () -> Unit,
    onGallery: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // 👇 PERSONALIZAR TÍTULO
                Text(
                    text = stringResource(R.string.select_photo_source),
                    fontSize = 18.sp,  // Cambia el tamaño del título aquí
                    fontWeight = FontWeight.Bold,
                    color = BlackFull,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    // 👇 BOTÓN CÁMARA PERSONALIZADO
                    Card(
                        modifier = Modifier
                            .width(100.dp)
                            .height(100.dp)
                            .clickable(onClick = onCamera),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFFB8E6E1)
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.icono_foto),
                                contentDescription = stringResource(R.string.take_photo_button),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = stringResource(R.string.take_photo_button),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = BlackFull,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(horizontal = 8.dp)
                            )
                        }
                    }

                    Card(
                        modifier = Modifier
                            .width(100.dp)
                            .height(100.dp)
                            .clickable(onClick = onGallery),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFFB8E6E1)
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.icono_galeria),
                                contentDescription = stringResource(R.string.add_from_gallery_button_dos),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = stringResource(R.string.add_from_gallery_button_dos),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = BlackFull,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(horizontal = 8.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

fun getSubtypesForReportType(reportType: String): List<String> {
    return when (reportType) {
        "huecos" -> listOf(
            "Hueco Pequeño",
            "Hueco Profundo",
            "Hueco Grande/Ancho",
            "Vía Deteriorada"
        )
        "alumbrado" -> listOf(
            "Luminaria Apagada",
            "Luminaria Intermitente",
            "Luz Encendida de Día",
            "Poste Dañado/Caído"
        )
        "alcantarilla" -> listOf(
            "Tapa Faltante/Rota",
            "Alcantarilla Obstruida",
            "Mal Olor Fuerte",
            "Fuga de Aguas Residuales"
        )
        "hidrante" -> listOf(
            "Hidrante Inoperable",
            "Fuga de Agua",
            "Obstrucción de Acceso",
            "Vandalismo/Daño Físico"
        )
        "semaforo" -> listOf(
            "Semáforo Apagado",
            "Luz Faltante/Rota",
            "Semáforo Intermitente",
            "Semáforo Caído"
        )
        "basura" -> listOf(
            "Punto Regular de Basura (Ilegal)",
            "Contenedor Desbordado",
            "Desechos Voluminosos",
            "Acumulación en Zona Verde/Parque"
        )
        else -> emptyList()
    }
}

