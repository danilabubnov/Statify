package org.danila.configuration.api.musicbrainz

import com.fasterxml.jackson.databind.ObjectMapper
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.danila.services.api.musicbrainz.client.MusicBrainzAPI
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import retrofit2.Retrofit
import retrofit2.converter.jackson.JacksonConverterFactory
import java.util.concurrent.TimeUnit

@Configuration
class MusicBrainzRetrofitConfig(
    @Qualifier("externalApiObjectMapper") private val objectMapper: ObjectMapper,
    @Value("\${music-brainz.api.base-url}") private val apiBaseUrl: String,
    @Value("\${http.client.timeout.seconds}") private val timeoutSeconds: Int,
    private val musicBrainzMetricsInterceptor: Interceptor,

    @Value("\${music-brainz.user-agent}") private val userAgent: String
) {

    @Bean
    fun musicBrainzOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .connectTimeout(timeoutSeconds.toLong(), TimeUnit.SECONDS)
            .readTimeout(timeoutSeconds.toLong(), TimeUnit.SECONDS)
            .writeTimeout(timeoutSeconds.toLong(), TimeUnit.SECONDS)
            /*
                *   The `User-Agent` header. Mandatory for all requests to MusicBrainz.
                *   Format: "AppName/Version (contact-info)".
             */
            .addInterceptor { chain -> chain.proceed(chain.request().newBuilder().header("User-Agent", userAgent).build()) }
            .addInterceptor(musicBrainzMetricsInterceptor)
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.NONE
            })
            .build()
    }

    @Bean
    fun musicBrainzApi(musicBrainzOkHttpClient: OkHttpClient): MusicBrainzAPI = Retrofit.Builder()
        .baseUrl(apiBaseUrl)
        .client(musicBrainzOkHttpClient)
        .addConverterFactory(JacksonConverterFactory.create(objectMapper))
        .build()
        .create(MusicBrainzAPI::class.java)

}