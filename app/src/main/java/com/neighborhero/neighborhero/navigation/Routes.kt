package com.neighborhero.neighborhero.navigation

import java.net.URLEncoder

object Routes {
    const val WELCOME = "welcome"
    const val COMMUNITY_ONBOARDING = "community_onboarding"
    const val LOGIN = "login"
    const val CREATE_ACCOUNT = "create_account"
    const val CREATE_PROFILE = "create_profile"
    const val HOME = "home"
    const val NEARBY_REQUESTS = "nearby_requests"
    const val HELP_REQUEST_DETAILS = "help_request_details"
    const val HELP_REQUEST_DETAILS_PARAM = "help_request_details/{requestId}"
    const val CREATE_REQUEST = "create_request"
    const val RECOMMENDED_HELPERS = "recommended_helpers"
    const val HELPER_PROFILE = "helper_profile"
    const val HELPER_PROFILE_PARAM = "helper_profile/{helperId}"
    const val CHAT = "chat"
    const val CHAT_PARAM = "chat?conversationId={conversationId}&recipientId={recipientId}&recipientName={recipientName}"
    const val ACTIVE_TASK = "active_task"
    const val ACTIVE_TASK_PARAM = "active_task/{taskId}"
    const val CONFIRM_TASK_START = "confirm_task_start"
    const val CONFIRM_TASK_START_PARAM = "confirm_task_start/{taskId}"
    const val SUBMIT_COMPLETION = "submit_completion"
    const val SUBMIT_COMPLETION_PARAM = "submit_completion/{taskId}"
    const val VERIFY_TASK_COMPLETION = "verify_task_completion"
    const val VERIFY_TASK_COMPLETION_PARAM = "verify_task_completion/{taskId}"
    const val SOS_AND_SAFETY = "sos_and_safety"
    const val TASK_HISTORY = "task_history"
    const val NOTIFICATIONS = "notifications"
    const val PROFILE_AND_SETTINGS = "profile_and_settings"
    const val MY_REQUESTS = "my_requests"
    const val MY_ACCEPTED_TASKS = "my_accepted_tasks"
    const val EDIT_PROFILE = "edit_profile"
    const val HELPER_LEADERBOARD = "helper_leaderboard"
    const val SETTINGS_AND_PRIVACY = "settings_and_privacy"

    fun helpRequestDetails(requestId: String) = "help_request_details/$requestId"
    fun helperProfile(helperId: String) = "helper_profile/$helperId"
    fun confirmTaskStart(taskId: String) = "confirm_task_start/$taskId"
    fun activeTask(taskId: String) = "active_task/$taskId"
    fun submitCompletion(taskId: String) = "submit_completion/$taskId"
    fun verifyTaskCompletion(taskId: String) = "verify_task_completion/$taskId"

    fun chat(
        conversationId: String? = null,
        recipientId: String? = null,
        recipientName: String? = null
    ): String {
        val params = mutableListOf<String>()
        if (!conversationId.isNullOrBlank()) params.add("conversationId=$conversationId")
        if (!recipientId.isNullOrBlank()) params.add("recipientId=$recipientId")
        if (!recipientName.isNullOrBlank()) {
            val encoded = try { URLEncoder.encode(recipientName, "UTF-8") } catch (_: Exception) { recipientName }
            params.add("recipientName=$encoded")
        }
        return if (params.isEmpty()) CHAT else "$CHAT?${params.joinToString("&")}"
    }
}
