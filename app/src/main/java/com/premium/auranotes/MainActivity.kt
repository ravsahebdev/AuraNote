package com.premium.auranotes

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.biometric.BiometricPrompt
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.fragment.app.FragmentActivity
import com.premium.auranotes.data.PreferencesManager
import com.premium.auranotes.data.ThemeMode
import com.premium.auranotes.navigation.AppNavigation
import com.premium.auranotes.reminders.ReminderReceiver
import com.premium.auranotes.ui.theme.AuraNotesTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : FragmentActivity() {

    @Inject
    lateinit var preferencesManager: PreferencesManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)

        val noteIdFromNotification = intent.getIntExtra(ReminderReceiver.EXTRA_NOTE_ID, 0)

        setContent {
            val userPrefs by preferencesManager.userPreferencesFlow.collectAsState(
                initial = com.premium.auranotes.data.UserPreferences()
            )

            val isDark = when (userPrefs.themeMode) {
                ThemeMode.SYSTEM -> isSystemInDarkTheme()
                ThemeMode.LIGHT -> false
                ThemeMode.DARK -> true
            }

            AuraNotesTheme(
                darkTheme = isDark,
                dynamicColor = userPrefs.dynamicColor
            ) {
                AppNavigation(initialNoteId = noteIdFromNotification)
            }
        }
    }
}
