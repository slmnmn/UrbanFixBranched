package com.example.urbanfix.screens

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.ExifInterface
import android.net.Uri
import androidx.activity.ComponentActivity
import com.example.urbanfix.services.AIService
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.urbanfix.R
import com.example.urbanfix.navigation.Pantallas
import com.example.urbanfix.ui.theme.*
import com.example.urbanfix.viewmodel.ReportViewModel
import com.example.urbanfix.viewmodel.ReportState
import com.example.urbanfix.viewmodel.ViewModelFactory
import kotlinx.coroutines.launch
import java.io.File

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
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()  // MOVER ANTES del activity
    val activity = context as? ComponentActivity ?: error("No Activity found")
    val viewModel: ReportViewModel = viewModel(
        viewModelStoreOwner = activity,
        factory = ViewModelFactory(context)
    )

    // Estados del ViewModel
    val eventAddress by viewModel.eventAddress.collectAsState()
    val referencePoint by viewModel.referencePoint.collectAsState()
    val photos by viewModel.photos.collectAsState()
    val selectedLocation by viewModel.selectedLocation.collectAsState()
    val selectedSubtype by viewModel.selectedSubtype.collectAsState()
    val description by viewModel.description.collectAsState()
    val isGeneratingDescription by viewModel.isGeneratingDescription.collectAsState()
    val reportState by viewModel.reportState.collectAsState()

    // Estados locales para UI
    var expanded by remember { mutableStateOf(false) }
    var expandedImageIndex by remember { mutableStateOf<Int?>(null) }
    var showExitDialog by remember { mutableStateOf(false) }
    var showIncompleteFieldsDialog by remember { mutableStateOf(false) }
    var showMaxPhotosDialog by remember { mutableStateOf(false) }
    var showPhotoOptionsDialog by remember { mutableStateOf(false) }
    var showNoImageDialog by remember { mutableStateOf(false) }
    var showNoSubtypeDialog by remember { mutableStateOf(false) }
    var tempImageUri by remember { mutableStateOf<Uri?>(null) }

    // Observar estado del reporte
    LaunchedEffect(reportState) {
        when (reportState) {
            is ReportState.Success -> {
                // El éxito se maneja en el diálogo
            }
            is ReportState.Error -> {
                // El error se maneja en el diálogo
            }
            else -> { /* Idle o Loading */ }
        }
    }

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
                    viewModel.addPhoto(bitmap)
                    inputStream?.close()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

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
                    viewModel.addPhoto(bitmap)
                    inputStream?.close()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

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

    val reportTypeName = when (reportType) {
        "huecos" -> stringResource(R.string.report_type_potholes)
        "alumbrado" -> stringResource(R.string.report_type_lighting)
        "basura" -> stringResource(R.string.report_type_trash)
        "semaforo" -> stringResource(R.string.report_type_traffic_light)
        "hidrante" -> stringResource(R.string.report_type_hydrant)
        "alcantarilla" -> stringResource(R.string.report_type_sewer)
        else -> stringResource(R.string.report_type_potholes)
    }

    val subtypes = when (reportType) {
        "huecos" -> listOf(
            stringResource(R.string.pothole_small),
            stringResource(R.string.pothole_deep),
            stringResource(R.string.pothole_large),
            stringResource(R.string.road_deteriorated)
        )
        "alumbrado" -> listOf(
            stringResource(R.string.light_off),
            stringResource(R.string.light_flickering),
            stringResource(R.string.light_on_day),
            stringResource(R.string.pole_damaged)
        )
        "alcantarilla" -> listOf(
            stringResource(R.string.cover_missing),
            stringResource(R.string.sewer_blocked),
            stringResource(R.string.strong_odor),
            stringResource(R.string.wastewater_leak)
        )
        "hidrante" -> listOf(
            stringResource(R.string.hydrant_inoperable),
            stringResource(R.string.water_leak),
            stringResource(R.string.access_blocked),
            stringResource(R.string.vandalism_damage)
        )
        "semaforo" -> listOf(
            stringResource(R.string.traffic_light_off),
            stringResource(R.string.light_missing),
            stringResource(R.string.traffic_light_flickering),
            stringResource(R.string.traffic_light_fallen)
        )
        "basura" -> listOf(
            stringResource(R.string.illegal_dump_site),
            stringResource(R.string.overflowing_container),
            stringResource(R.string.bulky_waste),
            stringResource(R.string.green_area_accumulation)
        )
        else -> emptyList()
    }

    suspend fun generateDescription() {
        viewModel.setGeneratingDescription(true)
        try {
            val generatedDesc = AIService.generateDescription(
                address = eventAddress,
                referencePoint = referencePoint,
                subtype = selectedSubtype,
                reportType = reportType
            )
            viewModel.onDescriptionChange(generatedDesc)
        } catch (e: Exception) {
            viewModel.onDescriptionChange(
                context.getString(R.string.error_generating_description, e.message ?: "")
            )
        }
        viewModel.setGeneratingDescription(false)
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
                .padding(16.dp)
        ) {
            Card(
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                MapboxMapComponent(
                    modifier = Modifier
                        .fillMaxSize()
                        .nestedScroll(connection = object : NestedScrollConnection {}),
                    hasPermission = true,
                    selectedLocation = selectedLocation,
                    onMapReady = {},
                    onLocationSelected = {}
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

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
                                viewModel.onSubtypeChange(subtype)
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

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                photos.forEachIndexed { index, bitmap ->
                    Box(
                        modifier = Modifier
                            .size(90.dp)
                            .padding(4.dp)
                            .clickable { expandedImageIndex = index }
                    ) {
                        Image(
                            bitmap = bitmap.asImageBitmap(),
                            contentDescription = stringResource(R.string.photo_number, index + 1),
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(RoundedCornerShape(12.dp))
                                .border(2.dp, Color.LightGray, RoundedCornerShape(12.dp)),
                            contentScale = ContentScale.Crop
                        )
                        IconButton(
                            onClick = {
                                viewModel.removePhoto(index)
                            },
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .size(28.dp)
                                .background(Color.Transparent, RoundedCornerShape(14.dp))
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.eliminar_foto),
                                contentDescription = stringResource(R.string.photo_delete_description),
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }

                if (photos.size < 2) {
                    Card(
                        modifier = Modifier
                            .size(90.dp)
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
                                text = stringResource(R.string.add_photo),
                                fontSize = 48.sp,
                                color = Color.White,
                                fontWeight = FontWeight.Light
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = stringResource(R.string.generate_description_ia),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = BlackFull,
                    modifier = Modifier.weight(1f)
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.padding(end = 16.dp)
                ) {
                    Card(
                        modifier = Modifier
                            .size(52.dp)
                            .clickable {
                                if (selectedSubtype.isEmpty()) {
                                    showNoSubtypeDialog = true
                                } else if (photos.isEmpty()) {
                                    showNoImageDialog = true
                                } else {
                                    coroutineScope.launch {
                                        generateDescription()
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
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }

                    Card(
                        modifier = Modifier
                            .size(52.dp)
                            .clickable {
                                if (description.isNotEmpty()) {
                                    coroutineScope.launch {
                                        generateDescription()
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

            Spacer(modifier = Modifier.height(15.dp))

            Button(
                onClick = {
                    if (viewModel.validateStep2()) {
                        viewModel.createReport(
                            reportType = reportType,
                            onSuccess = {
                                // El éxito se maneja en el diálogo
                            }
                        )
                    } else {
                        showIncompleteFieldsDialog = true
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

    // Diálogos
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

    if (showExitDialog) {
        ExitConfirmationDialog(
            onConfirm = {
                showExitDialog = false
                viewModel.clearReportData()
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

    if (showNoImageDialog) {
        NoImageDialog(
            onDismiss = { showNoImageDialog = false }
        )
    }

    if (showNoSubtypeDialog) {
        NoSubtypeDialog(
            onDismiss = { showNoSubtypeDialog = false }
        )
    }

    // Diálogo de carga/éxito/error
    if (reportState != ReportState.Idle) {
        LoadingReportDialog(
            reportState = reportState,
            onRetry = {
                viewModel.resetReportState()
                viewModel.createReport(
                    reportType = reportType,
                    onSuccess = {}
                )
            },
            onDismiss = {
                viewModel.resetReportState()
                viewModel.clearReportData()
                // Volver a la pantalla anterior (home/inicio)
                navController.popBackStack()
            }
        )
    }
}

// ===== COMPOSABLES AUXILIARES =====

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
                Text(
                    text = stringResource(R.string.select_photo_source),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = BlackFull,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
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
                                fontSize = 12.sp,
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
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = stringResource(R.string.add_from_gallery_button_dos),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = BlackFull,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(horizontal = 8.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = onDismiss,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF1D3557)
                    )
                ) {
                    Text(
                        text = stringResource(R.string.back_button),
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun NoImageDialog(
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 1.dp),
            shape = RoundedCornerShape(1.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            )
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
                        text = stringResource(R.string.image_required_title),
                        color = Color.Black,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Text(
                    text = stringResource(R.string.image_required_message),
                    fontSize = 15.sp,
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
                        text = stringResource(R.string.understood_button),
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
fun NoSubtypeDialog(
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 1.dp),
            shape = RoundedCornerShape(1.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            )
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
                        text = stringResource(R.string.event_type_required_title),
                        color = Color.Black,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Text(
                    text = stringResource(R.string.event_type_required_message),
                    fontSize = 15.sp,
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
                        text = stringResource(R.string.understood_button),
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

// Diálogo actualizado para usar ReportState
@Composable
fun LoadingReportDialog(
    reportState: ReportState,
    onRetry: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = { if (reportState is ReportState.Success) onDismiss() }) {
        Card(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                when (reportState) {
                    is ReportState.Loading -> {
                        CircularProgressIndicator(
                            modifier = Modifier.size(80.dp).padding(8.dp),
                            color = Color(0xFF6366F1),
                            strokeWidth = 6.dp
                        )
                        Spacer(modifier = Modifier.height(20.dp))
                        Text(
                            text = stringResource(R.string.loading_report),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    is ReportState.Error -> {
                        Image(
                            painter = painterResource(id = R.drawable.error_subir_reporte),
                            contentDescription = null,
                            modifier = Modifier.size(80.dp)
                        )
                        Spacer(modifier = Modifier.height(20.dp))
                        Text(
                            text = stringResource(reportState.messageId),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(20.dp))
                        Button(
                            onClick = onRetry,
                            modifier = Modifier.fillMaxWidth().height(48.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFB76998))
                        ) {
                            Text(
                                text = stringResource(R.string.retry_button),
                                color = Color.White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    is ReportState.Success -> {
                        Image(
                            painter = painterResource(id = R.drawable.subido_correcto_reporte),
                            contentDescription = null,
                            modifier = Modifier.size(80.dp)
                        )
                        Spacer(modifier = Modifier.height(20.dp))
                        Text(
                            text = stringResource(R.string.report_completed),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(20.dp))
                        Button(
                            onClick = onDismiss,
                            modifier = Modifier.fillMaxWidth().height(48.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFB76998))
                        ) {
                            Text(
                                text = stringResource(R.string.home_button),
                                color = Color.White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    is ReportState.Idle -> { /* No mostrar */ }
                }
            }
        }
    }
}