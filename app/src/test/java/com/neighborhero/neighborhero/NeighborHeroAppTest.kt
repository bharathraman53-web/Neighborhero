package com.neighborhero.neighborhero

import com.neighborhero.neighborhero.data.model.NotificationType
import com.neighborhero.neighborhero.data.model.RequestStatus
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
            com.neighborhero.neighborhero.data.model.NotificationItem(
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
}
