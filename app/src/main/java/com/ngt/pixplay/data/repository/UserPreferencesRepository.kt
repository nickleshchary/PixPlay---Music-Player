package com.ngt.pixplay.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserPreferencesRepository @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {
    private object PreferencesKeys {
        val IS_LIBRARY_GRID = booleanPreferencesKey("is_library_grid")
    }

    val isLibraryGrid: Flow<Boolean> = dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[PreferencesKeys.IS_LIBRARY_GRID] ?: false
        }

    suspend fun setLibraryGrid(isGrid: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.IS_LIBRARY_GRID] = isGrid
        }
    }
}
