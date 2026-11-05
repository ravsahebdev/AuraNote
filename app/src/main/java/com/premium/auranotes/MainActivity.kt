package com.premium.auranotes
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.view.WindowCompat
import com.premium.auranotes.ui.theme.AuraNotesTheme
import com.premium.auranotes.navigation.AppNavigation
import dagger.hilt.android.AndroidEntryPoint
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContent {
            AuraNotesTheme {
                AppNavigation()
            }
        }
    }
}
