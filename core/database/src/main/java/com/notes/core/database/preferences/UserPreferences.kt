package com.notes.core.database.preferences

/**
 * Preferências do usuário lidas do DataStore.
 *
 * Comparação das opções de persistência:
 *
 * | Opção                  | Quando usar                                      | Serialização  |
 * |------------------------|--------------------------------------------------|---------------|
 * | Room                   | Dados estruturados (notas, tags, topics)         | SQL           |
 * | DataStore Preferences  | Chave-valor simples (theme, flags, last sync)    | Proto/Prefs   |
 * | SharedPreferences      | Legado — NÃO usar em código novo                 | XML           |
 * | DataStore Proto        | Tipos tipados com schema Protobuf (mais robusto) | Protobuf      |
 *
 * Regra prática:
 * - Tem relações / queries complexas? → Room
 * - Precisa salvar uma configuração simples? → DataStore Preferences
 * - Precisa de schema tipado e evolução garantida? → DataStore Proto
 * - Código legado que precisa manter? → SharedPreferences (só para manter)
 */
data class UserPreferences(
    /** Timestamp (epoch ms) da última sincronização com a API */
    val lastSyncAt: Long = 0L,
    /** Usuário escolheu tema escuro? null = seguir sistema */
    val isDarkTheme: Boolean? = null,
    /** Tamanho de fonte preferido: SMALL, MEDIUM, LARGE */
    val fontSize: FontSize = FontSize.MEDIUM
)

enum class FontSize { SMALL, MEDIUM, LARGE }

