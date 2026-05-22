package com.notes.core.network.di

import com.notes.core.network.api.TopicsApi
import com.notes.core.network.datasource.TopicRemoteDataSource
import com.notes.core.network.datasource.TopicRemoteDataSourceImpl
import com.notes.core.network.interceptor.AuthInterceptor
import com.notes.core.network.interceptor.RetryInterceptor
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    /**
     * BASE_URL aponta para a API pública de testes.
     * Quando tiver servidor real, trocar aqui — só 1 lugar!
     */
    private const val BASE_URL = "https://jsonplaceholder.typicode.com/"

    // Timeouts: define quanto tempo esperar antes de desistir
    private const val CONNECT_TIMEOUT_S = 10L  // tempo para estabelecer conexão TCP
    private const val READ_TIMEOUT_S    = 30L  // tempo para receber o corpo da resposta
    private const val WRITE_TIMEOUT_S   = 30L  // tempo para enviar o corpo da requisição

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient =
        OkHttpClient.Builder()
            // Timeouts — evita requests pendurados para sempre
            .connectTimeout(CONNECT_TIMEOUT_S, TimeUnit.SECONDS)
            .readTimeout(READ_TIMEOUT_S, TimeUnit.SECONDS)
            .writeTimeout(WRITE_TIMEOUT_S, TimeUnit.SECONDS)
            // Auth — adiciona Bearer token se disponível
            .addInterceptor(AuthInterceptor())
            // Retry — tenta novamente em falhas de rede ou 5xx (até 3x)
            .addInterceptor(RetryInterceptor(maxRetries = 3))
            // Logging — só em debug, nunca em release
            .addInterceptor(
                HttpLoggingInterceptor().apply {
                    level = HttpLoggingInterceptor.Level.BODY
                }
            )
            .build()

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit =
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

    @Provides
    @Singleton
    fun provideTopicsApi(retrofit: Retrofit): TopicsApi =
        retrofit.create(TopicsApi::class.java)

    @Provides
    @Singleton
    fun provideTopicRemoteDataSource(api: TopicsApi): TopicRemoteDataSource =
        TopicRemoteDataSourceImpl(api)
}
