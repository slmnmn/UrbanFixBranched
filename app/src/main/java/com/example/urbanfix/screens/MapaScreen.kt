package com.example.urbanfix.screens

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
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
import com.mapbox.maps.extension.style.layers.addLayer
import com.mapbox.maps.plugin.locationcomponent.OnIndicatorPositionChangedListener
import com.mapbox.maps.plugin.locationcomponent.location
import com.mapbox.maps.RenderedQueryGeometry
import com.mapbox.maps.RenderedQueryOptions
import com.mapbox.maps.extension.style.style
import com.mapbox.maps.extension.style.sources.generated.geoJsonSource
import com.mapbox.maps.extension.style.layers.generated.circleLayer
import com.mapbox.maps.extension.style.sources.addSource
import com.mapbox.maps.plugin.gestures.addOnMapClickListener
import com.mapbox.geojson.Feature
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.urbanfix.data.ReportesRepository
import com.example.urbanfix.network.RetrofitInstance
import com.example.urbanfix.viewmodel.MapaUiState
import com.example.urbanfix.viewmodel.MapaViewModel
import com.example.urbanfix.viewmodel.MapaViewModelFactory
import com.mapbox.maps.extension.style.layers.getLayer
import com.mapbox.maps.extension.style.sources.generated.GeoJsonSource
import com.mapbox.maps.extension.style.sources.getSource
import com.mapbox.maps.extension.style.layers.generated.symbolLayer
import com.mapbox.maps.extension.style.expressions.generated.Expression
import com.mapbox.geojson.FeatureCollection


@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapaScreen(
    navController: NavHostController,
    mapaViewModel: MapaViewModel = viewModel(
        factory = MapaViewModelFactory(
            ReportesRepository(RetrofitInstance.api)
        )
    )
){
    val context = LocalContext.current
    var mostrarFiltro by remember { mutableStateOf(false) }
    var tipoReporteFiltro by remember { mutableStateOf<String?>(null) }
    var estadoReporteFiltro by remember { mutableStateOf<String?>(null) }

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

    val mapaUiState by mapaViewModel.uiState.collectAsState()

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
                            contentDescription = stringResource(R.string.filtrar_button_description),
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

            ReportesMapComponent(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                uiState = mapaUiState,
                hasPermission = hasLocationPermission,
                tipoFiltro = tipoReporteFiltro,
                estadoFiltro = estadoReporteFiltro,
                onMapViewReady = { map -> mapView = map },
                onUserLocationChanged = { location -> userLocation = location },
                onReporteClicked = { reporteId ->
                    navController.navigate(Pantallas.ConsultarReporte.crearRuta(reporteId.toString()))
                }
            )

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
                        text = stringResource(R.string.mas_zoom),
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
                        text = stringResource(R.string.menos_zoom),
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
                        contentDescription = stringResource(R.string.recentrar_ubicacion_description),
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

// Función para mapear categorías (igual que en VerReportesScreen)
fun mapearCategoriaParaMapa(nombreBD: String?): String {
    val processedName = nombreBD?.lowercase()?.trim() ?: ""
    return when (processedName) {
        "huecos" -> "Hueco"
        "alumbrado publico" -> "Alumbrado"
        "basura acumulada" -> "Basura"
        "semaforo dañado" -> "Semáforo"
        "hidrante roto" -> "Hidrante"
        "alcantarilla sin tapa" -> "Alcantarilla"
        else -> nombreBD ?: "Desconocido"
    }
}

// Función para filtrar GeoJSON según los filtros aplicados
fun filtrarGeoJson(geoJsonData: String, tipoFiltro: String?, estadoFiltro: String?): String {
    if (tipoFiltro == null && estadoFiltro == null) {
        return geoJsonData
    }

    try {
        val featureCollection = FeatureCollection.fromJson(geoJsonData)
        val features = featureCollection.features() ?: emptyList()

        val featuresFiltradas = features.filter { feature ->
            try {
                // Obtener propiedades del feature
                val categoria = feature.getStringProperty("categoria") ?: ""
                val estadoRaw = feature.getStringProperty("estado") ?: ""
                val estado = estadoRaw.trim()

                // Mapear la categoría para comparación
                val categoriaMapeada = mapearCategoriaParaMapa(categoria)

                // Verificar si cumple con los filtros
                val coincideTipo = tipoFiltro == null || categoriaMapeada == tipoFiltro
                val coincideEstado = estadoFiltro == null || estado.equals(estadoFiltro, ignoreCase = true)

                coincideTipo && coincideEstado
            } catch (e: Exception) {
                Log.e("MapaScreen", "Error procesando feature: ${e.message}", e)
                false
            }
        }

        // Crear nuevo FeatureCollection con features filtradas
        val nuevoFeatureCollection = FeatureCollection.fromFeatures(featuresFiltradas)
        return nuevoFeatureCollection.toJson()
    } catch (e: Exception) {
        Log.e("MapaScreen", "Error crítico filtrando GeoJSON: ${e.message}", e)
        e.printStackTrace()
        return geoJsonData
    }
}

@Composable
fun ReportesMapComponent(
    modifier: Modifier = Modifier,
    uiState: MapaUiState,
    hasPermission: Boolean,
    tipoFiltro: String?,
    estadoFiltro: String?,
    onMapViewReady: (MapView) -> Unit,
    onUserLocationChanged: (Point) -> Unit,
    onReporteClicked: (Int) -> Unit
) {
    val context = LocalContext.current

    AndroidView(
        factory = { ctx ->
            val mapView = MapView(ctx)
            val mapboxMap = mapView.getMapboxMap()

            val bogotaCenter = Point.fromLngLat(-74.0817, 4.6097)
            val initialZoom = 10.0
            mapboxMap.setCamera(
                CameraOptions.Builder()
                    .center(bogotaCenter)
                    .zoom(initialZoom)
                    .build()
            )

            mapboxMap.loadStyleUri(Style.MAPBOX_STREETS) { style ->

                if (hasPermission) {
                    initLocationComponentWithCallback(mapView, onUserLocationChanged)
                }

                // Cargar íconos al iniciar el mapa
                cargarIconosEnMapa(style, ctx)

                mapboxMap.addOnMapClickListener { point ->

                    val queryGeometry = RenderedQueryGeometry(mapboxMap.pixelForCoordinate(point))
                    val queryOptions = RenderedQueryOptions(listOf("reportes-layer"), null)

                    mapboxMap.queryRenderedFeatures(queryGeometry, queryOptions) { features ->
                        if (features.value?.isNotEmpty() == true) {
                            val feature: Feature = features.value!![0].queriedFeature.feature
                            val reporteId = feature.getNumberProperty("id")?.toInt()

                            if (reporteId != null) {
                                onReporteClicked(reporteId)
                            }
                        }
                    }
                    true
                }

                if (uiState is MapaUiState.Success) {
                    val geoJsonFiltrado = filtrarGeoJson(uiState.geoJsonData, tipoFiltro, estadoFiltro)
                    actualizarFuenteGeoJson(style, geoJsonFiltrado, ctx)
                }

                if (uiState is MapaUiState.Error) {
                    Log.e("MapaComponentFactory", "Error al cargar GeoJSON: ${uiState.message}")
                }
            }

            onMapViewReady(mapView)
            mapView
        },
        update = { mapView ->
            if (hasPermission) {
                initLocationComponentWithCallback(mapView, onUserLocationChanged)
            }

            val style = mapView.getMapboxMap().style
            if (style != null && style.isStyleLoaded()) {
                if (uiState is MapaUiState.Success) {
                    val geoJsonFiltrado = filtrarGeoJson(uiState.geoJsonData, tipoFiltro, estadoFiltro)
                    actualizarFuenteGeoJson(style, geoJsonFiltrado, mapView.context)
                }
                if (uiState is MapaUiState.Error) {
                    Log.e("MapaComponentUpdate", "Error al cargar GeoJSON: ${uiState.message}")
                }
            }
        },
        modifier = modifier
    )
}

private fun actualizarFuenteGeoJson(style: Style, geoJsonData: String, context: android.content.Context) {
    try {
        val source = style.getSource("reportes-source") as? GeoJsonSource

        if (source == null) {
            Log.d("MapaScreen", "Creando fuente y capa de reportes")

            // Agregar la fuente
            style.addSource(
                geoJsonSource("reportes-source") {
                    data(geoJsonData)
                }
            )

            // Cargar todas las imágenes como íconos si no están ya cargadas
            if (style.getLayer("reportes-layer") == null) {
                val iconosCargados = cargarIconosEnMapa(style, context)

                if (iconosCargados) {
                    // Crear capa de símbolos con imágenes
                    crearCapaSimbolos(style)
                } else {
                    // Fallback: usar círculos de colores
                    Log.w("MapaScreen", "Usando círculos como fallback")
                    crearCapaCirculos(style)
                }
            }
        } else {
            Log.d("MapaScreen", "Actualizando datos de fuente existente")
            source.data(geoJsonData)
        }
    } catch (e: Exception) {
        Log.e("MapaScreen", "Error en actualizarFuenteGeoJson: ${e.message}", e)
    }
}

private fun crearCapaSimbolos(style: Style) {
    style.addLayer(
        symbolLayer("reportes-layer", "reportes-source") {
            iconImage(
                Expression.match {
                    get { literal("categoria") }
                    stop {
                        literal("Alcantarilla sin tapa")
                        literal("ciralcantarilla")
                    }
                    stop {
                        literal("Alumbrado Publico")
                        literal("ciralumbrado")
                    }
                    stop {
                        literal("Basura acumulada")
                        literal("cirbasura")
                    }
                    stop {
                        literal("Huecos")
                        literal("circarro")
                    }
                    stop {
                        literal("Hidrante roto")
                        literal("cirhidrante")
                    }
                    stop {
                        literal("Semaforo dañado")
                        literal("cirsemaforo")
                    }
                    literal("circarro") // default
                }
            )
            iconSize(0.15)
            iconAllowOverlap(true)
            iconIgnorePlacement(true)
        }
    )
    Log.d("MapaScreen", "Capa de símbolos creada correctamente")
}

private fun crearCapaCirculos(style: Style) {
    style.addLayer(
        circleLayer("reportes-layer", "reportes-source") {
            circleColor(
                Expression.match {
                    get { literal("categoria") }
                    stop {
                        literal("Alcantarilla")
                        literal("#FF6B35")
                    }
                    stop {
                        literal("Alumbrado público")
                        literal("#F7C948")
                    }
                    stop {
                        literal("Basura")
                        literal("#6A994E")
                    }
                    stop {
                        literal("Huecos")
                        literal("#BC4749")
                    }
                    stop {
                        literal("Hidrante roto")
                        literal("#4361EE")
                    }
                    stop {
                        literal("Semáforo")
                        literal("#F72585")
                    }
                    literal("#0077B6") // default
                }
            )
            circleRadius(10.0)
            circleStrokeColor("white")
            circleStrokeWidth(2.0)
        }
    )
    Log.d("MapaScreen", "Capa de círculos creada como fallback")
}

private fun cargarIconosEnMapa(style: Style, context: android.content.Context): Boolean {
    val iconos = mapOf(
        "ciralcantarilla" to R.drawable.ciralcantarilla,
        "ciralumbrado" to R.drawable.ciralumbrado,
        "cirbasura" to R.drawable.cirbasura,
        "circarro" to R.drawable.circarro,
        "cirhidrante" to R.drawable.cirhidrante,
        "cirsemaforo" to R.drawable.cirsemaforo
    )

    var todosExitosos = true

    iconos.forEach { (nombre, recurso) ->
        try {
            val bitmap = android.graphics.BitmapFactory.decodeResource(context.resources, recurso)
            if (bitmap != null) {
                style.addImage(nombre, bitmap)
                Log.d("MapaScreen", "Ícono $nombre cargado correctamente")
            } else {
                Log.e("MapaScreen", "Bitmap null para ícono $nombre - Recurso no encontrado")
                todosExitosos = false
            }
        } catch (e: Exception) {
            Log.e("MapaScreen", "Error cargando ícono $nombre: ${e.message}", e)
            todosExitosos = false
        }
    }

    return todosExitosos
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
                text = stringResource(R.string.mostrar_listado_text),
                color = WhiteFull,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.width(8.dp))
            Icon(
                imageVector = Icons.Default.KeyboardArrowUp,
                contentDescription = stringResource(R.string.mostrar_listado_description),
                tint = WhiteFull,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}