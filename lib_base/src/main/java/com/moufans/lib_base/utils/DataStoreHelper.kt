package com.moufans.lib_base.utils

import android.content.Context
import android.text.TextUtils
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.google.gson.Gson
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlin.reflect.KClass

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "app_preferences")

open class DataStoreHelper {

    private constructor()

    private val dataStore: DataStore<Preferences> by lazy {
        InitUtils.getApplication().dataStore
    }

    suspend fun getString(key: String, defaultValue: String? = null): String? {
        return dataStore.data.map { preferences ->
            preferences[stringPreferencesKey(key)] ?: defaultValue
        }.first()
    }

    suspend fun getInt(key: String, defaultValue: Int? = null): Int? {
        return dataStore.data.map { preferences ->
            preferences[intPreferencesKey(key)] ?: defaultValue
        }.first()
    }

    suspend fun getDouble(key: String, defaultValue: Double? = null): Double? {
        return dataStore.data.map { preferences ->
            preferences[doublePreferencesKey(key)] ?: defaultValue
        }.first()
    }

    suspend fun getBoolean(key: String, defaultValue: Boolean? = null): Boolean? {
        return dataStore.data.map { preferences ->
            preferences[booleanPreferencesKey(key)] ?: defaultValue
        }.first()
    }

    suspend fun getFloat(key: String, defaultValue: Float? = null): Float? {
        return dataStore.data.map { preferences ->
            preferences[floatPreferencesKey(key)] ?: defaultValue
        }.first()
    }

    suspend fun getLong(key: String, defaultValue: Long? = null): Long? {
        return dataStore.data.map { preferences ->
            preferences[longPreferencesKey(key)] ?: defaultValue
        }.first()
    }

    suspend fun getStringSet(key: String, defaultValue: Set<String>? = null): Set<String>? {
        return dataStore.data.map { preferences ->
            preferences[stringSetPreferencesKey(key)] ?: defaultValue
        }.first()
    }

    suspend fun <T> getObject(key: String, clazz: Class<T>, defaultValue: T? = null): T? {
        return dataStore.data.map { preferences ->
            val a = preferences[stringPreferencesKey(key)]
            if (a != null) {
                Gson().fromJson(a, clazz)
            } else {
                defaultValue
            }
        }.first()
    }


    suspend fun <T : Any> get(key: String, clazz: KClass<T>, defaultValue: T? = null): T? {
        if (TextUtils.isEmpty(key)) {
            return defaultValue
        }
        val pKey = when (clazz) {
            String::class -> {
                stringPreferencesKey(key)
            }
            Int::class -> {
                intPreferencesKey(key)
            }
            Double::class -> {
                doublePreferencesKey(key)
            }
            Boolean::class -> {
                booleanPreferencesKey(key)
            }
            Float::class -> {
                floatPreferencesKey(key)
            }
            Long::class -> {
                longPreferencesKey(key)
            }
            Set::class -> {
                stringSetPreferencesKey(key)
            }
            else -> {
                null
            }
        }
        return dataStore.data.map { preferences ->
            if (pKey != null) {
                (preferences[pKey] as? T) ?: defaultValue
            } else {
                val a = preferences[stringPreferencesKey(key)]
                if (a != null) {
                    Gson().fromJson(a, clazz::class.java) as T
                } else {
                    defaultValue
                }
            }
        }?.first()
    }

    suspend fun put(key: String, value: Any) {
        if (TextUtils.isEmpty(key)) return
        when (value::class) {
            String::class -> {
                dataStore?.edit { settings ->
                    settings[stringPreferencesKey(key)] = value as String
                }
            }
            Int::class -> {
                dataStore?.edit { settings ->
                    settings[intPreferencesKey(key)] = value as Int
                }
            }
            Double::class -> {
                dataStore?.edit { settings ->
                    settings[doublePreferencesKey(key)] = value as Double
                }
            }
            Boolean::class -> {
                dataStore?.edit { settings ->
                    settings[booleanPreferencesKey(key)] = value as Boolean
                }
            }
            Float::class -> {
                dataStore?.edit { settings ->
                    settings[floatPreferencesKey(key)] = value as Float
                }
            }
            Long::class -> {
                dataStore?.edit { settings ->
                    settings[longPreferencesKey(key)] = value as Long
                }
            }
            Set::class -> {
                dataStore?.edit { settings ->
                    settings[stringSetPreferencesKey(key)] = value as Set<String>
                }
            }
            else -> {
                dataStore?.edit { settings ->
                    settings[stringPreferencesKey(key)] = Gson().toJson(value)
                }
            }
        }
    }

    companion object {
        val instance by lazy(LazyThreadSafetyMode.SYNCHRONIZED) {
            DataStoreHelper()
        }
    }
}