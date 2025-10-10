package com.example.urbanfix.screens

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
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
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.navigation.NavHostController
import com.example.urbanfix.R
import com.example.urbanfix.ui.theme.*
import com.mapbox.maps.MapView
import com.mapbox.maps.Style
import com.example.urbanfix.navigation.Pantallas
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(BlueMain)
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.urbanfixlogomenu),
                    contentDescription = stringResource(R.string.urbanfix_logo_description),
                    modifier = Modifier.height(40.dp)
                )
                Spacer(modifier = Modifier.weight(1f))
                IconButton(
                    onClick = { },
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(AquaSoft)

                ) {
                    Image(
                        painter = painterResource(id = R.drawable.preguntas),
                        contentDescription = stringResource(R.string.help_button_description),
                        modifier = Modifier.size(28.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                IconButton(
                    onClick = { },
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(AquaSoft)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.salir_png),
                        contentDescription = stringResource(R.string.exit_button_description),
                        modifier = Modifier.size(28.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            OutlinedTextField(
                value = "",
                onValueChange = {},
                placeholder = {
                    Text(
                        stringResource(R.string.search_placeholder),
                        color = GrayMedium,
                        fontSize = 14.sp
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .clip(RoundedCornerShape(30.dp)),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = WhiteFull,
                    focusedContainerColor = WhiteFull,
                    unfocusedBorderColor = Color.Transparent,
                    focusedBorderColor = Color.Transparent
                ),
                trailingIcon = {
                    IconButton(onClick = { }) {
                        Icon(
                            Icons.Default.Search,
                            contentDescription = stringResource(R.string.search_button_description),
                            tint = PurpleMain
                        )
                    }
                },
                shape = RoundedCornerShape(30.dp)
            )

            Spacer(modifier = Modifier.height(20.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clip(RoundedCornerShape(topStart = 30.dp, topEnd = 30.dp))
                    .background(WhiteFull)
                    .padding(20.dp)
                    .padding(bottom = 80.dp)
            ) {
                Text(
                    text = stringResource(R.string.greeting_user, "Dylan"),
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
                    text = stringResource(R.string.report_title),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = BlackFull
                )
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    ReportButton(R.drawable.huecos, stringResource(R.string.category_potholes))
                    ReportButton(R.drawable.alumbrado, stringResource(R.string.category_lighting))
                    ReportButton(R.drawable.basura, stringResource(R.string.category_trash))
                }
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    ReportButton(R.drawable.semaforo, stringResource(R.string.category_traffic_light))
                    ReportButton(R.drawable.hidrante, stringResource(R.string.category_hydrant))
                    ReportButton(R.drawable.alcantarilla, stringResource(R.string.category_sewer))
                }
                Spacer(modifier = Modifier.height(20.dp))
                Row(
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Button(
                        onClick = { },
                        colors = ButtonDefaults.buttonColors(containerColor = PurpleMain),
                        shape = RoundedCornerShape(25.dp),
                        modifier = Modifier
                            .width(167.dp)
                            .height(48.dp)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.apoyo),
                            contentDescription = stringResource(R.string.supports_icon_description),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            stringResource(R.string.my_supports),
                            color = WhiteFull,
                            fontWeight = FontWeight.Medium,
                            fontSize = 14.sp
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Button(
                        onClick = { },
                        colors = ButtonDefaults.buttonColors(containerColor = BlueMain),
                        shape = RoundedCornerShape(25.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.denuncia),
                            contentDescription = stringResource(R.string.reports_icon_description),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            stringResource(R.string.my_reports_button),
                            color = WhiteFull,
                            fontWeight = FontWeight.Medium,
                            fontSize = 14.sp
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
            MapView(context).apply {
                getMapboxMap().loadStyleUri(Style.MAPBOX_STREETS) {
                    if (hasPermission) {
                        initLocationComponent(this)
                    }
                }
            }
        },
        update = { mapView ->
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
        locationComponentPlugin.updateSettings {
            this.enabled = true
            this.pulsingEnabled = true
        }
    }
}

@Composable
fun ReportButton(iconId: Int, text: String) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = PurpleMain),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        modifier = Modifier
            .width(100.dp)
            .height(100.dp)
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
    NavigationBar(
        containerColor = BlueMain,
        modifier = Modifier
            .fillMaxWidth()
            .height(70.dp)
    ) {
        Spacer(modifier = Modifier.width(3.dp))
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
            onClick = { },
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
            onClick = { navController.navigate(Pantallas.Perfil.ruta) },
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