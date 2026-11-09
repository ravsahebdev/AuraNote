package com.premium.auranotes.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "aura_notes_preferences")

enum class ThemeMode {
    SYSTEM,
    LIGHT,
    DARK
}

enum class ViewMode {
    GRID,
    LIST
}

enum class SortOrder {
    DATE_MODIFIED,
    DATE_CREATED,
    TITLE_AZ,
    TITLE_ZA
}

data class UserPreferences(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val dynamicColor: Boolean = false,
    val viewMode: ViewMode = ViewMode.GRID,
    val defaultColor: Long = 0xFF1E1E1E,
    val sortOrder: SortOrder = SortOrder.DATE_MODIFIED,
    val isAppLockEnabled: Boolean = false
)

@Singleton
class PreferencesManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val dataStore = context.dataStore

    private object Keys {
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val DYNAMIC_COLOR = booleanPreferencesKey("dynamic_color")
        val VIEW_MODE = stringPreferencesKey("view_mode")
        val DEFAULT_COLOR = longPreferencesKey("default_color")
        val SORT_ORDER = stringPreferencesKey("sort_order")
        val APP_LOCK_ENABLED = booleanPreferencesKey("app_lock_enabled")
    }

    val userPreferencesFlow: Flow<UserPreferences> = dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            val themeStr = preferences[Keys.THEME_MODE] ?: ThemeMode.SYSTEM.name
            val themeMode = try { ThemeMode.valueOf(themeStr) } catch (e: Exception) { ThemeMode.SYSTEM }

            val dynamicColor = preferences[Keys.DYNAMIC_COLOR] ?: false

            val viewStr = preferences[Keys.VIEW_MODE] ?: ViewMode.GRID.name
            val viewMode = try { ViewMode.valueOf(viewStr) } catch (e: Exception) { ViewMode.GRID }

            val defaultColor = preferences[Keys.DEFAULT_COLOR] ?: 0xFF1E1E1E

            val sortStr = preferences[Keys.SORT_ORDER] ?: SortOrder.DATE_MODIFIED.name
            val sortOrder = try { SortOrder.valueOf(sortStr) } catch (e: Exception) { SortOrder.DATE_MODIFIED }

            val appLock = preferences[Keys.APP_LOCK_ENABLED] ?: false

            UserPreferences(
                themeMode = themeMode,
                dynamicColor = dynamicColor,
                viewMode = viewMode,
                defaultColor = defaultColor,
                sortOrder = sortOrder,
                isAppLockEnabled = appLock
            )
        }

    suspend fun setThemeMode(mode: ThemeMode) {
        dataStore.edit { preferences ->
            preferences[Keys.THEME_MODE] = mode.name
        }
    }

    suspend fun setDynamicColor(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[Keys.DYNAMIC_COLOR] = enabled
        }
    }

    suspend fun setViewMode(mode: ViewMode) {
        dataStore.edit { preferences ->
            preferences[Keys.VIEW_MODE] = mode.name
        }
    }

    suspend fun setDefaultColor(color: Long) {
        dataStore.edit { preferences ->
            preferences[Keys.DEFAULT_COLOR] = color
        }
    }

    suspend fun setSortOrder(order: SortOrder) {
        dataStore.edit { preferences ->
            preferences[Keys.SORT_ORDER] = order.name
        }
    }

    suspend fun setAppLockEnabled(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[Keys.APP_LOCK_ENABLED] = enabled
        }
    }
}
