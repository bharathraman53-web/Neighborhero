package com.neighborhero.neighborhero.data.network

import com.neighborhero.neighborhero.data.network.dto.*
import retrofit2.Response
import retrofit2.http.*

interface NeighborHeroApiService {

    // --- Health ---
    @GET("health")
    suspend fun checkHealth(): Response<Map<String, Any>>

    // --- Authentication ---
    @POST("auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<ApiResponse<AuthResponseData>>

    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): Response<ApiResponse<AuthResponseData>>

    @POST("auth/logout")
    suspend fun logout(): Response<ApiResponse<Unit>>

    @GET("auth/me")
    suspend fun getCurrentUser(): Response<ApiResponse<UserDto>>

    @PUT("auth/profile")
    suspend fun updateProfile(@Body request: UpdateProfileRequest): Response<ApiResponse<UserDto>>

    // --- Help Requests ---
    @GET("requests")
    suspend fun getRequests(
        @Query("category") category: String? = null,
        @Query("status") status: String? = null,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 50
    ): Response<ApiResponse<List<HelpRequestDto>>>

    @GET("requests/{id}")
    suspend fun getRequestById(@Path("id") id: String): Response<ApiResponse<HelpRequestDto>>

    @POST("requests")
    suspend fun createRequest(@Body request: CreateHelpRequestDto): Response<ApiResponse<HelpRequestDto>>

    @POST("requests/{id}/cancel")
    suspend fun cancelRequest(@Path("id") id: String): Response<ApiResponse<HelpRequestDto>>

    @GET("requests/my/posted")
    suspend fun getMyPostedRequests(): Response<ApiResponse<List<HelpRequestDto>>>

    @GET("requests/my/accepted")
    suspend fun getMyAcceptedRequests(): Response<ApiResponse<List<HelpRequestDto>>>

    // --- Helpers & Leaderboard ---
    @GET("helpers")
    suspend fun getHelpers(
        @Query("skill") skill: String? = null,
        @Query("limit") limit: Int = 20
    ): Response<ApiResponse<List<HelperProfileDto>>>

    @GET("helpers/{id}")
    suspend fun getHelperById(@Path("id") id: String): Response<ApiResponse<HelperProfileDto>>

    @GET("helpers/leaderboard")
    suspend fun getLeaderboard(@Query("limit") limit: Int = 50): Response<ApiResponse<List<HelperProfileDto>>>

    // --- Task Lifecycle ---
    @POST("tasks/{id}/assign")
    suspend fun assignHelper(
        @Path("id") id: String,
        @Body body: AssignHelperDto = AssignHelperDto()
    ): Response<ApiResponse<HelpRequestDto>>

    @POST("tasks/{id}/confirm-start")
    suspend fun confirmTaskStart(@Path("id") id: String): Response<ApiResponse<HelpRequestDto>>

    @POST("tasks/{id}/submit-completion")
    suspend fun submitCompletion(
        @Path("id") id: String,
        @Body body: SubmitCompletionDto
    ): Response<ApiResponse<HelpRequestDto>>

    @POST("tasks/{id}/verify-completion")
    suspend fun verifyCompletion(
        @Path("id") id: String,
        @Body body: VerifyCompletionDto
    ): Response<ApiResponse<HelpRequestDto>>

    // --- Chat ---
    @GET("chat/messages")
    suspend fun getChatMessages(
        @Query("conversation_id") conversationId: String? = null,
        @Query("since_id") sinceId: String? = null,
        @Query("limit") limit: Int = 50
    ): Response<ApiResponse<List<ChatMessageDto>>>

    @POST("chat/messages")
    suspend fun sendMessage(@Body message: SendMessageDto): Response<ApiResponse<ChatMessageDto>>

    // --- Notifications ---
    @GET("notifications")
    suspend fun getNotifications(
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 30
    ): Response<ApiResponse<List<NotificationDto>>>

    @POST("notifications/{id}/read")
    suspend fun markNotificationAsRead(@Path("id") id: String): Response<ApiResponse<Map<String, Any>>>

    @POST("notifications/read-all")
    suspend fun markAllNotificationsAsRead(): Response<ApiResponse<Unit>>

    // --- Safety & History ---
    @POST("safety/sos")
    suspend fun triggerSos(@Body body: TriggerSosDto): Response<ApiResponse<SosAlertDto>>

    @POST("safety/sos/{id}/resolve")
    suspend fun resolveSos(@Path("id") id: String): Response<ApiResponse<Map<String, Any>>>

    @GET("safety/task-history")
    suspend fun getTaskHistory(): Response<ApiResponse<List<TaskHistoryDto>>>
}

