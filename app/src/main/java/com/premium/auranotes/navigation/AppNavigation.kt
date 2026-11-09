package com.premium.auranotes.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.premium.auranotes.ui.screens.AboutScreen
import com.premium.auranotes.ui.screens.EditorScreen
import com.premium.auranotes.ui.screens.HomeScreen
import com.premium.auranotes.ui.screens.SettingsScreen
import com.premium.auranotes.ui.screens.SplashScreen

@Composable
fun AppNavigation(
    initialNoteId: Int = 0
) {
    val navController = rememberNavController()
    val startDest = if (initialNoteId != 0) "editor?noteId=$initialNoteId" else "splash"

    NavHost(navController = navController, startDestination = startDest) {
        composable("splash") {
            SplashScreen(navController = navController)
        }
        composable("home") {
            HomeScreen(navController = navController)
        }
        composable("about") {
            AboutScreen(navController = navController)
        }
        composable("settings") {
            SettingsScreen(navController = navController)
        }
        composable(
            route = "editor?noteId={noteId}&initialType={initialType}",
            arguments = listOf(
                navArgument("noteId") {
                    type = NavType.IntType
                    defaultValue = 0
                },
                navArgument("initialType") {
                    type = NavType.StringType
                    defaultValue = "TEXT"
                    nullable = true
                }
            )
        ) { backStackEntry ->
            val noteId = backStackEntry.arguments?.getInt("noteId") ?: 0
            EditorScreen(
                navController = navController,
                noteId = noteId
            )
        }
    }
}
