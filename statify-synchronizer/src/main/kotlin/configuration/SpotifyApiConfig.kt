package org.danila.configuration

import com.fasterxml.jackson.databind.ObjectMapper
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.danila.services.api.spotify.SpotifyAPI
import org.danila.services.api.spotify.SpotifyAuthAPI
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import retrofit2.Retrofit
import retrofit2.converter.jackson.JacksonConverterFactory
import java.util.concurrent.TimeUnit

@Configuration
class SpotifyApiConfig(
    @Qualifier("spotifyObjectMapper") private val objectMapper: ObjectMapper,
    @Value("\${spotify.api.base-url}") private val apiBaseUrl: String,
    @Value("\${spotify.auth.base-url}") private val authBaseUrl: String,
    @Value("\${spotify.timeout.seconds}") private val timeoutSeconds: Int
) {

    @Bean
    fun okHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .connectTimeout(timeoutSeconds.toLong(), TimeUnit.SECONDS)
            .readTimeout(timeoutSeconds.toLong(), TimeUnit.SECONDS)
            .writeTimeout(timeoutSeconds.toLong(), TimeUnit.SECONDS)
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BASIC
            })
            .build()
    }

    @Bean
    fun spotifyApi(okHttpClient: OkHttpClient): SpotifyAPI {
        return createRetrofit(
            baseUrl = apiBaseUrl,
            client = okHttpClient,
            objectMapper = objectMapper
        ).create(SpotifyAPI::class.java)
    }

    @Bean
    fun spotifyAuthApi(okHttpClient: OkHttpClient): SpotifyAuthAPI {
        return createRetrofit(
            baseUrl = authBaseUrl,
            client = okHttpClient,
            objectMapper = objectMapper
        ).create(SpotifyAuthAPI::class.java)
    }

    private fun createRetrofit(
        baseUrl: String,
        client: OkHttpClient,
        objectMapper: ObjectMapper
    ): Retrofit {
        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(client)
            .addConverterFactory(JacksonConverterFactory.create(objectMapper))
            .build()
    }

}