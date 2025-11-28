package com.fabio.cashcontrol.data

import android.content.Context
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// Nome do DataStore
private val Context.dataStore by preferencesDataStore(name = "settings")

object SettingsStore {

    // Chave
    private val LIMIT_KEY = doublePreferencesKey("monthly_limit")

    // Lê o limite salvo
    fun getMonthlyLimit(context: Context): Flow<Double?> {
        return context.dataStore.data.map { prefs ->
            prefs[LIMIT_KEY]
        }
    }

    // Salva ou remove
    suspend fun setMonthlyLimit(context: Context, value: Double?) {
        context.dataStore.edit { prefs ->
            if (value == null) prefs.remove(LIMIT_KEY)
            else prefs[LIMIT_KEY] = value
        }
    }
}
