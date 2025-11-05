package com.example.urbanfix.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.NavType
import com.example.urbanfix.screens.*

/* ---------------------------
   Definición de pantallas
----------------------------*/
sealed class Pantallas(val ruta: String) {
    object Bienvenida : Pantallas("bienvenida")
    object Login : Pantallas("login")
    object Registro : Pantallas("registro")
    object Olvido : Pantallas("olvido")
    object Home : Pantallas("home")
    object RegUsuario : Pantallas("regusuario")
    object RegEmpresa : Pantallas("regempresa")
    object Perfil : Pantallas("perfil")
    object Fotoperfil : Pantallas("fotoperfil")
    object EditProfile : Pantallas("edit_profile")
    object PreguntasFrec : Pantallas("preguntas_frec")

    // NUEVO: Ruta para ver perfil de otro usuario
    object Verperfilempresa : Pantallas("verperfilempresa/{userId}") {
        fun crearRuta(userId: Int) = "verperfilempresa/$userId"
        val rutaSinParametro = "verperfilempresa" // Para compatibilidad con llamadas sin ID
    }

    object Verperfilusuario : Pantallas("ver_perfil_usuario/{userId}/{userRole}") {
        fun crearRuta(userId: Int, userRole: String = "usuario") =
            "ver_perfil_usuario/$userId/$userRole"
    }
    object VerReportes : Pantallas("verreportes")
    object Mapa : Pantallas("mapa")

    object MisApoyos : Pantallas("misapoyos")
    object MisDenuncias : Pantallas("misdenuncias")

    object MisReportes : Pantallas("misreportes/{userId}") {
        fun crearRuta(userId: Int) = "misreportes/$userId"
    }

    object Reportar : Pantallas("reportar/{reportType}") {
        // Ruta para CREAR (sin ID)
        fun crearRuta(reportType: String) = "reportar/$reportType"

        // Ruta para EDITAR (con ID)
        fun crearRutaConId(reportType: String, reporteId: Int) =
            "reportar/$reportType?reporteId=$reporteId"
    }

    object ConsultarReporte : Pantallas("consultar_reporte_screen/{reportId}") {
        fun crearRuta(reportId: String) = "consultar_reporte_screen/$reportId"
    }

    object ReportarDos : Pantallas("reportar_dos/{reportType}") {
        fun crearRuta(reportType: String) = "reportar_dos/$reportType"
    }

    object EditarReporte : Pantallas("editar_reporte_screen/{reportId}") {
        fun crearRuta(reportId: String) = "editar_reporte_screen/$reportId"
    }
}

/* ---------------------------
   Navegación principal
----------------------------*/
@Composable
fun AppNavigator(navController: NavHostController) {
    NavHost(navController = navController, startDestination = Pantallas.Bienvenida.ruta) {

        composable(Pantallas.Bienvenida.ruta) { BienvenidaScreen(navController) }
        composable(Pantallas.Login.ruta) { LoginScreen(navController) }
        composable(Pantallas.Registro.ruta) { RegistroScreen(navController) }
        composable(Pantallas.Olvido.ruta) { OlvidarconScreen(navController) }
        composable(Pantallas.Home.ruta) { HomeScreen(navController) }
        composable(Pantallas.RegUsuario.ruta) { RegUsuarioScreen(navController) }
        composable(Pantallas.RegEmpresa.ruta) { RegEmpresaScreen(navController) }
        composable(Pantallas.Fotoperfil.ruta) { FotoperfilScreen(navController) }
        composable(Pantallas.Verperfilempresa.ruta) { VerperfilempresaScreen(navController) }
        composable(Pantallas.VerReportes.ruta) { VerReportesScreen(navController) }
        composable(Pantallas.Mapa.ruta) { MapaScreen(navController) }
        composable(Pantallas.PreguntasFrec.ruta) {  PreguntasFrecuentesScreen(navController) }

        composable(Pantallas.MisApoyos.ruta) {
            MisApoyosScreen(navController = navController)
        }
        composable(Pantallas.MisDenuncias.ruta) {
            MisDenunciasScreen(navController = navController)
        }

        composable(
            route = Pantallas.MisReportes.ruta,
            arguments = listOf(
                navArgument("userId") {
                    type = NavType.IntType
                    defaultValue = 0
                }
            )
        ) { backStackEntry ->
            val userId = backStackEntry.arguments?.getInt("userId") ?: 0
            MisReportesScreen(navController = navController, userId = userId)
        }

        composable(route = Pantallas.Perfil.ruta) {
            ProfileScreen(navController = navController)
        }

        composable(route = Pantallas.EditProfile.ruta) {
            EditProfileScreen(navController = navController)
        }

        /* ---------------------------
           RUTA: Ver perfil de otro usuario
        ----------------------------*/
        composable(
            route = Pantallas.Verperfilempresa.ruta,
            arguments = listOf(
                navArgument("userId") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId")
            VerperfilempresaScreen(
                navController = navController,
                userId = userId
            )
        }

        composable(
            route = Pantallas.Verperfilusuario.ruta,
            arguments = listOf(
                navArgument("userId") {
                    type = NavType.StringType
                },
                navArgument("userRole") {
                    type = NavType.StringType
                    defaultValue = "usuario"
                }
            )
        ) { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId")
            val userRole = backStackEntry.arguments?.getString("userRole") ?: "usuario"

            VerperfilusuarioScreen(
                navController = navController,
                userId = userId,
                userRole = userRole
            )
        }

        /* ---------------------------
           RUTA: ReportarScreen (Pantalla 1)
        ----------------------------*/
        composable(
            route = Pantallas.Reportar.ruta + "?reporteId={reporteId}",
            arguments = listOf(
                navArgument("reportType") {
                    type = NavType.StringType
                    defaultValue = "huecos"
                },
                navArgument("reporteId") {
                    type = NavType.IntType
                    defaultValue = -1
                }
            )
        ) { backStackEntry ->
            val reportType = backStackEntry.arguments?.getString("reportType") ?: "huecos"
            val reporteId = backStackEntry.arguments?.getInt("reporteId") ?: -1 // Obtenemos el ID

            ReportarScreen(
                navController = navController,
                reportType = reportType,
                reporteId = reporteId
            )
        }

        /* ---------------------------
           RUTA: ReportarDosScreen (Pantalla 2)
        ----------------------------*/
        composable(
            route = Pantallas.ReportarDos.ruta,
            arguments = listOf(
                navArgument("reportType") {
                    type = NavType.StringType
                    defaultValue = "huecos"
                }
            )
        ) { backStackEntry ->
            val reportType = backStackEntry.arguments?.getString("reportType") ?: "huecos"
            ReportarDosScreen(navController = navController, reportType = reportType)
        }

        /* ---------------------------
           RUTA: EditarReporteScreen (Detalles del Reporte)
        ----------------------------*/
        composable(
            route = Pantallas.EditarReporte.ruta,
            arguments = listOf(
                navArgument("reportId") {
                    type = NavType.StringType
                    defaultValue = "2025-0001"
                }
            )
        ) { backStackEntry ->
            val reportId = backStackEntry.arguments?.getString("reportId") ?: "2025-0001"
            EditarReporteScreen(navController = navController, reportId = reportId)
        }

        /* ---------------------------
           RUTA: MapDetailScreen
        ----------------------------*/
        composable("mapa_detalle") {
            MapDetailScreen(navController = navController)
        }
        composable(
            route = Pantallas.ConsultarReporte.ruta,
            arguments = listOf(
                navArgument("reportId") {
                    type = NavType.StringType
                    defaultValue = ""
                }
            )
        ) { backStackEntry ->
            val reportId = backStackEntry.arguments?.getString("reportId") ?: ""
            ConsultarReporteScreen(navController = navController, reportId = reportId)
        }
    }
}