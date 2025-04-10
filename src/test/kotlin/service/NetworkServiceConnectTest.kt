package service

import entity.PlayerType
import kotlin.reflect.KProperty0
import kotlin.test.*

/**
 * Tests [NetworkService.hostGame] and [NetworkService.joinGame]
 */
class NetworkServiceConnectTest {

    private var networkServiceHost = NetworkService(RootService())
    private var networkServiceJoin = NetworkService(RootService())
    private var hostRefresh = TestRefreshable()
    private var guestRefresh = TestRefreshable()

    /**
     * Waits until [variable] changes to true or more than [timeout] ms passed.
     * Used for testing purposes
     *
     * @throws IllegalStateException when [timeout] ms passed without [variable] turning true
     */
    private fun waitForTrue(variable: KProperty0<Boolean>, timeout: Int = 5000) {
        var timePassed = 0
        while (timePassed < timeout) {
            if (variable.get())
                return
            else {
                Thread.sleep(100)
                timePassed += 100
            }
        }
        error("Variable ${variable.name} did not turn true after waiting $timeout ms")
    }

    /**
     * Setting up all necessarily network variables.
     */
    @BeforeTest
    fun setUp() {
        networkServiceHost = NetworkService(RootService())
        networkServiceJoin = NetworkService(RootService())

        hostRefresh = TestRefreshable()
        guestRefresh = TestRefreshable()

        networkServiceHost.addRefreshable(hostRefresh)
        networkServiceJoin.addRefreshable(guestRefresh)
    }

    /**
     * Test hosting with an invalid name
     */
    @Test
    fun hostInvalidTest() {
        assertFails { networkServiceHost.hostGame("", PlayerType.LOCAL_HUMAN) }
    }

    /**
     * Test joining with an invalid invite code
     */
    @Test
    fun joinInvalidTest() {
        // I hope no one hosts a game with "invalidInviteCode" x3
        assertFails {
            networkServiceJoin.joinGame("Name", PlayerType.LOCAL_HUMAN, "invalidInviteCode")
            networkServiceJoin.waitForState(ConnectionState.WAITING_FOR_INIT)
        }
    }

    /**
     * Test hosting a game and joining
     */
    @Test
    fun connectTest() {
        networkServiceHost.hostGame("Test Host 4", PlayerType.LOCAL_HUMAN)
        networkServiceHost.waitForState(ConnectionState.WAITING_FOR_HOST_CONFIRMATION)
        networkServiceHost.waitForState(ConnectionState.WAITING_FOR_GUESTS)

        assert(hostRefresh.refreshAfterHostGame)
        assertNotNull(hostRefresh.inviteCode)
        val inviteCode = hostRefresh.inviteCode!!

        networkServiceJoin.joinGame("Test Guest 4", PlayerType.LOCAL_HUMAN, inviteCode)
        networkServiceJoin.waitForState(ConnectionState.WAITING_FOR_JOIN_CONFIRMATION)
        networkServiceJoin.waitForState(ConnectionState.WAITING_FOR_INIT)

        assert(guestRefresh.refreshAfterJoinGame)
        assertNotNull(guestRefresh.playerNames)
        assertContains(guestRefresh.playerNames!!, "Test Host 4")
        assertContains(guestRefresh.playerNames!!, "Test Guest 4")

        waitForTrue(hostRefresh::refreshAfterPlayerJoin)
        assertContains(hostRefresh.playerNames!!, "Test Host 4")
        assertContains(hostRefresh.playerNames!!, "Test Guest 4")
    }
}