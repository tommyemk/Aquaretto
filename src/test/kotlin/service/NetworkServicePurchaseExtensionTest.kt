package service

import edu.udo.cs.sopra.ntf.BuyExpansionMessage
import edu.udo.cs.sopra.ntf.PositionPair
import entity.*
import kotlin.test.*

/**
 * Testing the PurchaseExtinction method from the network standpoint.
 *
 * @property rootServiceHost hold all important components from the host
 * @property rootServiceGuest hold all important components from the guest
 * @property hostRefresh refreshes after the host did an action
 * @property guestRefresh refreshes after the guest did an action
 */
class NetworkServicePurchaseExtensionTest {

    private var rootServiceHost = RootService()
    private var rootServiceGuest = RootService()
    private var hostRefresh = TestRefreshable()
    private var guestRefresh = TestRefreshable()

    /**
     * Instantiating all variables before testing.
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
     * Tests if code fails when the wrong player tries to make a turn.
     */
    @Test
    fun wrongTurnTest() {
        val guestState = rootServiceGuest.currentGame.currentGameState[rootServiceGuest.currentGame.currentState]

        val waterparkExtension = guestState.players[1].waterParkExtensionList[0]
        waterparkExtension.x = 8
        waterparkExtension.y = 7
        waterparkExtension.rotation = 1

        assertFails {
            rootServiceGuest.networkService.sendBuyExpansion(waterparkExtension)
        }

        assertFails {
            rootServiceHost.networkService.receiveBuyExpansion(
                BuyExpansionMessage(
                    listOf(
                        PositionPair(0, 4),
                        PositionPair(0, 5),
                        PositionPair(1, 5)
                    )
                ),
                "Test Guest 4"
            )
        }
    }

    /**
     * Checking whether waterpark is getting extended for everybody in the network.
     */
    private fun checkWaterparkEquality(
        hostState: AquarettoGameState,
        guestState: AquarettoGameState,
        extension: WaterparkExtension
    ) {
        rootServiceGuest.networkService.waitForState(ConnectionState.WAITING_FOR_MY_TURN)

        assertEquals(
            hostState.players[0].waterPark.allowedExtensionList,
            guestState.players[0].waterPark.allowedExtensionList
        )
        assertContains(guestState.players[0].waterParkExtensionList, extension)
    }

    /**
     * Checking whether the player can place big waterpark extension from a network standpoint.
     */
    @Test
    fun buyLargeTest() {
        val hostState = rootServiceHost.currentGame.currentGameState[rootServiceHost.currentGame.currentState]
        val guestState = rootServiceGuest.currentGame.currentGameState[rootServiceGuest.currentGame.currentState]

        val waterparkExtension = guestState.players[0].waterParkExtensionList[2]
        waterparkExtension.x = 9
        waterparkExtension.y = 6

        rootServiceHost.playerActionService.actionCExtendWaterPark(waterparkExtension)

        checkWaterparkEquality(hostState, guestState, waterparkExtension)
    }

    /**
     * Checking whether the player can place small waterpark extension from a network standpoint.
     */
    @Test
    fun buySmallTest0() {
        val hostState = rootServiceHost.currentGame.currentGameState[rootServiceHost.currentGame.currentState]
        val guestState = rootServiceGuest.currentGame.currentGameState[rootServiceGuest.currentGame.currentState]

        val waterparkExtension = guestState.players[0].waterParkExtensionList[0]
        waterparkExtension.x = 12
        waterparkExtension.y = 12
        waterparkExtension.rotation = 0

        rootServiceHost.playerActionService.actionCExtendWaterPark(waterparkExtension)

        checkWaterparkEquality(hostState, guestState, waterparkExtension)
    }

    /**
     * Checking whether the player can place small
     * waterpark extension from a network standpoint.
     */
    @Test
    fun buySmallTest1() {
        val hostState = rootServiceHost.currentGame.currentGameState[rootServiceHost.currentGame.currentState]
        val guestState = rootServiceGuest.currentGame.currentGameState[rootServiceGuest.currentGame.currentState]

        val waterparkExtension = guestState.players[0].waterParkExtensionList[0]
        waterparkExtension.x = 6
        waterparkExtension.y = 10
        waterparkExtension.rotation = 1

        rootServiceHost.playerActionService.actionCExtendWaterPark(waterparkExtension)

        checkWaterparkEquality(hostState, guestState, waterparkExtension)
    }

    /**
     * Checking whether the player can place
     * small waterpark extension from a network standpoint.
     */
    @Test
    fun buySmallTest2() {
        val hostState = rootServiceHost.currentGame.currentGameState[rootServiceHost.currentGame.currentState]
        val guestState = rootServiceGuest.currentGame.currentGameState[rootServiceGuest.currentGame.currentState]

        val waterparkExtension = guestState.players[0].waterParkExtensionList[0]
        waterparkExtension.x = 11
        waterparkExtension.y = 7
        waterparkExtension.rotation = 2

        rootServiceHost.playerActionService.actionCExtendWaterPark(waterparkExtension)

        checkWaterparkEquality(hostState, guestState, waterparkExtension)
    }

    /**
     * Checking whether the player can place small
     * waterpark extension from a network standpoint.
     */
    @Test
    fun buySmallTest3() {
        val hostState = rootServiceHost.currentGame.currentGameState[rootServiceHost.currentGame.currentState]
        val guestState = rootServiceGuest.currentGame.currentGameState[rootServiceGuest.currentGame.currentState]

        val waterparkExtension = guestState.players[0].waterParkExtensionList[0]
        waterparkExtension.x = 12
        waterparkExtension.y = 11
        waterparkExtension.rotation = 3

        rootServiceHost.playerActionService.actionCExtendWaterPark(waterparkExtension)

        checkWaterparkEquality(hostState, guestState, waterparkExtension)
    }
}