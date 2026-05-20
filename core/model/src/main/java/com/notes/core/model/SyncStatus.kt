package com.notes.core.model

/**
 * Estado de sincronização de um tópico com o servidor.
 *
 * Transições possíveis:
 *
 *   insertTopic()          → PENDING   (criado localmente, aguardando envio)
 *   updateTopic() success  → SYNCED    (confirmado pelo servidor)
 *   updateTopic() failure  → snapshot restaurado (mantém status anterior)
 *   sync() server vence    → SYNCED    (dado veio do servidor)
 *   sync() local vence     → PENDING   (local é mais recente, ainda não enviado)
 *   sync() empate (==)     → CONFLICT  (mesmo timestamp, decisão ambígua)
 *
 * Por que isso importa para o usuário?
 *   - SYNCED   → dado está seguro na nuvem ✓
 *   - PENDING  → dado só existe localmente, pode ser perdido se o app for reinstalado
 *   - CONFLICT → sistema detectou ambiguidade — usuário pode querer revisar
 *   - ERROR    → tentativa de sync falhou — dado local mas server discordou
 */
enum class SyncStatus {
    /** Confirmado pelo servidor — dado está consistente local + remoto. */
    SYNCED,

    /** Criado ou editado localmente, aguardando próximo sync para enviar ao servidor. */
    PENDING,

    /**
     * Conflito detectado: local e servidor têm o mesmo timestamp (empate no LWW).
     * Local foi mantido por convenção, mas o dado do servidor foi descartado.
     */
    CONFLICT,

    /**
     * Última tentativa de sync resultou em erro do servidor.
     * O dado está salvo localmente mas pode não estar no servidor.
     */
    ERROR
}

