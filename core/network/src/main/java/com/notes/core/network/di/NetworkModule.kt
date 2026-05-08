package com.notes.core.network.di

import com.notes.core.network.api.TopicsApi
import com.notes.core.network.datasource.TopicRemoteDataSource
import com.notes.core.network.datasource.TopicRemoteDataSourceImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    /**
     * BASE_URL aponta para o emulador Android (10.0.2.2 = localhost do host).
     * Quando tiver servidor real, trocar aqui — só 1 lugar!
     *
     * Por enquanto usamos FakeTopicRemoteDataSource então
     * o Retrofit é criado mas não é chamado.
     */
    private const val BASE_URL = "https://jsonplaceholder.typicode.com/"

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient =
        OkHttpClient.Builder()
            .addInterceptor(
                HttpLoggingInterceptor().apply {
                    // Loga request e response completos em debug
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
        TopicRemoteDataSourceImpl(api)  // ← implementação real ativa!
}
