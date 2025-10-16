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
    object Verperfilempresa : Pantallas("verperfilempresa")
    object Verperfilusuario : Pantallas("verperfilusuario")

    // 👇 Nueva pantalla con parámetro
    object Reportar : Pantallas("reportar/{reportType}") {
        fun crearRuta(reportType: String) = "reportar/$reportType"
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
        composable(Pantallas.Verperfilusuario.ruta) { VerperfilusuarioScreen(navController) }

        composable(route = Pantallas.Perfil.ruta) {
            ProfileScreen(navController = navController)
        }

        composable(route = Pantallas.EditProfile.ruta) {
            EditProfileScreen(navController = navController)
        }

        /* ---------------------------
           NUEVA RUTA: ReportarScreen
        ----------------------------*/
        composable(
            route = Pantallas.Reportar.ruta,
            arguments = listOf(
                navArgument("reportType") {
                    type = NavType.StringType
                    defaultValue = "huecos" // Valor por defecto
                }
            )
        ) { backStackEntry ->
            val reportType = backStackEntry.arguments?.getString("reportType") ?: "huecos"
            ReportarScreen(navController = navController, reportType = reportType)
        }
    }
}

