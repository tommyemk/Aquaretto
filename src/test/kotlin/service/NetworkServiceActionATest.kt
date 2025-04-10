package service

import edu.udo.cs.sopra.ntf.AddTileToTruckMessage
import entity.*
import kotlin.test.*

/**
 * This class tests ActionA from a network standpoint.
 *
 * @property rootServiceHost hold all important components from the host
 * @property rootServiceGuest hold all important components from the guest
 * @property hostRefresh refreshes after the host did an action
 * @property guestRefresh refreshes after the guest did an action
 */
class NetworkServiceActionATest {

    private var rootServiceHost = RootService()
    private var rootServiceGuest = RootService()
    private var hostRefresh = TestRefreshable()
    private var guestRefresh = TestRefreshable()

    /**
     * This method instantiates all required network
     * variables in order to test ActionA from a network standpoint
     */
    @BeforeTest
    fun setUp() {
        rootServiceHost = RootService()
        rootServiceGuest = RootService()

        hostRefresh = TestRefreshable()
        guestRefresh = TestRefreshable()

        rootServiceHost.addRefreshable(hostRefresh)
        rootServiceGuest.addRefreshable(guestRefresh)

        rootServiceHost.networkService.hostGame("Test Host 4", PlayerType.LOCAL_HUMAN)
        rootServiceHost.networkService.waitForState(ConnectionState.WAITING_FOR_GUESTS)

        val inviteCode = hostRefresh.inviteCode!!
        rootServiceGuest.networkService.joinGame("Test Guest 4", PlayerType.LOCAL_HUMAN, inviteCode)
        rootServiceGuest.networkService.waitForState(ConnectionState.WAITING_FOR_INIT)

        rootServiceHost.aquarettoGameService.startGame(
            players = listOf(
                Pair("Test Host 4", PlayerType.LOCAL_HUMAN),
                Pair("Test Guest 4", PlayerType.ONLINE)
            ),
            isOnline = true,
            shuffle = false,
            speed = 1
        )
        rootServiceGuest.networkService.waitForState(ConnectionState.WAITING_FOR_OPPONENT)
    }

    /**
     * Tests if code fails when the wrong player tries to make a turn
     */
    @Test
    fun wrongTurnTest() {
        assertFails {
            rootServiceGuest.networkService.sendAddTileToTruck(0)
        }

        assertFails {
            rootServiceHost.networkService.receiveAddTileToTruck(AddTileToTruckMessage(0), "Test Guest 4")
        }
    }

    /**
     * Tests if action a gets replicated correctly
     */
    @Test
    fun actionATest() {
        rootServiceHost.playerActionService.actionA(1)

        rootServiceGuest.networkService.waitForState(ConnectionState.WAITING_FOR_MY_TURN)

        val hostState = rootServiceHost.currentGame.currentGameState[rootServiceHost.currentGame.currentState]
        val guestState = rootServiceGuest.currentGame.currentGameState[rootServiceGuest.currentGame.currentState]

        assertEquals(hostState.board.mainPile.first(), guestState.board.mainPile.first())

        hostState.board.transporters.forEachIndexed() { i, trans ->
            assertEquals(trans.tiles, guestState.board.transporters[i].tiles)
            assertEquals(trans.taken, guestState.board.transporters[i].taken)
        }
    }
}