package com.notes.core.network.interceptor

import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException

/**
 * Interceptor de retry automático.
 *
 * Comportamento:
 * - Tenta novamente em falhas de rede (IOException) — ex: sem internet momentânea
 * - Tenta novamente em respostas 5xx (erro do servidor)
 * - NÃO tenta novamente em 4xx (erro do cliente — ex: 401, 404)
 * - Máximo de [maxRetries] tentativas (além da tentativa original)
 *
 * Nota: para retry com backoff exponencial no WorkManager, usamos BackoffPolicy.EXPONENTIAL.
 * Este interceptor é para falhas transitórias de rede dentro de uma única requisição.
 */
class RetryInterceptor(
    private val maxRetries: Int = 3
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        var response: Response? = null
        var attempt = 0

        while (attempt <= maxRetries) {
            try {
                response?.close() // fecha response anterior antes de tentar de novo
                response = chain.proceed(request)

                // 5xx = erro do servidor → tenta de novo
                if (response.isSuccessful || response.code < 500) {
                    return response
                }
            } catch (e: IOException) {
                // falha de rede → tenta de novo
                if (attempt == maxRetries) throw e
            }

            attempt++
        }

        // Se chegou aqui, maxRetries foi atingido
        return response ?: throw IOException("Request failed after $maxRetries retries")
    }
}

