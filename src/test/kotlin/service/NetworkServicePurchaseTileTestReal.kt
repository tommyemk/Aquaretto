package service

import entity.Animal
import entity.AnimalTile
import entity.PlayerType
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Testing the PurchaseExtinction method from the network standpoint.
 *
 * @property rootServiceHost hold all important components from the host
 * @property rootServiceGuest hold all important components from the guest
 * @property hostRefresh refreshes after the host did an action
 * @property guestRefresh refreshes after the guest did an action
 */
class NetworkServicePurchaseTileTestReal {

    private var rootServiceHost = RootService()
    private var rootServiceGuest = RootService()
    private var hostRefresh = TestRefreshable()
    private var guestRefresh = TestRefreshable()

    /**
     * Setting up game variables before testing.
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

        val hostState = rootServiceHost.currentGame.currentGameState[rootServiceHost.currentGame.currentState]
        val guestState = rootServiceGuest.currentGame.currentGameState[rootServiceGuest.currentGame.currentState]
        hostState.players[0].numCoins = 2
        guestState.players[0].numCoins = 2
    }

    /**
     * Testing if the player is able to purchase
     * the top tile from a different player depot.
     */
    @Test
    fun homeBuysTest(){
        var hostState = rootServiceHost.currentGame.currentGameState[rootServiceHost.currentGame.currentState]
        var guestState = rootServiceGuest.currentGame.currentGameState[rootServiceGuest.currentGame.currentState]

        val guestDepot = guestState.players[1].depot.add(AnimalTile(1, animalType = Animal.DOLPHIN))
        val guestHDepot = hostState.players[1].depot.add(AnimalTile(1, animalType = Animal.DOLPHIN))

        val guestTile = hostState.players[1].depot.first()
        rootServiceHost.playerActionService.actionCPurchase(hostState.players[1], Pair(11,11))

        rootServiceGuest.networkService.waitForState(ConnectionState.WAITING_FOR_MY_TURN)

        hostState = rootServiceHost.currentGame.currentGameState[rootServiceHost.currentGame.currentState]
        guestState = rootServiceGuest.currentGame.currentGameState[rootServiceGuest.currentGame.currentState]

        assertEquals(hostState.players[0].depot, guestState.players[0].depot)
        assertEquals(hostState.players[1].depot, guestState.players[1].depot)
    }
}