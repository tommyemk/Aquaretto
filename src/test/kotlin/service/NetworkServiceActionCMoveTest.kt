package service

import entity.*
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * This class tests ActionA from a network standpoint.
 *
 * @property rootServiceHost hold all important components from the host
 * @property rootServiceGuest hold all important components from the guest
 * @property hostRefresh refreshes after the host did an action
 * @property guestRefresh refreshes after the guest did an action
 */
class NetworkServiceActionCMoveTest {

    private var rootServiceHost = RootService()
    private var rootServiceGuest = RootService()
    private var hostRefresh = TestRefreshable()
    private var guestRefresh = TestRefreshable()

    /**
     * Setting up all important game variables before testing the ActionCMove.
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
     * Testing ActionCMove()
     */
    @Test
    fun actionCMoveTest() {
        val hostState = rootServiceHost.currentGame.currentGameState[rootServiceHost.currentGame.currentState]
        val guestState = rootServiceGuest.currentGame.currentGameState[rootServiceGuest.currentGame.currentState]

        guestState.players[0].depot.add(AnimalTile(1, animalType = Animal.DOLPHIN))
        hostState.players[0].depot.add(AnimalTile(1, animalType = Animal.DOLPHIN))

        rootServiceHost.playerActionService.actionCMove(hostState.players[0].depot[0], Pair(11, 11))

        rootServiceGuest.networkService.waitForState(ConnectionState.WAITING_FOR_MY_TURN)

        assertEquals(
            hostState.players[0].waterPark.fieldMap[Pair(11, 11)],
            guestState.players[0].waterPark.fieldMap[Pair(11, 11)]
        )
    }

    /**
     * Testing ActionCMove() with special tests.
     */
    @Test
    fun actionCMoveSpecialTest() {
        var hostState = rootServiceHost.currentGame.currentGameState[rootServiceHost.currentGame.currentState]
        var guestState = rootServiceGuest.currentGame.currentGameState[rootServiceGuest.currentGame.currentState]

        hostState.players[0].waterPark.fieldMap[Pair(9, 10)] = AnimalTile(15, animalType = Animal.HIPPO)
        hostState.players[0].waterPark.fieldMap[Pair(10, 10)] = AnimalTile(16, animalType = Animal.HIPPO)
        hostState.players[0].waterPark.fieldMap[Pair(11, 10)] = AnimalTile(12, animalType = Animal.HIPPO, isMale = true)
        guestState.players[0].waterPark = hostState.players[0].waterPark.clone()

        guestState.players[0].depot.add(AnimalTile(1, animalType = Animal.HIPPO, isFemale = true))
        hostState.players[0].depot.add(AnimalTile(1, animalType = Animal.HIPPO, isFemale = true))

        rootServiceHost.playerActionService.actionCMove(hostState.players[0].depot[0], Pair(11, 11))
        hostState = rootServiceHost.currentGame.currentGameState[rootServiceHost.currentGame.currentState]
        rootServiceHost.playerActionService.placeOffspring(
            Pair(10, 11), hostState.board.offspring
                .find { it.animalType == Animal.HIPPO }!!
        )
        rootServiceHost.playerActionService.placeCoworker(Pair(9, 11), Coworker(CoworkerTask.TRAINER))

        rootServiceGuest.networkService.waitForState(ConnectionState.WAITING_FOR_MY_TURN)
        hostState = rootServiceHost.currentGame.currentGameState[rootServiceHost.currentGame.currentState]
        guestState = rootServiceGuest.currentGame.currentGameState[rootServiceGuest.currentGame.currentState]

        assertEquals(hostState.players[0].waterPark, guestState.players[0].waterPark)
        assertEquals(hostState.players[0].depot, guestState.players[0].depot)
    }

    /**
     * Testing ActionCMove() with its coworkers.
     */
    @Test
    fun actionCMoveCoworkerTest() {
        val hostState = rootServiceHost.currentGame.currentGameState[rootServiceHost.currentGame.currentState]
        var guestState = rootServiceGuest.currentGame.currentGameState[rootServiceGuest.currentGame.currentState]
        hostState.players[0].waterPark.fieldMap[Waterpark.CASH_POS_2] = Coworker(CoworkerTask.CASHIER)
        guestState.players[0].waterPark.fieldMap[Waterpark.CASH_POS_2] = Coworker(CoworkerTask.CASHIER)

        rootServiceHost.playerActionService.actionCMove(
            hostState.players[0].waterPark.fieldMap[Waterpark.CASH_POS_2]!!,
            Waterpark.ANIM_POS_1
        )

        rootServiceGuest.networkService.waitForState(ConnectionState.WAITING_FOR_MY_TURN)
        guestState = rootServiceGuest.currentGame.currentGameState[rootServiceGuest.currentGame.currentState]
        val guestCoworkerKeys = guestState.players[0].waterPark.fieldMap
            .filterValues { it is Coworker && it.coworkerTask == CoworkerTask.ANIMAL_KEEPER }
            .keys
        assertTrue(Waterpark.ANIM_POS_1 in guestCoworkerKeys || Waterpark.ANIM_POS_2 in guestCoworkerKeys)

    }

    /**
     * Testing ActionCMove() with its coworkers.
     */
    @Test
    fun actionCMoveCoworkerTest2() {
        val hostState = rootServiceHost.currentGame.currentGameState[rootServiceHost.currentGame.currentState]
        var guestState = rootServiceGuest.currentGame.currentGameState[rootServiceGuest.currentGame.currentState]
        hostState.players[0].waterPark.fieldMap[Waterpark.CASH_POS_1] = Coworker(CoworkerTask.CASHIER)
        guestState.players[0].waterPark.fieldMap[Waterpark.CASH_POS_1] = Coworker(CoworkerTask.CASHIER)

        rootServiceHost.playerActionService.actionCMove(
            hostState.players[0].waterPark.fieldMap[Waterpark.CASH_POS_1]!!,
            Pair(11, 11)
        )

        rootServiceGuest.networkService.waitForState(ConnectionState.WAITING_FOR_MY_TURN)
        guestState = rootServiceGuest.currentGame.currentGameState[rootServiceGuest.currentGame.currentState]
        val guestCoworkerKeys = guestState.players[0].waterPark.fieldMap
            .filterValues { it is Coworker && it.coworkerTask == CoworkerTask.TRAINER }
            .keys
        assertTrue(Pair(11, 11) in guestCoworkerKeys)

    }

    /**
     * Testing ActionCMove() with its coworkers.
     */
    @Test
    fun actionCMoveCoworkerTest3() {
        val hostState = rootServiceHost.currentGame.currentGameState[rootServiceHost.currentGame.currentState]
        var guestState = rootServiceGuest.currentGame.currentGameState[rootServiceGuest.currentGame.currentState]
        hostState.players[0].waterPark.fieldMap[Waterpark.ANIM_POS_1] = Coworker(CoworkerTask.ANIMAL_KEEPER)
        guestState.players[0].waterPark.fieldMap[Waterpark.ANIM_POS_1] = Coworker(CoworkerTask.ANIMAL_KEEPER)

        rootServiceHost.playerActionService.actionCMove(
            hostState.players[0].waterPark.fieldMap[Waterpark.ANIM_POS_1]!!,
            Waterpark.DEPOT_POS
        )

        rootServiceGuest.networkService.waitForState(ConnectionState.WAITING_FOR_MY_TURN)
        guestState = rootServiceGuest.currentGame.currentGameState[rootServiceGuest.currentGame.currentState]
        val guestCoworkerKeys = guestState.players[0].waterPark.fieldMap
            .filterValues { it is Coworker && it.coworkerTask == CoworkerTask.MANAGER }
            .keys
        assertTrue(Waterpark.DEPOT_POS in guestCoworkerKeys)

    }

    /**
     * Testing ActionCMove() with its coworkers.
     */
    @Test
    fun actionCMoveCoworkerTest4() {
        val hostState = rootServiceHost.currentGame.currentGameState[rootServiceHost.currentGame.currentState]
        var guestState = rootServiceGuest.currentGame.currentGameState[rootServiceGuest.currentGame.currentState]
        hostState.players[0].waterPark.fieldMap[Pair(11, 11)] = Coworker(CoworkerTask.ANIMAL_KEEPER)
        guestState.players[0].waterPark.fieldMap[Pair(11, 11)] = Coworker(CoworkerTask.ANIMAL_KEEPER)

        rootServiceHost.playerActionService.actionCMove(
            hostState.players[0].waterPark.fieldMap[Pair(11, 11)]!!,
            Waterpark.CASH_POS_2
        )

        rootServiceGuest.networkService.waitForState(ConnectionState.WAITING_FOR_MY_TURN)
        guestState = rootServiceGuest.currentGame.currentGameState[rootServiceGuest.currentGame.currentState]
        val guestCoworkerKeys = guestState.players[0].waterPark.fieldMap
            .filterValues { it is Coworker && it.coworkerTask == CoworkerTask.CASHIER }
            .keys
        assertTrue(Waterpark.CASH_POS_1 in guestCoworkerKeys || Waterpark.CASH_POS_2 in guestCoworkerKeys)
    }
}