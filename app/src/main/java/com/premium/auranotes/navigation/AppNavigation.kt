package com.premium.auranotes.navigation
import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.premium.auranotes.ui.screens.HomeScreen
import com.premium.auranotes.ui.screens.EditorScreen
import com.premium.auranotes.ui.screens.SplashScreen
import com.premium.auranotes.ui.screens.AboutScreen

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = "splash") {
        composable("splash") { SplashScreen(navController = navController) }
        composable("home") { HomeScreen(navController = navController) }
        composable("about") { AboutScreen(navController = navController) }
        composable(
            route = "editor?noteId={noteId}",
            arguments = listOf(navArgument("noteId") { type = NavType.IntType; defaultValue = 0 })
        ) { backStackEntry ->
            EditorScreen(navController = navController, noteId = backStackEntry.arguments?.getInt("noteId") ?: 0)
        }
    }
}
