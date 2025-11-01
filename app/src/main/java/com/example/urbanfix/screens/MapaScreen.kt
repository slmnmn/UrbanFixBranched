package com.example.urbanfix.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.NavHostController
import com.example.urbanfix.R
import com.example.urbanfix.navigation.Pantallas
import com.example.urbanfix.ui.theme.*
import com.mapbox.geojson.Point
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.MapView
import com.mapbox.maps.Style
import com.mapbox.maps.plugin.locationcomponent.OnIndicatorPositionChangedListener
import com.mapbox.maps.plugin.locationcomponent.location

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapaScreen(navController: NavHostController) {
    val context = LocalContext.current
    var mostrarFiltro by remember { mutableStateOf(false) }
    var tipoReporteFiltro by remember { mutableStateOf<String?>(null) }
    var estadoReporteFiltro by remember { mutableStateOf<String?>(null) }

    // Referencias para controlar el mapa
    var mapView by remember { mutableStateOf<MapView?>(null) }
    var userLocation by remember { mutableStateOf<Point?>(null) }

    var hasLocationPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            hasLocationPermission = isGranted
        }
    )

    LaunchedEffect(Unit) {
        if (!hasLocationPermission) {
            permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.urbanfixlogomenu),
                            contentDescription = stringResource(R.string.urbanfix_logo_description),
                            modifier = Modifier.height(40.dp)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(id = R.string.back_button_content_description),
                            tint = WhiteFull
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { mostrarFiltro = true }) {
                        Image(
                            painter = painterResource(id = R.drawable.filtrar),
                            contentDescription = "Filtrar",
                            modifier = Modifier.size(24.dp)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = BlueMain
                )
            )
        },
        bottomBar = {
            Column {
                MostrarListadoBar(
                    onClick = {
                        navController.navigate(Pantallas.VerReportes.ruta)
                    }
                )
                BottomNavBarThreee(navController = navController)
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize()) {
            MapboxMapComponentWithControls(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                hasPermission = hasLocationPermission,
                onMapViewReady = { map -> mapView = map },
                onUserLocationChanged = { location -> userLocation = location }
            )

            // Botones de control del mapa
            Column(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Botón Zoom In (+)
                FloatingActionButton(
                    onClick = {
                        mapView?.let { map ->
                            val currentZoom = map.getMapboxMap().cameraState.zoom
                            map.getMapboxMap().setCamera(
                                CameraOptions.Builder()
                                    .zoom(currentZoom + 1.0)
                                    .build()
                            )
                        }
                    },
                    modifier = Modifier.size(48.dp),
                    containerColor = Color(0xFFB76998),
                    contentColor = WhiteFull
                ) {
                    Text(
                        text = "+",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Botón Zoom Out (-)
                FloatingActionButton(
                    onClick = {
                        mapView?.let { map ->
                            val currentZoom = map.getMapboxMap().cameraState.zoom
                            map.getMapboxMap().setCamera(
                                CameraOptions.Builder()
                                    .zoom(currentZoom - 1.0)
                                    .build()
                            )
                        }
                    },
                    modifier = Modifier.size(48.dp),
                    containerColor = Color(0xFFB76998),
                    contentColor = WhiteFull
                ) {
                    Text(
                        text = "−",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Botón Recentrar ubicación
                FloatingActionButton(
                    onClick = {
                        mapView?.let { map ->
                            userLocation?.let { location ->
                                map.getMapboxMap().setCamera(
                                    CameraOptions.Builder()
                                        .center(location)
                                        .zoom(14.0)
                                        .build()
                                )
                            }
                        }
                    },
                    modifier = Modifier.size(48.dp),
                    containerColor = Color(0xFFB76998),
                    contentColor = WhiteFull
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.recentrar),
                        contentDescription = "Recentrar ubicación",
                        modifier = Modifier.size(24.dp),
                        tint = WhiteFull
                    )
                }
            }
        }
    }

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
}

@Composable
fun MapboxMapComponentWithControls(
    modifier: Modifier = Modifier,
    hasPermission: Boolean,
    onMapViewReady: (MapView) -> Unit,
    onUserLocationChanged: (Point) -> Unit
) {
    AndroidView(
        factory = { context ->
            val mapView = MapView(context)
            val mapboxMap = mapView.getMapboxMap()

            val bogotaCenter = Point.fromLngLat(-74.0817, 4.6097)
            val initialZoom = 10.0

            mapboxMap.setCamera(
                CameraOptions.Builder()
                    .center(bogotaCenter)
                    .zoom(initialZoom)
                    .build()
            )

            mapboxMap.loadStyleUri(Style.MAPBOX_STREETS) {
                if (hasPermission) {
                    initLocationComponentWithCallback(mapView, onUserLocationChanged)
                }
            }

            onMapViewReady(mapView)
            mapView
        },
        update = { mapView ->
            if (hasPermission) {
                initLocationComponentWithCallback(mapView, onUserLocationChanged)
            }
        },
        modifier = modifier
    )
}

private fun initLocationComponentWithCallback(
    mapView: MapView,
    onUserLocationChanged: (Point) -> Unit
) {
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

        val listener = object : OnIndicatorPositionChangedListener {
            override fun onIndicatorPositionChanged(point: Point) {
                onUserLocationChanged(point)
                mapView.getMapboxMap().setCamera(
                    CameraOptions.Builder()
                        .center(point)
                        .zoom(14.0)
                        .build()
                )
                locationComponentPlugin.removeOnIndicatorPositionChangedListener(this)
            }
        }

        locationComponentPlugin.addOnIndicatorPositionChangedListener(listener)
    }
}

@Composable
fun MostrarListadoBar(onClick: () -> Unit) {
    var offsetY by remember { mutableStateOf(0f) }
    val animatedOffsetY by animateFloatAsState(
        targetValue = offsetY,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "offset"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .offset(y = animatedOffsetY.dp)
            .height(50.dp)
            .background(Color(0xFFB76998))
            .clickable {
                offsetY = -10f
                onClick()
            }
            .pointerInput(Unit) {
                detectVerticalDragGestures(
                    onDragEnd = {
                        if (offsetY < -20) {
                            onClick()
                        }
                        offsetY = 0f
                    },
                    onVerticalDrag = { _, dragAmount ->
                        if (dragAmount < 0) {
                            offsetY = (offsetY + dragAmount * 0.5f).coerceIn(-50f, 0f)
                        }
                    }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Mostrar listado",
                color = WhiteFull,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.width(8.dp))
            Icon(
                imageVector = Icons.Default.KeyboardArrowUp,
                contentDescription = "Mostrar listado",
                tint = WhiteFull,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}