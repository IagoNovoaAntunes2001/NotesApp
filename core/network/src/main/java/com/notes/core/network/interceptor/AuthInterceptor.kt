package com.notes.core.network.interceptor

import okhttp3.Interceptor
import okhttp3.Response

/**
 * Interceptor de autenticação.
 *
 * Em produção, injetaria um TokenProvider (DataStore / EncryptedSharedPrefs)
 * e leria o token em tempo de execução.
 *
 * Por enquanto o token é vazio — JSONPlaceholder não exige auth.
 * Quando a API real estiver pronta, basta setar o token aqui.
 *
 * Padrão: Authorization: Bearer <token>
 */
class AuthInterceptor(
    private val tokenProvider: () -> String = { "" }
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val token = tokenProvider()

        val request = if (token.isNotBlank()) {
            chain.request().newBuilder()
                .addHeader("Authorization", "Bearer $token")
                .build()
        } else {
            chain.request()
        }

        return chain.proceed(request)
    }
}

