package com.example.chillrate.api

import com.example.chillrate.model.*
import retrofit2.Response
import retrofit2.http.*

interface AuthApi {

    // регистрация
    @POST("auth/register")
    suspend fun register(
        @Body request: RegisterRequest
    ): Response<Unit>

    // логин (form-urlencoded)
    @FormUrlEncoded
    @POST("auth/login")
    suspend fun login(
        @Field("username") email: String,
        @Field("password") password: String
    ): Response<LoginResponse>

    // получить себя
    @GET("auth/me")
    suspend fun me(
        @Header("Authorization") token: String
    ): Response<UserResponse>
}
