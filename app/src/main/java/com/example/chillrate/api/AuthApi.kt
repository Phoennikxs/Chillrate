package com.example.chillrate.api

import com.example.chillrate.model.*
import retrofit2.Response
import retrofit2.http.*

interface AuthApi {

    // Регистрация
    @POST("auth/register")
    suspend fun register(
        @Body request: RegisterRequest
    ): Response<UserResponse>

    // Подтверждение email
    @POST("auth/verify-email")
    suspend fun verifyEmail(
        @Body request: VerifyEmailRequest
    ): Response<Map<String, String>>

    // Повторная отправка кода
    @POST("auth/resend-code")
    suspend fun resendCode(
        @Body request: ResendCodeRequest
    ): Response<Map<String, String>>

    // Логин
    @FormUrlEncoded
    @POST("auth/login")
    suspend fun login(
        @Field("username") email: String,
        @Field("password") password: String
    ): Response<LoginResponse>

    // Получить текущего пользователя (на будущее)
    @GET("auth/me")
    suspend fun me(
        @Header("Authorization") token: String
    ): Response<UserResponse>
}