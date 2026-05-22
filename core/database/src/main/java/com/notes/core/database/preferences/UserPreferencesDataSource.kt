package com.notes.core.database.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * DataSource que gerencia preferências do usuário via DataStore.
 *
 * DataStore vs SharedPreferences:
 * - DataStore é assíncrono (Flows + coroutines) — nunca bloqueia a Main thread
 * - SharedPreferences tem APIs síncronas que causam ANR se usadas na Main thread
 * - DataStore é type-safe e lida com erros via exceções, não silenciosamente
 * - DataStore suporta transações atômicas (edit { } é atomic)
 *
 * Por que não Room para isso?
 * - Room é ótimo para dados estruturados com relações e queries.
 * - Para um par chave-valor como "última vez que sincronizou" ou "tema escolhido",
 *   criar uma tabela SQL é overkill — DataStore é mais simples e direto.
 */
@Singleton
class UserPreferencesDataSource @Inject constructor(
    @ApplicationContext private val context: Context
) {

    // Cria (ou reutiliza) o DataStore com nome "user_preferences"
    // O arquivo fica em: data/data/<package>/files/datastore/user_preferences.preferences_pb
    private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(
        name = "user_preferences"
    )

    // ── Chaves ──────────────────────────────────────────────────────────────
    private object Keys {
        val LAST_SYNC_AT   = longPreferencesKey("last_sync_at")
        val IS_DARK_THEME  = booleanPreferencesKey("is_dark_theme")
        val FONT_SIZE      = intPreferencesKey("font_size") // ordinal do enum
    }

    // ── Leitura (Flow — reativo, emite sempre que uma chave muda) ────────────

    /**
     * Flow que emite [UserPreferences] sempre que qualquer preferência mudar.
     *
     * A UI coleta este Flow com collectAsStateWithLifecycle — exatamente como
     * coleta o StateFlow do ViewModel. O DataStore é reativo por design.
     */
    val userPreferences: Flow<UserPreferences> = context.dataStore.data.map { prefs ->
        val darkThemeValue = prefs[Keys.IS_DARK_THEME] // null se nunca foi salvo
        UserPreferences(
            lastSyncAt  = prefs[Keys.LAST_SYNC_AT] ?: 0L,
            isDarkTheme = darkThemeValue,
            fontSize    = FontSize.entries.getOrElse(prefs[Keys.FONT_SIZE] ?: 1) { FontSize.MEDIUM }
        )
    }

    // ── Escrita (suspend — executa em IO, nunca bloqueia Main) ──────────────

    /**
     * Atualiza o timestamp da última sync.
     * Chamado pelo SyncWorker após um sync bem-sucedido.
     */
    suspend fun updateLastSyncAt(timestampMs: Long) {
        context.dataStore.edit { prefs ->
            prefs[Keys.LAST_SYNC_AT] = timestampMs
        }
    }

    suspend fun setDarkTheme(enabled: Boolean?) {
        context.dataStore.edit { prefs ->
            if (enabled == null) {
                prefs.remove(Keys.IS_DARK_THEME) // volta para "seguir sistema"
            } else {
                prefs[Keys.IS_DARK_THEME] = enabled
            }
        }
    }

    suspend fun setFontSize(size: FontSize) {
        context.dataStore.edit { prefs ->
            prefs[Keys.FONT_SIZE] = size.ordinal
        }
    }
}

