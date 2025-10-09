package com.example.urbanfix.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.urbanfix.screens.HomeScreen
import com.example.urbanfix.screens.BienvenidaScreen
import com.example.urbanfix.screens.LoginScreen
import com.example.urbanfix.screens.RegistroScreen
import com.example.urbanfix.screens.OlvidarconScreen
import com.example.urbanfix.screens.RegEmpresaScreen
import com.example.urbanfix.screens.RegUsuarioScreen

sealed class Pantallas(val ruta: String) {
    object Bienvenida : Pantallas("bienvenida")
    object Login : Pantallas("login")
    object Registro : Pantallas("registro")
    object Olvido : Pantallas("olvido")
    object Home : Pantallas("home")
    object RegUsuario : Pantallas("regusuario")

    object RegEmpresa : Pantallas("regempresa")
}


@Composable
fun AppNavigator(navController: NavHostController) {
    NavHost(navController = navController, startDestination = Pantallas.Bienvenida.ruta) {
        composable(Pantallas.Bienvenida.ruta) {
            BienvenidaScreen(navController)
        }
        composable(Pantallas.Login.ruta) {
            LoginScreen(navController)
        }
        composable(Pantallas.Registro.ruta) {
            RegistroScreen(navController)
        }
        composable(Pantallas.Olvido.ruta) {
            OlvidarconScreen(navController)
        }
        composable(Pantallas.Home.ruta) {
            HomeScreen(navController)
        }
        composable(Pantallas.RegUsuario.ruta) {
            RegUsuarioScreen(navController)
        }
        composable(Pantallas.RegEmpresa.ruta) {
            RegEmpresaScreen(navController)
        }
    }
}