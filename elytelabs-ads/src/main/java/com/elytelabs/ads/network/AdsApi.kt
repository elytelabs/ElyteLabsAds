package com.elytelabs.ads.network

import com.elytelabs.ads.models.AdResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Retrofit interface for the Elyte Labs promotion API.
 *
 * Base URL: `https://elytelabs.com/`
 */
interface AdsApi {

    /**
     * Fetches a shuffled list of promotional app entries.
     *
     * @param limit   maximum number of items to return (max 50).
     * @param type    filter: `"apps"`, `"websites"`, or `"all"`.
     * @param exclude package ID to exclude from results (typically the host app).
     * @return a [Call] wrapping the [AdResponse].
     */
    @GET("api/promote")
    fun getAds(
        @Query("limit") limit: Int,
        @Query("type") type: String,
        @Query("exclude") exclude: String
    ): Call<AdResponse>
}
