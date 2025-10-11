package com.example.urbanfix.navigation

import android.R.attr.defaultValue
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
import com.example.urbanfix.screens.UserProfileScreen
import com.example.urbanfix.screens.EditProfileScreen
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.navArgument
import com.example.urbanfix.screens.CompanyProfileScreen
import com.example.urbanfix.screens.EditCompanyProfileScreen
import com.example.urbanfix.screens.FotoperfilScreen
import com.example.urbanfix.screens.VerperfilempresaScreen
import com.example.urbanfix.screens.VerperfilusuarioScreen
import com.example.urbanfix.viewmodel.CompanyProfileViewModel
import com.example.urbanfix.viewmodel.UserProfileViewModel

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
    object CompanyProfile : Pantallas("company_profile")

    object EditCompanyProfile : Pantallas("edit_company_profile")
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
        composable(Pantallas.Fotoperfil.ruta) {
            FotoperfilScreen(navController)
        }
        composable(Pantallas.Verperfilempresa.ruta) {
            VerperfilempresaScreen(navController)
        }
        composable(Pantallas.Verperfilusuario.ruta) {
            VerperfilusuarioScreen(navController)
        }
        composable(route = Pantallas.Perfil.ruta) {
            UserProfileScreen(
                navController = navController
            )
        }
        composable(route = Pantallas.EditProfile.ruta) {
            EditProfileScreen(
                navController = navController
            )
        }
        composable(Pantallas.CompanyProfile.ruta) {
            CompanyProfileScreen(
                navController = navController
            )
        }
        composable(Pantallas.EditCompanyProfile.ruta) {
            EditCompanyProfileScreen(navController = navController)
        }
    }
}