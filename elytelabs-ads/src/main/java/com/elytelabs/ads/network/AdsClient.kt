package com.elytelabs.ads.network

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 * Singleton Retrofit client for the Elyte Labs promotion API.
 *
 * Provides a lazily-initialised [AdsApi] instance backed by Gson
 * deserialization. The base URL points to `https://elytelabs.com/`.
 */
internal object AdsClient {

    private const val BASE_URL = "https://elytelabs.com/"

    private val retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val api: AdsApi by lazy {
        retrofit.create(AdsApi::class.java)
    }
}
