package com.example.urbanfix.screens

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.window.Dialog
import androidx.compose.material3.LocalTextStyle
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.navigation.NavHostController
import com.example.urbanfix.R
import com.example.urbanfix.data.UserPreferencesManager
import com.example.urbanfix.ui.theme.*
import com.mapbox.maps.MapView
import com.mapbox.maps.Style
import com.example.urbanfix.navigation.Pantallas
import com.mapbox.geojson.Point
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.MapOptions
import com.mapbox.maps.plugin.locationcomponent.OnIndicatorPositionChangedListener
import com.mapbox.maps.plugin.locationcomponent.location

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        val controller = WindowInsetsControllerCompat(window, window.decorView)
        controller.hide(WindowInsetsCompat.Type.systemBars())
        controller.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        setContent {
            UrbanFixTheme {
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavHostController) {
    val context = LocalContext.current
    val userPreferencesManager = remember { UserPreferencesManager(context) }
    val userName = remember { userPreferencesManager.getUserName() }
    var searchText by remember { mutableStateOf("") }
    var showLogoutDialog by remember { mutableStateOf(false) }

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
        onResult = { isGranted -> hasLocationPermission = isGranted }
    )

    LaunchedEffect(Unit) {
        if (!hasLocationPermission) {
            permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF1FAEE))
    ) {

        Image(
            painter = painterResource(id = R.drawable.part_arriba_menu),
            contentDescription = "Decoración superior",
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .align(Alignment.TopCenter),
            contentScale = ContentScale.FillWidth
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            Image(
                painter = painterResource(id = R.drawable.urbanfixlogomenu),
                contentDescription = stringResource(R.string.urbanfix_logo_description),
                modifier = Modifier
                    .height(40.dp)
                    .padding(start=10.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.back_search),
                    contentDescription = "Fondo barra de búsqueda",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(61.dp)
                        .align(Alignment.Center),
                    contentScale = ContentScale.FillBounds
                )
                OutlinedTextField(
                    value = searchText,
                    onValueChange = { searchText = it },
                    placeholder = {
                        Text(
                            text = stringResource(R.string.search_placeholder),
                            color = Color(0xFF666666),
                            fontSize = 13.sp,
                            modifier = Modifier
                                .offset(y = (-3).dp)
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(51.dp)
                        .padding(horizontal = 8.dp)
                        .align(Alignment.Center)
                        .clip(RoundedCornerShape(12.dp)),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedContainerColor = WhiteFull,
                        focusedContainerColor = WhiteFull,
                        unfocusedBorderColor = Color.Transparent,
                        focusedBorderColor = Color.Transparent
                    ),
                    trailingIcon = {
                        IconButton(onClick = {
                            navController.navigate(Pantallas.Verperfilempresa.ruta)
                        }) {
                            Icon(
                                Icons.Default.Search,
                                contentDescription = stringResource(R.string.search_button_description),
                                tint = PurpleMain
                            )
                        }
                    },
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    textStyle = LocalTextStyle.current.copy(fontSize = 13.sp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(topStart = 1.dp, topEnd = 1.dp))
                        .background(Color.Transparent)
                        .padding(1.dp)
                        .padding(bottom = 1.dp)
                ) {
                    Spacer(modifier = Modifier.height(58.dp))
                    Text(
                        text = stringResource(R.string.greeting_user, userName),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = BlackFull
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Card(
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier
                            .width(318.dp)
                            .height(168.dp)
                            .align(Alignment.CenterHorizontally)
                            .clickable {
                                navController.navigate("mapa_detalle")
                            },
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                        colors = CardDefaults.cardColors(containerColor = WhiteFull)
                    ) {
                        MapboxMapComponent(
                            modifier = Modifier.fillMaxSize(),
                            hasPermission = hasLocationPermission
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Text(
                        text = stringResource(R.string.report_title_2),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = BlackFull
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    // Primera fila de botones con navegación
                    Row(
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        ReportButton(
                            iconId = R.drawable.huecos,
                            text = stringResource(R.string.category_potholes),
                            onClick = { navController.navigate("reportar/huecos") }
                        )
                        ReportButton(
                            iconId = R.drawable.alumbrado,
                            text = stringResource(R.string.category_lighting),
                            onClick = { navController.navigate("reportar/alumbrado") }
                        )
                        ReportButton(
                            iconId = R.drawable.basura,
                            text = stringResource(R.string.category_trash),
                            onClick = { navController.navigate("reportar/basura") }
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Segunda fila de botones con navegación
                    Row(
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        ReportButton(
                            iconId = R.drawable.semaforo,
                            text = stringResource(R.string.category_traffic_light),
                            onClick = { navController.navigate("reportar/semaforo") }
                        )
                        ReportButton(
                            iconId = R.drawable.hidrante,
                            text = stringResource(R.string.category_hydrant),
                            onClick = { navController.navigate("reportar/hidrante") }
                        )
                        ReportButton(
                            iconId = R.drawable.alcantarilla,
                            text = stringResource(R.string.category_sewer),
                            onClick = { navController.navigate("reportar/alcantarilla") }
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))
                    Row(
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.btn_mis_apoyos),
                            contentDescription = stringResource(R.string.my_supports),
                            modifier = Modifier
                                .width(167.dp)
                                .height(48.dp)
                                .clickable {navController.navigate(Pantallas.MisApoyos.ruta) }
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Image(
                            painter = painterResource(id = R.drawable.btn_mis_denuncias),
                            contentDescription = stringResource(R.string.my_reports_button),
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .clickable { }
                        )
                    }
                }

                Row(
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(top = 1.dp)
                ) {
                    IconButton(
                        onClick = { navController.navigate(Pantallas.Verperfilusuario.ruta) },
                        modifier = Modifier
                            .size(55.dp)
                            .clip(CircleShape)
                            .background(AquaSoft)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.preguntas),
                            contentDescription = stringResource(R.string.help_button_description),
                            modifier = Modifier.size(35.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    IconButton(
                        onClick = { showLogoutDialog = true },
                        modifier = Modifier
                            .size(55.dp)
                            .clip(CircleShape)
                            .background(AquaSoft)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.salir_png),
                            contentDescription = stringResource(R.string.exit_button_description),
                            modifier = Modifier.size(35.dp)
                        )
                    }
                }
            }
        }

        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
        ) {
            BottomNavBar(navController = navController)
        }
    }

    // Diálogo de confirmación de cerrar sesión
    if (showLogoutDialog) {
        LogoutConfirmationDialog(
            onConfirm = {
                userPreferencesManager.clearCredentials()
                showLogoutDialog = false
                navController.navigate(Pantallas.Login.ruta) {
                    popUpTo(0) { inclusive = true }
                }
            },
            onDismiss = {
                showLogoutDialog = false
            }
        )
    }
}

@Composable
fun LogoutConfirmationDialog(
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
                        text = stringResource(R.string.logout_confirmation_title),
                        color = Color.Black,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Text(
                    text = stringResource(R.string.logout_confirmation_message),
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
                            text = stringResource(R.string.logout_no_button),
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
                            text = stringResource(R.string.logout_yes_button),
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
fun MapDetailScreen(navController: NavHostController) {
    val context = LocalContext.current

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

    Box(modifier = Modifier.fillMaxSize()) {
        MapboxMapComponent(
            modifier = Modifier.fillMaxSize(),
            hasPermission = hasLocationPermission
        )
        IconButton(
            onClick = { navController.popBackStack() },
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(16.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.8f))
        ) {
            Icon(
                Icons.Default.ArrowBack,
                contentDescription = stringResource(R.string.back_button_content_description),
                tint = BlackFull
            )
        }
    }
}

@Composable
fun MapboxMapComponent(modifier: Modifier = Modifier, hasPermission: Boolean) {
    AndroidView(
        factory = { context ->
            // 1. Crea el MapView
            val mapView = MapView(context)

            // 2. Obtiene la instancia de MapboxMap
            val mapboxMap = mapView.getMapboxMap()

            // --- PASO 1: Centrar en Bogotá ---
            // 3. Define las coordenadas y el zoom inicial
            val bogotaCenter = Point.fromLngLat(-74.0817, 4.6097)
            val initialZoom = 10.0

            // 4. Llama a setCamera para centrar en Bogotá INMEDIATAMENTE
            mapboxMap.setCamera(
                CameraOptions.Builder()
                    .center(bogotaCenter)
                    .zoom(initialZoom)
                    .build()
            )
            // --- Fin Paso 1 ---
            // 5. Carga el estilo. La lógica del Paso 2 (usuario) está en initLocationComponent
            mapboxMap.loadStyleUri(Style.MAPBOX_STREETS) {
                if (hasPermission) {
                    initLocationComponent(mapView)
                }
            }

            // 6. Devuelve el MapView
            mapView
        },
        update = { mapView ->
            // Se llama si hasPermission cambia
            if (hasPermission) {
                initLocationComponent(mapView)
            }
        },
        modifier = modifier
    )
}
private fun initLocationComponent(mapView: MapView) {
    val context = mapView.context
    if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

        val locationComponentPlugin = mapView.location

        // Habilita el punto azul
        locationComponentPlugin.updateSettings {
            this.enabled = true
            this.pulsingEnabled = true
        }

        // --- PASO 2: Moverse al Usuario ---
        // Creamos un listener que se disparará cuando se encuentre la ubicación
        val listener = object : OnIndicatorPositionChangedListener {
            override fun onIndicatorPositionChanged(point: Point) {

                // 1. Mueve la cámara a la nueva 'point' (ubicación del usuario)
                mapView.getMapboxMap().setCamera(
                    CameraOptions.Builder()
                        .center(point)
                        .zoom(14.0) // Un zoom más cercano
                        .build()
                )
                locationComponentPlugin.removeOnIndicatorPositionChangedListener(this)
            }
        }

        // 3. Añadimos el listener al plugin de ubicación
        locationComponentPlugin.addOnIndicatorPositionChangedListener(listener)
        // --- Fin Paso 2 ---
    }
}

// ReportButton ACTUALIZADO con onClick
@Composable
fun ReportButton(iconId: Int, text: String, onClick: () -> Unit = {}) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = PurpleMain),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        modifier = Modifier
            .width(100.dp)
            .height(100.dp)
            .clickable(onClick = onClick)  // CAMBIO AQUÍ
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp)
        ) {
            Image(
                painter = painterResource(id = iconId),
                contentDescription = text,
                modifier = Modifier.size(40.dp)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = text,
                color = WhiteFull,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center,
                lineHeight = 12.sp
            )
        }
    }
}

@Composable
fun BottomNavBar(navController: NavHostController) {
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
            selected = false, // Ajusta esta lógica si es necesario
            onClick = { navController.navigate(Pantallas.Home.ruta) },
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
            selected = false, // Ajusta esta lógica si es necesario
            // CAMBIO: La lógica ahora está dentro del onClick.
            onClick = {
                // 1. Obtenemos el ID del usuario guardado en SharedPreferences.
                val userId = userPreferencesManager.getUserId()

                // 2. Comprobamos que el ID sea válido (no -1).
                if (userId != -1) {
                    // 3. Usamos la función `crearRuta` para navegar con el ID real.
                    navController.navigate(Pantallas.MisReportes.crearRuta(userId))
                } else {
                    // Si no hay usuario, mostramos un mensaje para evitar el crash.
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
            onClick = { /* Lógica para notificaciones */ },
            icon = {
                Image(
                    painter = painterResource(id = R.drawable.notificaciones),
                    contentDescription = stringResource(R.string.nav_notifications),
                    modifier = Modifier.size(26.dp),
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
        Spacer(modifier = Modifier.width(3.dp))
    }
}