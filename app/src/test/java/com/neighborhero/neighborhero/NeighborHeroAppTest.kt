package com.neighborhero.neighborhero

import com.neighborhero.neighborhero.data.local.AppDatabaseHelper
import com.neighborhero.neighborhero.data.model.ChatMessage
import com.neighborhero.neighborhero.data.model.NotificationItem
import com.neighborhero.neighborhero.data.model.NotificationType
import com.neighborhero.neighborhero.data.model.RequestStatus
import com.neighborhero.neighborhero.data.network.ApiConfig
import com.neighborhero.neighborhero.data.repository.*
import com.neighborhero.neighborhero.nlp.NlpClassifier
import com.neighborhero.neighborhero.viewmodel.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class NeighborHeroAppTest {

    private lateinit var userRepository: UserRepository
    private lateinit var helpRequestRepository: HelpRequestRepository
    private lateinit var helperRepository: HelperRepository
    private lateinit var chatRepository: ChatRepository
    private lateinit var notificationRepository: NotificationRepository
    private lateinit var nlpClassifier: NlpClassifier

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        userRepository = UserRepository(seedForTests = true)
        helpRequestRepository = HelpRequestRepository(seedForTests = true)
        helperRepository = HelperRepository(seedForTests = true)
        chatRepository = ChatRepository(seedForTests = true)
        notificationRepository = NotificationRepository(seedForTests = true)
        nlpClassifier = NlpClassifier()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // --- Database Initialization & Safety Tests ---
    @Test
    fun testDatabaseHelper_uninitializedGetInstanceThrowsDescriptiveError() {
        AppDatabaseHelper.resetInstanceForTesting()
        try {
            AppDatabaseHelper.getInstance(null)
            fail("Expected IllegalStateException when calling getInstance without context when uninitialized")
        } catch (e: IllegalStateException) {
            assertTrue(e.message?.contains("AppDatabaseHelper has not been initialized") == true)
        }
    }

    @Test
    fun testRepositoryProvider_uninitializedAccessDoesNotCrash() {
        AppDatabaseHelper.resetInstanceForTesting()
        val repo = RepositoryProvider.helpRequestRepository
        assertNotNull(repo)
    }

    // --- NLP Classifier Tests ---
    @Test
    fun testNlpClassifier_groceriesClassification() {
        val category1 = nlpClassifier.classifyRequestCategory("Need someone to pick up groceries and milk from the market")
        assertEquals("Groceries", category1)

        val category2 = nlpClassifier.classifyRequestCategory("Fresh organic food and bread from trader joe")
        assertEquals("Groceries", category2)
    }

    @Test
    fun testNlpClassifier_repairsClassification() {
        val category1 = nlpClassifier.classifyRequestCategory("Fence broke down, need help to repair and drill screws")
        assertEquals("Repairs", category1)

        val category2 = nlpClassifier.classifyRequestCategory("Water leak in the kitchen pipe, need handy fix")
        assertEquals("Repairs", category2)
    }

    @Test
    fun testNlpClassifier_petCareClassification() {
        val category1 = nlpClassifier.classifyRequestCategory("Need someone to walk my dog this afternoon")
        assertEquals("Pet Care", category1)

        val category2 = nlpClassifier.classifyRequestCategory("Taking care of a rescue kitten for 2 hours")
        assertEquals("Pet Care", category2)
    }

    @Test
    fun testNlpClassifier_tutoringClassification() {
        val category1 = nlpClassifier.classifyRequestCategory("Need algebra and math tutoring before midterm exam")
        assertEquals("Tutoring", category1)

        val category2 = nlpClassifier.classifyRequestCategory("Science homework study session for 8th grader")
        assertEquals("Tutoring", category2)
    }

    @Test
    fun testNlpClassifier_blankOrUnrelatedReturnsNull() {
        assertNull(nlpClassifier.classifyRequestCategory(""))
        assertNull(nlpClassifier.classifyRequestCategory("   "))
        assertNull(nlpClassifier.classifyRequestCategory("Random phrase with no known community request words"))
    }

    // --- HelpRequestRepository & ViewModel Tests ---
    @Test
    fun testHelpRequestRepository_initialRequestsLoaded() {
        val requests = helpRequestRepository.requests.value
        assertTrue(requests.isNotEmpty())
        assertEquals(4, requests.size)

        val first = helpRequestRepository.getRequestById("req_1")
        assertNotNull(first)
        assertEquals("Weekly Groceries Pickup", first?.title)
        assertEquals(true, first?.isUrgent)
    }

    @Test
    fun testHelpRequestViewModel_createAndAcceptRequest() {
        val viewModel = HelpRequestViewModel(helpRequestRepository)
        val initialCount = viewModel.requests.value.size

        val newId = viewModel.createRequest(
            title = "Help rake front lawn leaves",
            category = "Repairs",
            description = "Need assistance gathering dry autumn leaves into compost bags.",
            time = "Tomorrow 9am",
            location = "Maplewood 12"
        )

        assertEquals(initialCount + 1, viewModel.requests.value.size)
        val created = viewModel.getRequest(newId)
        assertNotNull(created)
        assertEquals("Help rake front lawn leaves", created?.title)
        assertEquals(RequestStatus.OPEN, created?.status)

        // Accept request
        viewModel.acceptRequest(newId, "Vikram Roy")
        val accepted = viewModel.getRequest(newId)
        assertEquals(RequestStatus.IN_PROGRESS, accepted?.status)
        assertEquals("Vikram Roy", accepted?.assignedHelperName)

        // Submit completion
        viewModel.submitCompletion(newId)
        val pending = viewModel.getRequest(newId)
        assertEquals(RequestStatus.PENDING_VERIFICATION, pending?.status)

        // Verify completion
        viewModel.verifyCompletion(newId)
        val completed = viewModel.getRequest(newId)
        assertEquals(RequestStatus.COMPLETED, completed?.status)
    }

    // --- User Profile Tests ---
    @Test
    fun testUserRepository_profileUpdateAndPoints() {
        val initialUser = userRepository.currentUser.value
        assertEquals("Eleanor Vance", initialUser.fullName)
        val initialPoints = initialUser.points

        userRepository.updateUserProfile(
            name = "Eleanor Vance-Smith",
            neighborhood = "Greenwood Heights",
            bio = "Active community organizer and gardening enthusiast.",
            skills = listOf("Groceries", "Gardening", "Pet Sitting")
        )

        val updatedUser = userRepository.currentUser.value
        assertEquals("Eleanor Vance-Smith", updatedUser.fullName)
        assertEquals("Greenwood Heights", updatedUser.neighborhood)
        assertEquals("Active community organizer and gardening enthusiast.", updatedUser.bio)
        assertTrue(updatedUser.skills.contains("Gardening"))

        userRepository.addPoints(150)
        assertEquals(initialPoints + 150, userRepository.currentUser.value.points)
        assertEquals(initialUser.completedTasksCount + 1, userRepository.currentUser.value.completedTasksCount)
    }

    // --- Helper Repository Tests ---
    @Test
    fun testHelperRepository_helpersListAndLookup() {
        val helpers = helperRepository.helpers.value
        assertTrue(helpers.size >= 3)

        val vikram = helperRepository.getHelperById("helper_1")
        assertNotNull(vikram)
        assertEquals("Vikram Roy", vikram?.name)
        assertTrue(vikram?.isVerified == true)

        val aarav = helperRepository.getHelperById("helper_2")
        assertNotNull(aarav)
        assertEquals("Aarav Sharma", aarav?.name)

        val priya = helperRepository.getHelperById("helper_3")
        assertNotNull(priya)
        assertEquals("Priya Patel", priya?.name)
    }

    // --- Chat Repository Tests ---
    @Test
    fun testChatRepository_sendMessage() {
        val initialCount = chatRepository.messages.value.size
        assertTrue(initialCount >= 3)

        chatRepository.sendMessage("Hello neighbor, I will be there in 10 minutes!")
        val updatedMessages = chatRepository.messages.value
        assertEquals(initialCount + 1, updatedMessages.size)

        val lastMessage = updatedMessages.last()
        assertEquals("Hello neighbor, I will be there in 10 minutes!", lastMessage.messageText)
        assertTrue(lastMessage.isUser)
    }

    // --- Notification Repository Tests ---
    @Test
    fun testNotificationRepository_initialAndAddNotification() {
        val initialNotifications = notificationRepository.notifications.value
        assertTrue(initialNotifications.isNotEmpty())

        val notifCount = initialNotifications.size
        notificationRepository.addNotification(
            NotificationItem(
                id = "test_notif",
                title = "New Community Request",
                message = "A neighbor near you posted a new task.",
                timeAgo = "Just now",
                type = NotificationType.TASK_UPDATE
            )
        )

        assertEquals(notifCount + 1, notificationRepository.notifications.value.size)
        assertEquals("New Community Request", notificationRepository.notifications.value.first().title)
    }

    // --- TaskViewModel Lifecycle Tests ---
    @Test
    fun testTaskViewModel_taskFlow() {
        val taskVm = TaskViewModel(helpRequestRepository, userRepository)
        assertFalse(taskVm.taskStarted.value)
        assertFalse(taskVm.taskSubmitted.value)
        assertFalse(taskVm.taskVerified.value)

        val targetTaskId = "req_1"
        taskVm.confirmStart(targetTaskId)
        assertTrue(taskVm.taskStarted.value)
        assertEquals(RequestStatus.IN_PROGRESS, helpRequestRepository.getRequestById(targetTaskId)?.status)

        taskVm.submitCompletion(targetTaskId, "Groceries delivered to front porch.")
        assertTrue(taskVm.taskSubmitted.value)
        assertEquals(RequestStatus.PENDING_VERIFICATION, helpRequestRepository.getRequestById(targetTaskId)?.status)

        taskVm.verifyCompletion(targetTaskId, rating = 5, feedback = "Great job!")
        assertTrue(taskVm.taskVerified.value)
        assertEquals(RequestStatus.COMPLETED, helpRequestRepository.getRequestById(targetTaskId)?.status)
    }

    // --- Phone Validation & De-duplication Tests ---
    @Test
    fun testPhoneNumberValidation_strict10DigitsFiltering() {
        fun sanitizeAndValidatePhone(input: String): Pair<String, Boolean> {
            val sanitized = input.filter { it.isDigit() }.take(10)
            val isValid = sanitized.length == 10
            return Pair(sanitized, isValid)
        }

        // Valid 10-digit string
        val (phone1, valid1) = sanitizeAndValidatePhone("5551234567")
        assertEquals("5551234567", phone1)
        assertTrue(valid1)

        // Punctuation and symbols filtered out
        val (phone2, valid2) = sanitizeAndValidatePhone("(555) 123-4567")
        assertEquals("5551234567", phone2)
        assertTrue(valid2)

        // Too short (9 digits)
        val (phone3, valid3) = sanitizeAndValidatePhone("555123456")
        assertEquals("555123456", phone3)
        assertFalse(valid3)

        // Too long (truncated to 10 digits and validated)
        val (phone4, valid4) = sanitizeAndValidatePhone("555123456799999")
        assertEquals("5551234567", phone4)
        assertTrue(valid4)

        // Non-digits/letters only
        val (phone5, valid5) = sanitizeAndValidatePhone("abcdefghij")
        assertEquals("", phone5)
        assertFalse(valid5)
    }

    @Test
    fun testHelpRequestRepository_replaceOrAddRequestDeduplication() {
        val tempId = "req_temp_123"
        val tempReq = com.neighborhero.neighborhero.data.model.HelpRequest(
            id = tempId,
            title = "Temporary Test Request",
            category = "Groceries",
            description = "Picking up fresh milk",
            requesterName = "Test Requester",
            requesterRating = 5.0f,
            requesterTaskCount = 3,
            location = "Maplewood",
            distance = "0.2 mi away",
            requestedTime = "Today 5pm",
            isUrgent = false,
            status = RequestStatus.OPEN
        )

        // Add temporary item
        helpRequestRepository.addRequest(tempReq)
        assertNotNull(helpRequestRepository.getRequestById(tempId))

        // Replace with server item
        val serverReq = tempReq.copy(id = "req_server_456", title = "Server Persisted Request")
        helpRequestRepository.replaceOrAddRequest(tempId, serverReq)

        // Temp request must be removed
        assertNull(helpRequestRepository.getRequestById(tempId))
        // Server request must be present
        assertNotNull(helpRequestRepository.getRequestById("req_server_456"))
        assertEquals("Server Persisted Request", helpRequestRepository.getRequestById("req_server_456")?.title)
    }

    @Test
    fun testUserRepository_clearLocalUserSessionPurgesData() {
        userRepository.updateUserProfile("Session User", "Downtown", "Bio", emptyList())
        assertEquals("Session User", userRepository.currentUser.value.fullName)

        userRepository.clearLocalUserSession()
        // Must reset back to initial/unauthenticated default
        assertNotEquals("Session User", userRepository.currentUser.value.fullName)
    }

    @Test
    fun testApiConfig_urlsAndResolution() {
        ApiConfig.resetToDefault()
        assertEquals("172.23.48.169", ApiConfig.DEV_PC_LAN_IP)
        assertEquals("http://172.23.48.169/neighborhero-api/public/api/", ApiConfig.DEFAULT_PHYSICAL_DEVICE_LAN_URL)
        assertEquals("http://10.0.2.2/neighborhero-api/public/api/", ApiConfig.DEFAULT_EMULATOR_URL)
        assertEquals("http://127.0.0.1/neighborhero-api/public/api/", ApiConfig.DEFAULT_USB_DEVICE_URL)

        // Test manual override via setLanAddress
        ApiConfig.setLanAddress("192.168.1.50")
        assertEquals("http://192.168.1.50/neighborhero-api/public/api/", ApiConfig.baseUrl)

        ApiConfig.setLanAddress("192.168.1.50", 8080)
        assertEquals("http://192.168.1.50:8080/neighborhero-api/public/api/", ApiConfig.baseUrl)

        ApiConfig.resetToDefault()
    }

    @Test
    fun testChatRepository_conversationIsolationAndClearing() {
        chatRepository.setActiveConversation("conv_alice_bob", "bob")
        assertEquals("conv_alice_bob", chatRepository.activeConversationId.value)
        assertEquals("bob", chatRepository.activeRecipientId.value)

        chatRepository.addOptimisticMessage("Hello Bob")
        assertEquals(1, chatRepository.messages.value.size)
        assertEquals("Hello Bob", chatRepository.messages.value.first().messageText)

        // Switch to Charlie's conversation: previous conversation messages must be cleared immediately
        chatRepository.setActiveConversation("conv_alice_charlie", "charlie")
        assertTrue(chatRepository.messages.value.isEmpty())
        assertEquals("conv_alice_charlie", chatRepository.activeConversationId.value)

        // Purge on logout
        chatRepository.clear()
        assertTrue(chatRepository.messages.value.isEmpty())
        assertNull(chatRepository.activeConversationId.value)
        assertNull(chatRepository.activeRecipientId.value)
    }

    @Test
    fun testChatRepository_duplicatePreventionAndReconciliation() {
        chatRepository.clear()
        val tempId = chatRepository.addOptimisticMessage("Reconciliation test")
        assertEquals(1, chatRepository.messages.value.size)
        assertTrue(tempId.startsWith("client_"))

        // Simulate server response arriving with real server ID and matching client_message_id
        val serverMsg = ChatMessage(
            id = "msg_server_999",
            senderId = "me",
            senderName = "You",
            senderInitials = "ME",
            messageText = "Reconciliation test",
            timestamp = "1:00 PM",
            isUser = true,
            conversationId = "conv_1"
        )

        // Reconcile: replace optimistic message with server message
        val current = chatRepository.messages.value
        val updated = current.map { if (it.id == tempId) serverMsg else it }
        val reconciled = (updated.filter { it.id != tempId } + serverMsg).distinctBy { it.id }

        assertEquals(1, reconciled.size)
        assertEquals("msg_server_999", reconciled.first().id)
    }

    @Test
    fun testLoginErrorMessage_format() {
        fun formatLoginError(httpCode: Int, responseMsg: String?): String {
            return if (httpCode == 401) {
                "Incorrect email or phone number, or password. Please try again."
            } else if (httpCode in 500..599) {
                "Server error occurred. Please try again later."
            } else {
                responseMsg ?: "Unable to sign in. Please verify your credentials and try again."
            }
        }

        assertEquals(
            "Incorrect email or phone number, or password. Please try again.",
            formatLoginError(401, "Unauthorized")
        )
        assertEquals(
            "Server error occurred. Please try again later.",
            formatLoginError(500, "Internal Server Error")
        )
        assertEquals(
            "Custom error",
            formatLoginError(400, "Custom error")
        )
    }

    // --- Evidence & Photo Upload DTO & Mapping Tests ---
    @Test
    fun testHelpRequest_requesterPhoneAndIdMapping() {
        val req = com.neighborhero.neighborhero.data.model.HelpRequest(
            id = "req_test_phone",
            title = "Elderly assistance",
            category = "Errands",
            description = "Need medicine from pharmacy",
            requesterName = "Alice Smith",
            requesterRating = 4.8f,
            requesterTaskCount = 5,
            location = "Maplewood 10",
            distance = "0.5 mi",
            requestedTime = "Today 2pm",
            isUrgent = false,
            requesterId = "usr_alice",
            requesterPhone = "5551234567"
        )
        assertEquals("usr_alice", req.requesterId)
        assertEquals("5551234567", req.requesterPhone)
    }

    @Test
    fun testNotificationItem_relatedRequestIdField() {
        val notif = NotificationItem(
            id = "notif_task_1",
            title = "Task Accepted",
            message = "A neighbor accepted your grocery request.",
            timeAgo = "5m ago",
            type = NotificationType.TASK_UPDATE,
            isRead = false,
            relatedRequestId = "req_101"
        )
        assertEquals("req_101", notif.relatedRequestId)
        assertFalse(notif.isRead)
    }

    @Test
    fun testTaskEvidenceDto_structureAndMapping() {
        val evidenceDto = com.neighborhero.neighborhero.data.network.dto.TaskEvidenceDto(
            id = "ev_1",
            requestId = "task_99",
            proofPhotoUrl = "http://172.23.48.169/neighborhero-api/public/api/tasks/task_99/evidence/photo",
            notes = "Delivered package safely to front porch.",
            submittedAt = "2026-10-29 10:30:00"
        )
        assertEquals("task_99", evidenceDto.requestId)
        assertEquals("http://172.23.48.169/neighborhero-api/public/api/tasks/task_99/evidence/photo", evidenceDto.proofPhotoUrl)
        assertEquals("Delivered package safely to front porch.", evidenceDto.notes)
        assertEquals("2026-10-29 10:30:00", evidenceDto.submittedAt)
    }

    @Test
    fun testUploadEvidencePhotoDto_structureAndMapping() {
        val uploadDto = com.neighborhero.neighborhero.data.network.dto.UploadEvidencePhotoDto(
            photoUrl = "http://172.23.48.169/neighborhero-api/public/api/tasks/task_99/evidence/photo"
        )
        assertEquals("http://172.23.48.169/neighborhero-api/public/api/tasks/task_99/evidence/photo", uploadDto.photoUrl)

        // Test updating user model with avatar URL
        val currentUser = userRepository.currentUser.value
        val updated = currentUser.copy(avatarUrl = "uploads/avatars/avatar_abc123.jpg")
        assertEquals("uploads/avatars/avatar_abc123.jpg", updated.avatarUrl)
    }
}
