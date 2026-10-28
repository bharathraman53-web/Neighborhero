package com.neighborhero.neighborhero.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.neighborhero.neighborhero.data.model.RequestStatus
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
import com.neighborhero.neighborhero.viewmodel.AuthViewModel
import com.neighborhero.neighborhero.viewmodel.ChatViewModel
import com.neighborhero.neighborhero.viewmodel.HelpRequestViewModel
import com.neighborhero.neighborhero.viewmodel.HelperViewModel
import com.neighborhero.neighborhero.viewmodel.HomeViewModel
import com.neighborhero.neighborhero.viewmodel.TaskViewModel

private fun navigateTopLevel(navController: NavHostController, route: String) {
    navController.navigate(route) {
        popUpTo(navController.graph.findStartDestination().id) {
            saveState = true
        }
        launchSingleTop = true
        restoreState = true
    }
}

@Composable
fun AppNavigation(
    navController: NavHostController = rememberNavController(),
    homeViewModel: HomeViewModel = viewModel(),
    requestViewModel: HelpRequestViewModel = viewModel(),
    helperViewModel: HelperViewModel = viewModel(),
    chatViewModel: ChatViewModel = viewModel(),
    taskViewModel: TaskViewModel = viewModel(),
    authViewModel: AuthViewModel = viewModel()
) {
    val currentUser by homeViewModel.currentUser.collectAsState()
    val requests by requestViewModel.requests.collectAsState()
    val helpers by helperViewModel.helpers.collectAsState()
    val chatMessages by chatViewModel.messages.collectAsState()
    val notifications by homeViewModel.notifications.collectAsState()

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
            val isLoggedIn by authViewModel.isLoggedIn.collectAsState()
            val authError by authViewModel.authError.collectAsState()
            LaunchedEffect(isLoggedIn) {
                if (isLoggedIn) {
                    homeViewModel.refreshDashboard()
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.WELCOME) { inclusive = true }
                    }
                }
            }
            LoginScreen(
                onLoginSuccess = {
                    homeViewModel.refreshDashboard()
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.WELCOME) { inclusive = true }
                    }
                },
                onLoginSubmit = { email, pass ->
                    authViewModel.login(email, pass)
                },
                errorMessage = authError,
                onCreateAccountClick = {
                    authViewModel.clearError()
                    navController.navigate(Routes.CREATE_ACCOUNT)
                }
            )
        }

        // 4. Create Account
        composable(Routes.CREATE_ACCOUNT) {
            val isLoggedIn by authViewModel.isLoggedIn.collectAsState()
            val authError by authViewModel.authError.collectAsState()
            LaunchedEffect(isLoggedIn) {
                if (isLoggedIn) {
                    homeViewModel.refreshDashboard()
                    navController.navigate(Routes.CREATE_PROFILE)
                }
            }
            CreateAccountScreen(
                onSignUpSuccess = { navController.navigate(Routes.CREATE_PROFILE) },
                onSignUpSubmit = { fullName, email, phone, pass ->
                    authViewModel.register(
                        fullName = fullName,
                        email = email,
                        pass = pass,
                        phone = phone.ifBlank { null },
                        onSuccess = {
                            homeViewModel.refreshDashboard()
                            navController.navigate(Routes.CREATE_PROFILE)
                        }
                    )
                },
                errorMessage = authError,
                onLoginClick = {
                    authViewModel.clearError()
                    navController.navigate(Routes.LOGIN)
                }
            )
        }

        // 5. Create Profile
        composable(Routes.CREATE_PROFILE) {
            CreateProfileScreen(
                initialName = currentUser.fullName,
                onProfileCompleted = { name, neighborhood, bio ->
                    homeViewModel.updateUserProfile(name, neighborhood, bio)
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.WELCOME) { inclusive = true }
                    }
                }
            )
        }

        // 6. Home
        composable(Routes.HOME) {
            HomeScreen(
                requests = requests,
                helpers = helpers,
                onRequestClick = { req -> navController.navigate(Routes.helpRequestDetails(req.id)) },
                onHelperClick = { helper -> navController.navigate(Routes.helperProfile(helper.id)) },
                onCreateRequestClick = { navController.navigate(Routes.CREATE_REQUEST) },
                onSosClick = { navController.navigate(Routes.SOS_AND_SAFETY) },
                onActiveTaskClick = { activeTask ->
                    val taskId = activeTask?.id ?: requests.firstOrNull { it.status == RequestStatus.IN_PROGRESS }?.id
                    if (taskId != null) {
                        navController.navigate(Routes.activeTask(taskId))
                    } else {
                        navController.navigate(Routes.ACTIVE_TASK)
                    }
                },
                onNavigate = { route -> navigateTopLevel(navController, route) }
            )
        }

        // 7. Nearby Requests
        composable(Routes.NEARBY_REQUESTS) {
            NearbyRequestsScreen(
                requests = requests,
                onRequestClick = { req -> navController.navigate(Routes.helpRequestDetails(req.id)) },
                onNavigate = { route -> navigateTopLevel(navController, route) }
            )
        }

        // 8. Help Request Details (with or without requestId param)
        composable(
            route = Routes.HELP_REQUEST_DETAILS_PARAM,
            arguments = listOf(navArgument("requestId") { type = NavType.StringType })
        ) { backStackEntry ->
            val reqId = backStackEntry.arguments?.getString("requestId")
            val req = requestViewModel.getRequest(reqId) ?: requests.firstOrNull()
            if (req != null) {
                HelpRequestDetailsScreen(
                    request = req,
                    onBackClick = { navController.popBackStack() },
                    onAcceptClick = {
                        requestViewModel.acceptRequest(req.id, currentUser.fullName)
                        navController.navigate(Routes.confirmTaskStart(req.id))
                    },
                    onChatClick = { navController.navigate(Routes.CHAT) }
                )
            }
        }

        composable(Routes.HELP_REQUEST_DETAILS) {
            val req = requests.firstOrNull()
            if (req != null) {
                HelpRequestDetailsScreen(
                    request = req,
                    onBackClick = { navController.popBackStack() },
                    onAcceptClick = {
                        requestViewModel.acceptRequest(req.id, currentUser.fullName)
                        navController.navigate(Routes.confirmTaskStart(req.id))
                    },
                    onChatClick = { navController.navigate(Routes.CHAT) }
                )
            }
        }

        // 9. Create Request
        composable(Routes.CREATE_REQUEST) {
            CreateRequestScreen(
                onBackClick = { navController.popBackStack() },
                onSubmitRequest = { title, cat, desc, time, loc ->
                    requestViewModel.createRequest(title, cat, desc, time, loc)
                    navController.navigate(Routes.NEARBY_REQUESTS) {
                        popUpTo(Routes.HOME)
                    }
                }
            )
        }

        // 10. Recommended Helpers
        composable(Routes.RECOMMENDED_HELPERS) {
            RecommendedHelpersScreen(
                helpers = helpers,
                onHelperClick = { helper -> navController.navigate(Routes.helperProfile(helper.id)) },
                onNavigate = { route -> navigateTopLevel(navController, route) }
            )
        }

        // 11. Helper Profile (with or without helperId param)
        composable(
            route = Routes.HELPER_PROFILE_PARAM,
            arguments = listOf(navArgument("helperId") { type = NavType.StringType })
        ) { backStackEntry ->
            val helperId = backStackEntry.arguments?.getString("helperId")
            val helper = helperViewModel.getHelper(helperId) ?: helpers.firstOrNull()
            if (helper != null) {
                HelperProfileScreen(
                    helper = helper,
                    onBackClick = { navController.popBackStack() },
                    onRequestHelperClick = { navController.navigate(Routes.CHAT) },
                    onChatClick = { navController.navigate(Routes.CHAT) }
                )
            }
        }

        composable(Routes.HELPER_PROFILE) {
            val helper = helpers.firstOrNull()
            if (helper != null) {
                HelperProfileScreen(
                    helper = helper,
                    onBackClick = { navController.popBackStack() },
                    onRequestHelperClick = { navController.navigate(Routes.CHAT) },
                    onChatClick = { navController.navigate(Routes.CHAT) }
                )
            }
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
        composable(
            route = Routes.ACTIVE_TASK_PARAM,
            arguments = listOf(navArgument("taskId") { type = NavType.StringType })
        ) { backStackEntry ->
            val taskId = backStackEntry.arguments?.getString("taskId")
            val task = requestViewModel.getRequest(taskId) ?: requests.firstOrNull { it.status == RequestStatus.IN_PROGRESS }
            ActiveTaskScreen(
                task = task,
                onBackClick = { navController.popBackStack() },
                onChatClick = { navController.navigate(Routes.CHAT) },
                onSubmitCompletionClick = {
                    navController.navigate(Routes.submitCompletion(task?.id ?: taskId ?: ""))
                }
            )
        }

        composable(Routes.ACTIVE_TASK) {
            val task = requests.firstOrNull { it.status == RequestStatus.IN_PROGRESS } ?: requests.firstOrNull()
            ActiveTaskScreen(
                task = task,
                onBackClick = { navController.popBackStack() },
                onChatClick = { navController.navigate(Routes.CHAT) },
                onSubmitCompletionClick = {
                    navController.navigate(Routes.submitCompletion(task?.id ?: ""))
                }
            )
        }

        // 14. Confirm Task Start
        composable(
            route = Routes.CONFIRM_TASK_START_PARAM,
            arguments = listOf(navArgument("taskId") { type = NavType.StringType })
        ) { backStackEntry ->
            val taskId = backStackEntry.arguments?.getString("taskId")
            val task = requestViewModel.getRequest(taskId)
            ConfirmTaskStartScreen(
                task = task,
                onBackClick = { navController.popBackStack() },
                onStartConfirmed = {
                    taskViewModel.confirmStart(taskId)
                    navController.navigate(Routes.activeTask(taskId ?: ""))
                }
            )
        }

        composable(Routes.CONFIRM_TASK_START) {
            val task = requests.firstOrNull()
            ConfirmTaskStartScreen(
                task = task,
                onBackClick = { navController.popBackStack() },
                onStartConfirmed = {
                    taskViewModel.confirmStart(task?.id)
                    navController.navigate(Routes.activeTask(task?.id ?: ""))
                }
            )
        }

        // 15. Submit Completion
        composable(
            route = Routes.SUBMIT_COMPLETION_PARAM,
            arguments = listOf(navArgument("taskId") { type = NavType.StringType })
        ) { backStackEntry ->
            val taskId = backStackEntry.arguments?.getString("taskId")
            val task = requestViewModel.getRequest(taskId)
            SubmitCompletionScreen(
                task = task,
                onBackClick = { navController.popBackStack() },
                onSubmitSuccess = { notes ->
                    taskViewModel.submitCompletion(taskId, notes)
                    navController.navigate(Routes.verifyTaskCompletion(taskId ?: ""))
                }
            )
        }

        composable(Routes.SUBMIT_COMPLETION) {
            val task = requests.firstOrNull()
            SubmitCompletionScreen(
                task = task,
                onBackClick = { navController.popBackStack() },
                onSubmitSuccess = { notes ->
                    taskViewModel.submitCompletion(task?.id, notes)
                    navController.navigate(Routes.verifyTaskCompletion(task?.id ?: ""))
                }
            )
        }

        // 16. Verify Task Completion
        composable(
            route = Routes.VERIFY_TASK_COMPLETION_PARAM,
            arguments = listOf(navArgument("taskId") { type = NavType.StringType })
        ) { backStackEntry ->
            val taskId = backStackEntry.arguments?.getString("taskId")
            val task = requestViewModel.getRequest(taskId)
            VerifyTaskCompletionScreen(
                task = task,
                onBackClick = { navController.popBackStack() },
                onVerified = { rating, reviewText ->
                    taskViewModel.verifyCompletion(taskId, rating, reviewText)
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.HOME) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.VERIFY_TASK_COMPLETION) {
            val task = requests.firstOrNull()
            VerifyTaskCompletionScreen(
                task = task,
                onBackClick = { navController.popBackStack() },
                onVerified = { rating, reviewText ->
                    taskViewModel.verifyCompletion(task?.id, rating, reviewText)
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.HOME) { inclusive = true }
                    }
                }
            )
        }

        // 17. SOS and Safety
        composable(Routes.SOS_AND_SAFETY) {
            SosAndSafetyScreen(
                onNavigate = { route -> navigateTopLevel(navController, route) },
                onBackClick = { navController.popBackStack() }
            )
        }

        // 18. Task History
        composable(Routes.TASK_HISTORY) {
            val historyTasks = requests
                .filter { it.status == RequestStatus.COMPLETED || it.status == RequestStatus.CANCELLED }
                .map { req ->
                    com.neighborhero.neighborhero.feature.task.HistoryTask(
                        title = req.title,
                        subtitle = req.description,
                        date = req.requestedTime,
                        rating = req.requesterRating,
                        status = if (req.status == RequestStatus.COMPLETED) "Completed" else "Cancelled",
                        category = if (req.status == RequestStatus.COMPLETED) "completed" else "cancelled"
                    )
                }
            TaskHistoryScreen(
                tasks = historyTasks,
                userRating = currentUser.rating,
                completedCount = currentUser.completedTasksCount,
                onBackClick = { navController.popBackStack() },
                onNavigate = { route -> navigateTopLevel(navController, route) }
            )
        }

        // 19. Notifications
        composable(Routes.NOTIFICATIONS) {
            NotificationsScreen(
                notifications = notifications,
                onNavigate = { route -> navigateTopLevel(navController, route) }
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
                onNavigate = { route -> navigateTopLevel(navController, route) }
            )
        }

        // 21. My Requests
        composable(Routes.MY_REQUESTS) {
            val myRequests = requests.filter { it.requesterName == currentUser.fullName }
            MyRequestsScreen(
                requests = myRequests,
                onRequestClick = { req -> navController.navigate(Routes.helpRequestDetails(req.id)) },
                onBackClick = { navController.popBackStack() }
            )
        }

        // 22. My Accepted Tasks
        composable(Routes.MY_ACCEPTED_TASKS) {
            val myAccepted = requests.filter { it.assignedHelperName == currentUser.fullName || it.assignedHelperId == currentUser.id }
            MyAcceptedTasksScreen(
                tasks = myAccepted,
                onTaskClick = { task -> navController.navigate(Routes.activeTask(task.id)) },
                onBackClick = { navController.popBackStack() }
            )
        }

        // 23. Edit Profile
        composable(Routes.EDIT_PROFILE) {
            EditProfileScreen(
                user = currentUser,
                onBackClick = { navController.popBackStack() },
                onSaveSuccess = { name, neighborhood, bio ->
                    homeViewModel.updateUserProfile(name, neighborhood, bio)
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
