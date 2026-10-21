package com.example.data.api

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.*
import java.util.concurrent.TimeUnit

interface WatchNextApiService {

    @GET("api/status")
    suspend fun getStatus(): ApiQueueStatus

    @POST("api/queue/toggle-pause")
    suspend fun togglePause(): ApiTogglePauseResponse

    @POST("api/queue/pause")
    suspend fun pauseQueue(): Map<String, Any>

    @POST("api/queue/resume")
    suspend fun resumeQueue(): Map<String, Any>

    @POST("api/limits")
    suspend fun updateLimits(@Body request: ApiUpdateLimitsRequest): Map<String, Any>

    @GET("api/packs")
    suspend fun listPacks(): List<ApiPackItem>

    @GET("api/packs/{pack_hash}")
    suspend fun getPack(@Path("pack_hash") hash: String): ApiPackItem

    @POST("api/packs/add")
    suspend fun addPack(@Body request: ApiAddTorrentRequest): Map<String, Any>

    @POST("api/packs/{pack_hash}/from")
    suspend fun setPackStart(
        @Path("pack_hash") hash: String,
        @Body request: ApiSetStartRequest
    ): Map<String, Any>

    @POST("api/packs/{pack_hash}/finish")
    suspend fun finishPack(@Path("pack_hash") hash: String): Map<String, Any>

    @POST("api/packs/{pack_hash}/retry")
    suspend fun retryPack(@Path("pack_hash") hash: String): Map<String, Any>

    @DELETE("api/packs/{pack_hash}")
    suspend fun deletePack(@Path("pack_hash") hash: String): Map<String, Any>

    @DELETE("api/packs/{pack_hash}/episodes/{episode_index}")
    suspend fun deleteEpisode(
        @Path("pack_hash") hash: String,
        @Path("episode_index") episodeIndex: Int
    ): Map<String, Any>

    @GET("api/search")
    suspend fun searchTorrents(
        @Query("query") query: String,
        @Query("kind") kind: String = "series",
        @Query("minSeeders") minSeeders: Int = 0,
        @Query("voiceFilter") voiceFilter: String? = null,
        @Query("qualityFilter") qualityFilter: String? = null
    ): List<ApiTorrentSearchResult>

    @GET("api/subscriptions")
    suspend fun listSubscriptions(): List<ApiSubscriptionItem>

    @POST("api/subscriptions")
    suspend fun addSubscription(@Body request: ApiAddSubscriptionRequest): Map<String, Any>

    @DELETE("api/subscriptions/{sub_id}")
    suspend fun removeSubscription(@Path("sub_id") id: String): Map<String, Any>

    @POST("api/subscriptions/{sub_id}/import")
    suspend fun importSubscriptionRelease(@Path("sub_id") id: String): Map<String, Any>

    companion object {
        fun create(baseUrl: String, apiToken: String = ""): WatchNextApiService {
            val normalizedUrl = if (baseUrl.endsWith("/")) baseUrl else "$baseUrl/"

            val authInterceptor = Interceptor { chain ->
                val original = chain.request()
                val requestBuilder = original.newBuilder()
                if (apiToken.isNotBlank()) {
                    requestBuilder.header("Authorization", "Bearer $apiToken")
                }
                chain.proceed(requestBuilder.build())
            }

            val okHttpClient = OkHttpClient.Builder()
                .addInterceptor(authInterceptor)
                .connectTimeout(5, TimeUnit.SECONDS)
                .readTimeout(15, TimeUnit.SECONDS)
                .writeTimeout(15, TimeUnit.SECONDS)
                .build()

            val moshi = Moshi.Builder()
                .addLast(KotlinJsonAdapterFactory())
                .build()

            return Retrofit.Builder()
                .baseUrl(normalizedUrl)
                .client(okHttpClient)
                .addConverterFactory(MoshiConverterFactory.create(moshi))
                .build()
                .create(WatchNextApiService::class.java)
        }
    }
}
