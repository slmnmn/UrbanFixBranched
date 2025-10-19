package com.example.urbanfix.screens

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.ExifInterface
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.unit.Dp
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.DialogProperties
import androidx.core.app.ActivityCompat
import com.mapbox.maps.MapView
import com.example.urbanfix.data.ReportDataHolder
import com.example.urbanfix.navigation.Pantallas
import com.mapbox.maps.Style
import com.mapbox.maps.plugin.locationcomponent.location
import com.mapbox.maps.plugin.gestures.addOnMapClickListener
import com.mapbox.geojson.Point
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.plugin.annotation.annotations
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationOptions
import com.mapbox.maps.plugin.annotation.generated.createPointAnnotationManager
import androidx.navigation.NavHostController
import com.example.urbanfix.R
import com.example.urbanfix.ui.theme.*
import java.io.File
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.URLEncoder
import org.json.JSONObject
import java.net.URL


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

// Función para geocodificar dirección a coordenadas
private suspend fun geocodeAddress(address: String, context: android.content.Context): Point? {
    return withContext(Dispatchers.IO) {
        try {
            val encodedAddress = URLEncoder.encode(address, "UTF-8")
            val accessToken = context.getString(R.string.mapbox_access_token)
            val url = "https://api.mapbox.com/geocoding/v5/mapbox.places/$encodedAddress.json?access_token=$accessToken&limit=1"

            val response = URL(url).readText()
            val json = JSONObject(response)
            val features = json.getJSONArray("features")

            if (features.length() > 0) {
                val coordinates = features.getJSONObject(0)
                    .getJSONArray("coordinates")
                Point.fromLngLat(
                    coordinates.getDouble(0),
                    coordinates.getDouble(1)
                )
            } else null
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}

// Función para geocodificación inversa (coordenadas a dirección)
private suspend fun reverseGeocode(point: Point, context: android.content.Context): String? {
    return withContext(Dispatchers.IO) {
        try {
            val accessToken = context.getString(R.string.mapbox_access_token)
            val url = "https://api.mapbox.com/geocoding/v5/mapbox.places/${point.longitude()},${point.latitude()}.json?access_token=$accessToken&limit=1"

            val response = URL(url).readText()
            val json = JSONObject(response)
            val features = json.getJSONArray("features")

            if (features.length() > 0) {
                features.getJSONObject(0).getString("place_name")
            } else null
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}

// Función para actualizar la ubicación en el mapa
private fun updateMapLocation(mapView: MapView, point: Point) {
    try {
        // Mover la cámara al punto
        mapView.getMapboxMap().setCamera(
            CameraOptions.Builder()
                .center(point)
                .zoom(15.0)
                .build()
        )

        // Agregar marcador
        val annotationApi = mapView.annotations
        val pointAnnotationManager = annotationApi.createPointAnnotationManager()
        pointAnnotationManager.deleteAll()

        val pointAnnotationOptions = PointAnnotationOptions()
            .withPoint(point)

        pointAnnotationManager.create(pointAnnotationOptions)
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportarScreen(
    navController: NavHostController,
    reportType: String = "huecos"
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    val coroutineScope = rememberCoroutineScope()

    var eventAddress by remember { mutableStateOf("") }
    var referencePoint by remember { mutableStateOf("") }
    var photos by remember { mutableStateOf<List<Bitmap>>(emptyList()) }
    var tempImageUri by remember { mutableStateOf<Uri?>(null) }

    // Estados para el mapa
    var selectedLocation by remember { mutableStateOf<Point?>(null) }
    var mapView by remember { mutableStateOf<MapView?>(null) }

    var showExitDialog by remember { mutableStateOf(false) }
    var showIncompleteFieldsDialog by remember { mutableStateOf(false) }
    var showMaxPhotosDialog by remember { mutableStateOf(false) }
    var expandedImageIndex by remember { mutableStateOf<Int?>(null) }


    // Estado para permisos de ubicación
    var hasLocationPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted -> hasLocationPermission = isGranted }
    )

    LaunchedEffect(Unit) {
        if (!hasLocationPermission) {
            locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
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
                            if (eventAddress.isNotEmpty() || referencePoint.isNotEmpty() || photos.isNotEmpty()) {
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
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 4.dp)
            ) {
                Text(
                    text = stringResource(R.string.event_address_label),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = BlackFull
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = stringResource(R.string.event_address_required),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFE63946)
                )
            }
            OutlinedTextField(
                value = eventAddress,
                onValueChange = { newAddress ->
                    eventAddress = newAddress
                    // Geocodificar cuando el usuario termine de escribir
                    if (newAddress.length > 5) {
                        coroutineScope.launch {
                            val location = geocodeAddress(newAddress, context)
                            location?.let { point ->
                                selectedLocation = point
                                mapView?.let { mv ->
                                    updateMapLocation(mv, point)
                                }
                            }
                        }
                    }
                },
                placeholder = {
                    Text(
                        stringResource(R.string.event_address_placeholder),
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(6.dp, RoundedCornerShape(12.dp))
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = Color(0xFFE2DEDE),
                    focusedContainerColor = WhiteFull,
                    unfocusedBorderColor = Color.LightGray,
                    focusedBorderColor = BlueMain
                ),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Punto de referencia
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 4.dp)
            ) {
                Text(
                    text = stringResource(R.string.reference_point_label),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = BlackFull
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = stringResource(R.string.reference_point_required),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFE63946)
                )
            }

            OutlinedTextField(
                value = referencePoint,
                onValueChange = { referencePoint = it },
                placeholder = {
                    Text(
                        stringResource(R.string.reference_point_placeholder),
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .shadow(6.dp, RoundedCornerShape(12.dp)),
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = Color(0xFFE2DEDE),
                    focusedContainerColor = WhiteFull,
                    unfocusedBorderColor = Color.LightGray,
                    focusedBorderColor = BlueMain
                ),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Mapa con sincronización bidireccional
            Card(
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(if (photos.isEmpty() || photos.size >= 2) 220.dp else 150.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                MapboxMapComponent(
                    modifier = Modifier
                        .fillMaxSize()
                        .nestedScroll(connection = object : NestedScrollConnection {}),
                    hasPermission = hasLocationPermission,
                    selectedLocation = selectedLocation,
                    onMapReady = { mv -> mapView = mv },
                    onLocationSelected = { point ->
                        selectedLocation = point
                        // Geocodificación inversa: obtener dirección del punto
                        coroutineScope.launch {
                            val address = reverseGeocode(point, context)
                            if (address != null) {
                                eventAddress = address
                            }
                        }
                    }
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Adjuntar fotos
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.attach_photos),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = BlackFull,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.max_photos),
                    fontSize = 14.sp,
                    color = Color(0xFFE63946),
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
            }
            Spacer(modifier = Modifier.height(5.dp))

            // Mostrar fotos agregadas o botones
            if (photos.isEmpty()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    PhotoActionButton(
                        iconId = R.drawable.icono_foto,
                        text = stringResource(R.string.take_photo_button),
                        onClick = {
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
                        }
                    )

                    PhotoActionButton(
                        iconId = R.drawable.icono_galeria,
                        text = stringResource(R.string.add_from_gallery_button),
                        onClick = {
                            galleryLauncher.launch("image/*")
                        }
                    )
                }
            } else {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        photos.forEachIndexed { index, bitmap ->
                            Box(
                                modifier = Modifier
                                    .size(100.dp)
                                    .padding(4.dp)
                                    .clickable { expandedImageIndex = index }
                            ) {
                                Image(
                                    bitmap = bitmap.asImageBitmap(),
                                    contentDescription = "Foto ${index + 1}",
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(RoundedCornerShape(8.dp))
                                        .border(2.dp, Color.LightGray, RoundedCornerShape(8.dp)),
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
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    if (photos.size < 2) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            SmallPhotoButton(
                                iconId = R.drawable.icono_foto,
                                text = stringResource(R.string.take_photo_button),
                                width = 150.dp,
                                height = 100.dp,
                                onClick = {
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
                                }
                            )

                            Spacer(modifier = Modifier.width(8.dp))

                            SmallPhotoButton(
                                iconId = R.drawable.icono_galeria,
                                text = stringResource(R.string.add_from_gallery_button),
                                width = 150.dp,
                                height = 100.dp,
                                onClick = {
                                    galleryLauncher.launch("image/*")
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = {
                    if (eventAddress.isEmpty() || referencePoint.isEmpty()) {
                        showIncompleteFieldsDialog = true
                    } else {
                        // Guardar datos temporalmente incluyendo la ubicación
                        ReportDataHolder.setData(
                            address = eventAddress,
                            reference = referencePoint,
                            photoList = photos,
                            location = selectedLocation
                        )

                        // Navegar a la segunda pantalla
                        navController.navigate(Pantallas.ReportarDos.crearRuta(reportType))
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
                    text = stringResource(R.string.continue_button),
                    color = WhiteFull,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }

    // Diálogos
    if (showExitDialog) {
        ExitConfirmationDialog(
            onConfirm = {
                showExitDialog = false
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


private fun initLocationComponent(mapView: MapView) {
    val context = mapView.context
    if (ActivityCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    ) {
        val locationComponentPlugin = mapView.location
        locationComponentPlugin.updateSettings {
            this.enabled = true
            this.pulsingEnabled = true
        }
    }
}

@Composable
fun PhotoActionButton(
    iconId: Int,
    text: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(160.dp)
            .height(120.dp)
            .clickable(onClick = onClick),
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
                painter = painterResource(id = iconId),
                contentDescription = text,
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = text,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = BlackFull,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun SmallPhotoButton(
    iconId: Int,
    text: String,
    onClick: () -> Unit,
    width: Dp = 100.dp,
    height: Dp = 80.dp
) {
    Card(
        modifier = Modifier
            .width(width)
            .height(height)
            .clickable(onClick = onClick),
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
                painter = painterResource(id = iconId),
                contentDescription = text,
                modifier = Modifier.size(26.dp)
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = text,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = BlackFull,
                textAlign = TextAlign.Center,
                lineHeight = 10.sp
            )
        }
    }
}

@Composable
fun ExitConfirmationDialog(
    onConfirm: () -> Unit,
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
                        .background(Color(0xFFFFB74D)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(R.string.exit_confirmation_title),
                        color = Color.Black,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Text(
                    text = stringResource(R.string.exit_confirmation_message),
                    fontSize = 15.sp,
                    fontFamily = FontFamily.SansSerif,
                    color = Color.Black,
                    fontStyle = FontStyle.Italic,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp, horizontal = 24.dp)
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
                            text = stringResource(R.string.exit_no_button),
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
                            text = stringResource(R.string.exit_yes_button),
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
fun IncompleteFieldsDialog(
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
                        text = stringResource(R.string.incomplete_fields_title),
                        color = Color.Black,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Text(
                    text = stringResource(R.string.incomplete_fields_message),
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
                        text = stringResource(R.string.back_button),
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
fun MaxPhotosErrorDialog(
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
                        text = stringResource(R.string.max_photos_error_title),
                        color = Color.Black,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Text(
                    text = stringResource(R.string.max_photos_error_message),
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
                        text = stringResource(R.string.back_button),
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
fun ImagePreviewDialog(
    bitmap: Bitmap,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnClickOutside = true
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.9f)),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth(0.95f)
                    .fillMaxHeight(0.85f)
                    .background(Color.White, RoundedCornerShape(12.dp))
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Imagen ampliada
                Image(
                    bitmap = bitmap.asImageBitmap(),
                    contentDescription = "Imagen ampliada",
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp)),
                    contentScale = ContentScale.Fit
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Botón Volver
                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF1D3557)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                ) {
                    Text(
                        text = "Volver",
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
fun MapboxMapComponent(
    modifier: Modifier = Modifier,
    hasPermission: Boolean = false,
    selectedLocation: Point? = null,
    onMapReady: (MapView) -> Unit = {},
    onLocationSelected: (Point) -> Unit = {}
) {
    val context = LocalContext.current

    AndroidView(
        modifier = modifier,
        factory = { ctx ->
            val mapView = MapView(ctx).apply {
                getMapboxMap().loadStyleUri(Style.MAPBOX_STREETS)
            }

            // Callback cuando el mapa está listo
            onMapReady(mapView)

            // Detectar clics en el mapa
            mapView.getMapboxMap().addOnMapClickListener { point ->
                onLocationSelected(point)
                true
            }

            // Activar ubicación si hay permiso
            if (hasPermission) {
                try {
                    val locationComponent = mapView.location
                    locationComponent.updateSettings {
                        enabled = true
                        pulsingEnabled = true
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            mapView
        },
        update = { mapView ->
            // Mover cámara si hay una ubicación seleccionada
            selectedLocation?.let { point ->
                mapView.getMapboxMap().setCamera(
                    CameraOptions.Builder()
                        .center(point)
                        .zoom(15.0)
                        .build()
                )
            }
        }
    )
}


