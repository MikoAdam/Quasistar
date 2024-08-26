package com.quasistar.settings

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.io.IOException

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class SettingsManager(context: Context) {

    private val dataStore = context.dataStore

    companion object {
        private val VIBRATION_KEY = booleanPreferencesKey("vibration_enabled")
        private val WINNING_CONDITION_KEY = intPreferencesKey("winning_condition")
        private val SHOW_LABELS_KEY = booleanPreferencesKey("show_labels")

        // Getters
        suspend fun getVibrationEnabled(context: Context): Boolean {
            val dataStore = context.dataStore
            return dataStore.data
                .catch { exception ->
                    if (exception is IOException) {
                        emit(emptyPreferences())
                    } else {
                        throw exception
                    }
                }
                .map { preferences ->
                    preferences[VIBRATION_KEY] ?: true // Default to true if not set
                }.first()
        }

        suspend fun getWinningCondition(context: Context): Int {
            val dataStore = context.dataStore
            return dataStore.data
                .catch { exception ->
                    if (exception is IOException) {
                        emit(emptyPreferences())
                    } else {
                        throw exception
                    }
                }
                .map { preferences ->
                    preferences[WINNING_CONDITION_KEY] ?: 1 // Default to 1 if not set
                }.first()
        }

        suspend fun getShowLabels(context: Context): Boolean {
            val dataStore = context.dataStore
            return dataStore.data
                .catch { exception ->
                    if (exception is IOException) {
                        emit(emptyPreferences())
                    } else {
                        throw exception
                    }
                }
                .map { preferences ->
                    preferences[SHOW_LABELS_KEY] ?: false // Default to false if not set
                }.first()
        }

        // Setters
        suspend fun setVibrationEnabled(context: Context, enabled: Boolean) {
            val dataStore = context.dataStore
            dataStore.edit { preferences ->
                preferences[VIBRATION_KEY] = enabled
            }
        }

        suspend fun setWinningCondition(context: Context, condition: Int) {
            val dataStore = context.dataStore
            dataStore.edit { preferences ->
                preferences[WINNING_CONDITION_KEY] = condition
            }
        }

        suspend fun setShowLabels(context: Context, showLabels: Boolean) {
            val dataStore = context.dataStore
            dataStore.edit { preferences ->
                preferences[SHOW_LABELS_KEY] = showLabels
            }
        }

        // Reset Settings
        suspend fun resetSettings(context: Context) {
            val dataStore = context.dataStore
            dataStore.edit { preferences ->
                preferences.clear()
            }
        }
    }
}
