package com.neighborhero.neighborhero.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.neighborhero.neighborhero.feature.auth.CommunityOnboardingScreen
import com.neighborhero.neighborhero.feature.auth.CreateAccountScreen
import com.neighborhero.neighborhero.feature.auth.CreateProfileScreen
import com.neighborhero.neighborhero.feature.auth.LoginScreen
import com.neighborhero.neighborhero.feature.chat.ChatScreen
import com.neighborhero.neighborhero.feature.helpers.HelperLeaderboardScreen
import com.neighborhero.neighborhero.feature.helpers.HelperProfileScreen
import com.neighborhero.neighborhero.feature.helpers.RecommendedHelpersScreen
import com.neighborhero.neighborhero.feature.home.HomeScreen
import com.neighborhero.neighborhero.feature.notifications.NotificationsScreen
import com.neighborhero.neighborhero.feature.profile.EditProfileScreen
import com.neighborhero.neighborhero.feature.profile.ProfileAndSettingsScreen
import com.neighborhero.neighborhero.feature.requests.CreateRequestScreen
import com.neighborhero.neighborhero.feature.requests.HelpRequestDetailsScreen
import com.neighborhero.neighborhero.feature.requests.MyRequestsScreen
import com.neighborhero.neighborhero.feature.requests.NearbyRequestsScreen
import com.neighborhero.neighborhero.feature.safety.SosAndSafetyScreen
import com.neighborhero.neighborhero.feature.settings.SettingsAndPrivacyScreen
import com.neighborhero.neighborhero.feature.task.ActiveTaskScreen
import com.neighborhero.neighborhero.feature.task.ConfirmTaskStartScreen
import com.neighborhero.neighborhero.feature.task.MyAcceptedTasksScreen
import com.neighborhero.neighborhero.feature.task.SubmitCompletionScreen
import com.neighborhero.neighborhero.feature.task.TaskHistoryScreen
import com.neighborhero.neighborhero.feature.task.VerifyTaskCompletionScreen
import com.neighborhero.neighborhero.feature.welcome.WelcomeScreen
import com.neighborhero.neighborhero.viewmodel.ChatViewModel
import com.neighborhero.neighborhero.viewmodel.HelpRequestViewModel
import com.neighborhero.neighborhero.viewmodel.HelperViewModel
import com.neighborhero.neighborhero.viewmodel.HomeViewModel

@Composable
fun AppNavigation(
    navController: NavHostController = rememberNavController(),
    homeViewModel: HomeViewModel = viewModel(),
    requestViewModel: HelpRequestViewModel = viewModel(),
    helperViewModel: HelperViewModel = viewModel(),
    chatViewModel: ChatViewModel = viewModel()
) {
    val currentUser by homeViewModel.currentUser.collectAsState()
    val requests by requestViewModel.requests.collectAsState()
    val helpers by helperViewModel.helpers.collectAsState()
    val chatMessages by chatViewModel.messages.collectAsState()

    NavHost(
        navController = navController,
        startDestination = Routes.WELCOME
    ) {
        // 1. Welcome
        composable(Routes.WELCOME) {
            WelcomeScreen(
                onGetStartedClick = { navController.navigate(Routes.COMMUNITY_ONBOARDING) },
                onLoginClick = { navController.navigate(Routes.LOGIN) }
            )
        }

        // 2. Community Onboarding
        composable(Routes.COMMUNITY_ONBOARDING) {
            CommunityOnboardingScreen(
                onContinueClick = { navController.navigate(Routes.CREATE_ACCOUNT) },
                onSkipClick = { navController.navigate(Routes.HOME) }
            )
        }

        // 3. Login
        composable(Routes.LOGIN) {
            LoginScreen(
                onLoginSuccess = { navController.navigate(Routes.HOME) },
                onCreateAccountClick = { navController.navigate(Routes.CREATE_ACCOUNT) }
            )
        }

        // 4. Create Account
        composable(Routes.CREATE_ACCOUNT) {
            CreateAccountScreen(
                onSignUpSuccess = { navController.navigate(Routes.CREATE_PROFILE) },
                onLoginClick = { navController.navigate(Routes.LOGIN) }
            )
        }

        // 5. Create Profile
        composable(Routes.CREATE_PROFILE) {
            CreateProfileScreen(
                onProfileCompleted = { navController.navigate(Routes.HOME) }
            )
        }

        // 6. Home
        composable(Routes.HOME) {
            HomeScreen(
                requests = requests,
                helpers = helpers,
                onRequestClick = { navController.navigate(Routes.HELP_REQUEST_DETAILS) },
                onHelperClick = { navController.navigate(Routes.HELPER_PROFILE) },
                onCreateRequestClick = { navController.navigate(Routes.CREATE_REQUEST) },
                onSosClick = { navController.navigate(Routes.SOS_AND_SAFETY) },
                onNavigate = { route -> navController.navigate(route) }
            )
        }

        // 7. Nearby Requests
        composable(Routes.NEARBY_REQUESTS) {
            NearbyRequestsScreen(
                requests = requests,
                onRequestClick = { navController.navigate(Routes.HELP_REQUEST_DETAILS) },
                onNavigate = { route -> navController.navigate(route) }
            )
        }

        // 8. Help Request Details
        composable(Routes.HELP_REQUEST_DETAILS) {
            val req = requests.firstOrNull() ?: return@composable
            HelpRequestDetailsScreen(
                request = req,
                onBackClick = { navController.popBackStack() },
                onAcceptClick = {
                    requestViewModel.acceptRequest(req.id, currentUser.fullName)
                    navController.navigate(Routes.CONFIRM_TASK_START)
                },
                onChatClick = { navController.navigate(Routes.CHAT) }
            )
        }

        // 9. Create Request
        composable(Routes.CREATE_REQUEST) {
            CreateRequestScreen(
                onBackClick = { navController.popBackStack() },
                onSubmitRequest = { title, cat, desc, time, loc ->
                    requestViewModel.createRequest(title, cat, desc, time, loc)
                    navController.navigate(Routes.NEARBY_REQUESTS)
                }
            )
        }

        // 10. Recommended Helpers
        composable(Routes.RECOMMENDED_HELPERS) {
            RecommendedHelpersScreen(
                helpers = helpers,
                onHelperClick = { navController.navigate(Routes.HELPER_PROFILE) },
                onNavigate = { route -> navController.navigate(route) }
            )
        }

        // 11. Helper Profile
        composable(Routes.HELPER_PROFILE) {
            val helper = helpers.firstOrNull() ?: return@composable
            HelperProfileScreen(
                helper = helper,
                onBackClick = { navController.popBackStack() },
                onRequestHelperClick = { navController.navigate(Routes.CHAT) },
                onChatClick = { navController.navigate(Routes.CHAT) }
            )
        }

        // 12. Chat
        composable(Routes.CHAT) {
            ChatScreen(
                messages = chatMessages,
                onSendMessage = { text -> chatViewModel.sendMessage(text) },
                onBackClick = { navController.popBackStack() }
            )
        }

        // 13. Active Task
        composable(Routes.ACTIVE_TASK) {
            ActiveTaskScreen(
                onBackClick = { navController.popBackStack() },
                onChatClick = { navController.navigate(Routes.CHAT) },
                onSubmitCompletionClick = { navController.navigate(Routes.SUBMIT_COMPLETION) }
            )
        }

        // 14. Confirm Task Start
        composable(Routes.CONFIRM_TASK_START) {
            ConfirmTaskStartScreen(
                onBackClick = { navController.popBackStack() },
                onStartConfirmed = { navController.navigate(Routes.ACTIVE_TASK) }
            )
        }

        // 15. Submit Completion
        composable(Routes.SUBMIT_COMPLETION) {
            SubmitCompletionScreen(
                onBackClick = { navController.popBackStack() },
                onSubmitSuccess = { navController.navigate(Routes.VERIFY_TASK_COMPLETION) }
            )
        }

        // 16. Verify Task Completion
        composable(Routes.VERIFY_TASK_COMPLETION) {
            VerifyTaskCompletionScreen(
                onBackClick = { navController.popBackStack() },
                onVerified = { navController.navigate(Routes.HOME) }
            )
        }

        // 17. SOS and Safety
        composable(Routes.SOS_AND_SAFETY) {
            SosAndSafetyScreen(
                onNavigate = { route -> navController.navigate(route) },
                onBackClick = { navController.popBackStack() }
            )
        }

        // 18. Task History
        composable(Routes.TASK_HISTORY) {
            TaskHistoryScreen(
                onBackClick = { navController.popBackStack() },
                onNavigate = { route -> navController.navigate(route) }
            )
        }

        // 19. Notifications
        composable(Routes.NOTIFICATIONS) {
            NotificationsScreen(
                notifications = emptyList(),
                onNavigate = { route -> navController.navigate(route) }
            )
        }

        // 20. Profile and Settings
        composable(Routes.PROFILE_AND_SETTINGS) {
            ProfileAndSettingsScreen(
                user = currentUser,
                onEditProfileClick = { navController.navigate(Routes.EDIT_PROFILE) },
                onMyRequestsClick = { navController.navigate(Routes.MY_REQUESTS) },
                onMyAcceptedTasksClick = { navController.navigate(Routes.MY_ACCEPTED_TASKS) },
                onTaskHistoryClick = { navController.navigate(Routes.TASK_HISTORY) },
                onLeaderboardClick = { navController.navigate(Routes.HELPER_LEADERBOARD) },
                onSettingsClick = { navController.navigate(Routes.SETTINGS_AND_PRIVACY) },
                onNavigate = { route -> navController.navigate(route) }
            )
        }

        // 21. My Requests
        composable(Routes.MY_REQUESTS) {
            MyRequestsScreen(
                requests = requests,
                onRequestClick = { navController.navigate(Routes.HELP_REQUEST_DETAILS) },
                onBackClick = { navController.popBackStack() }
            )
        }

        // 22. My Accepted Tasks
        composable(Routes.MY_ACCEPTED_TASKS) {
            MyAcceptedTasksScreen(
                tasks = requests.filter { it.assignedHelperName != null },
                onTaskClick = { navController.navigate(Routes.ACTIVE_TASK) },
                onBackClick = { navController.popBackStack() }
            )
        }

        // 23. Edit Profile
        composable(Routes.EDIT_PROFILE) {
            EditProfileScreen(
                user = currentUser,
                onBackClick = { navController.popBackStack() },
                onSaveSuccess = { _, _, _ ->
                    navController.popBackStack()
                }
            )
        }

        // 24. Helper Leaderboard
        composable(Routes.HELPER_LEADERBOARD) {
            HelperLeaderboardScreen(
                helpers = helpers,
                onBackClick = { navController.popBackStack() }
            )
        }

        // 25. Settings and Privacy
        composable(Routes.SETTINGS_AND_PRIVACY) {
            SettingsAndPrivacyScreen(
                onBackClick = { navController.popBackStack() }
            )
        }
    }
}
