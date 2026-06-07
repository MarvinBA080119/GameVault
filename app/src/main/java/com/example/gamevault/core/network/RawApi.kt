package com.example.gamevault.core.network

import com.example.gamevault.core.model.GameDetail
import com.example.gamevault.core.model.GamesListResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface RawgApi {

    @GET("games")
    suspend fun listGames(
        @Query("key") key: String,
        @Query("page") page: Int? = null,
        @Query("page_size") pageSize: Int? = null,
        @Query("search") search: String? = null,
        @Query("ordering") ordering: String? = null
    ): Response<GamesListResponse>

    @GET("games/{id}")
    suspend fun getGame(
        @Path("id") id: Int,
        @Query("key") key: String
    ): Response<GameDetail>
}