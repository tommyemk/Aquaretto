package service

import entity.*
import kotlin.test.*

/**
 * Testing the discard method from the network standpoint.
 *
 * @property rootServiceHost hold all important components from the host
 * @property rootServiceGuest hold all important components from the guest
 * @property hostRefresh refreshes after the host did an action
 * @property guestRefresh refreshes after the guest did an action
 */
class NetworkServiceDiscardTest {

    private var rootServiceHost = RootService()
    private var rootServiceGuest = RootService()
    private var hostRefresh = TestRefreshable()
    private var guestRefresh = TestRefreshable()

    /**
     * Setting up game variables before testing discard.
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
        val gameStateHost = rootServiceHost.currentGame.currentGameState[rootServiceHost.currentGame.currentState]
        val gameStateGuest = rootServiceGuest.currentGame.currentGameState[rootServiceGuest.currentGame.currentState]
        gameStateHost.players[0].depot.add(AnimalTile(1, animalType = Animal.HIPPO))
        gameStateGuest.players[0].depot.add(AnimalTile(1, animalType = Animal.HIPPO))
        gameStateGuest.players[0].numCoins = 2
        gameStateHost.players[0].numCoins = 2
    }

    /**
     * Tests if the player is able to discard from a network standpoint.
     */
    @Test
    fun testDiscard() {
        rootServiceHost.playerActionService.actionCDiscardTile()

        rootServiceGuest.networkService.waitForState(ConnectionState.WAITING_FOR_MY_TURN)
        val gameStateHost = rootServiceHost.currentGame.currentGameState[rootServiceHost.currentGame.currentState]
        val gameStateGuest = rootServiceGuest.currentGame.currentGameState[rootServiceGuest.currentGame.currentState]
        assertEquals(gameStateGuest.players[0].depot, gameStateHost.players[0].depot)
        assertEquals(gameStateGuest.players[0].numCoins, gameStateHost.players[0].numCoins)
    }
}