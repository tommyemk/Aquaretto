package service

import entity.*
import kotlin.test.*

/**
 * This class tests starting the game from a network standpoint.
 *
 * @property rootServiceHost hold all important components from the host
 * @property rootServiceGuest hold all important components from the guest
 * @property hostRefresh refreshes after the host did an action
 * @property guestRefresh refreshes after the guest did an action
 */
class NetworkServiceStartTest {

    private var rootServiceHost = RootService()
    private var rootServiceGuest = RootService()
    private var hostRefresh = TestRefreshable()
    private var guestRefresh = TestRefreshable()

    /**
     * Setting up all game variables before testing
     */
    @BeforeTest
    fun setUp() {
        rootServiceHost = RootService()
        rootServiceGuest = RootService()

        hostRefresh = TestRefreshable()
        guestRefresh = TestRefreshable()

        rootServiceHost.addRefreshable(hostRefresh)
        rootServiceGuest.addRefreshable(guestRefresh)
    }

    /**
     * Tries to start a game, before hosting it
     */
    @Test
    fun startGameInvalidStateTest() {
        assertFails {
            rootServiceHost.networkService.startHostedGame()
        }
    }

    /**
     * Tests starting the game from a network layer perspective
     */
    @Test
    fun startGameTest() {
        rootServiceHost.networkService.hostGame("Test Host 4", PlayerType.LOCAL_HUMAN)
        rootServiceHost.networkService.waitForState(ConnectionState.WAITING_FOR_GUESTS)

        val inviteCode = hostRefresh.inviteCode!!
        rootServiceGuest.networkService.joinGame("Test Guest 4", PlayerType.LOCAL_HUMAN, inviteCode)
        rootServiceGuest.networkService.waitForState(ConnectionState.WAITING_FOR_INIT)

        // Create test game
        rootServiceHost.aquarettoGameService.startGame(
            players = listOf(
                Pair("Test Host 4", PlayerType.LOCAL_HUMAN),
                Pair("Test Guest 4", PlayerType.ONLINE)
            ),
            isOnline = true,
            shuffle = true,
            speed = 1
        )

        val hostState = rootServiceHost.currentGame.currentGameState[rootServiceHost.currentGame.currentState]
        if (hostState.players[0].name ==
            "Test Host 4") {
            rootServiceGuest.networkService.waitForState(ConnectionState.WAITING_FOR_OPPONENT)
        } else {
            rootServiceGuest.networkService.waitForState(ConnectionState.WAITING_FOR_MY_TURN)
        }

        val guestState = rootServiceGuest.currentGame.currentGameState[rootServiceHost.currentGame.currentState]
        hostState.board.finalPile.forEachIndexed() { i, tile ->
            assertEquals(tile, guestState.board.finalPile[i])
        }
        hostState.board.mainPile.forEachIndexed() { i, tile ->
            assertEquals(tile, guestState.board.mainPile[i])
        }
        hostState.board.transporters.forEachIndexed { i, transporter ->
            assertEquals(transporter, guestState.board.transporters[i])
        }
        assertEquals(hostState.finalRound, guestState.finalRound)
    }
}