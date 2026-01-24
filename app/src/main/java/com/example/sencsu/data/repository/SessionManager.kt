package com.example.sencsu.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.example.sencsu.data.remote.dto.AgentDto
import com.google.gson.Gson
import kotlinx.coroutines.flow.*
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.let

@Singleton
class SessionManager @Inject constructor(
    private val dataStore: DataStore<Preferences>,
    private val gson: Gson
) {
    companion object {
        private val AUTH_TOKEN_KEY = stringPreferencesKey("auth_token")
        private val USER_KEY = stringPreferencesKey("user")
        private val AGENT_ID_KEY = longPreferencesKey("id")
    }

    // Événement pour notifier la déconnexion
    private val _logoutEvent = MutableSharedFlow<Unit>(replay = 0)
    val logoutEvent: SharedFlow<Unit> = _logoutEvent.asSharedFlow()

    // Flow pour le token d'authentification
    val tokenFlow: Flow<String?> = dataStore.data
        .map { it[AUTH_TOKEN_KEY] }
        .catch { emit(null) }

    // Flow pour l'utilisateur
    val userFlow: Flow<AgentDto?> = dataStore.data
        .map { preferences ->
            preferences[USER_KEY]?.let { json ->
                try {
                    gson.fromJson(json, AgentDto::class.java)
                } catch (e: Exception) {
                    null
                }
            }
        }
        .catch { emit(null) }

    // Flow pour l'ID de l'agent
    val agentIdFlow: Flow<Long?> = dataStore.data
        .map { it[AGENT_ID_KEY] }
        .catch { emit(null) }

    // Flow combiné pour vérifier si l'utilisateur est authentifié
    val isAuthenticatedFlow: Flow<Boolean> = combine(
        tokenFlow,
        userFlow
    ) { token, user ->
        !token.isNullOrBlank() && user != null
    }

    /**
     * Sauvegarde le token d'authentification
     */
    suspend fun saveAuthToken(token: String) {
        dataStore.edit { preferences ->
            preferences[AUTH_TOKEN_KEY] = token
        }
    }

    /**
     * Sauvegarde l'utilisateur et son ID
     */
    suspend fun saveUser(user: AgentDto) {
        dataStore.edit { preferences ->
            preferences[USER_KEY] = gson.toJson(user)
            user.id?.let { preferences[AGENT_ID_KEY] = it.toLong() }
        }
    }

    /**
     * Sauvegarde l'ID de l'agent séparément
     */
    suspend fun saveAgentId(agentId: Long) {
        dataStore.edit { preferences ->
            preferences[AGENT_ID_KEY] = agentId
        }
    }

    /**
     * Récupère le token de manière suspendue
     */
    suspend fun getToken(): String? = tokenFlow.first()

    /**
     * Récupère l'utilisateur de manière suspendue
     */
    suspend fun getUser(): AgentDto? = userFlow.first()

    /**
     * Récupère l'ID de l'agent de manière suspendue
     */
    suspend fun getAgentId(): Long? = agentIdFlow.first()

    /**
     * Vérifie si l'utilisateur est authentifié
     */
    suspend fun isAuthenticated(): Boolean = isAuthenticatedFlow.first()

    /**
     * Efface toutes les données de session
     */
    suspend fun clear() {
        dataStore.edit { preferences ->
            preferences.clear()
        }
    }

    /**
     * Efface la session et notifie la déconnexion
     */
    suspend fun clearSessionAndNotify() {
        clear()
        _logoutEvent.emit(Unit)
    }

    /**
     * Sauvegarde l'utilisateur complet avec token
     */
    suspend fun saveSession(token: String, user: AgentDto) {
        dataStore.edit { preferences ->
            preferences[AUTH_TOKEN_KEY] = token
            preferences[USER_KEY] = gson.toJson(user)
            user.id?.let { preferences[AGENT_ID_KEY] = it.toLong() }
        }
    }
}