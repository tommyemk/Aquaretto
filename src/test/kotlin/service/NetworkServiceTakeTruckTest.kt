package service

import entity.*
import kotlin.test.*

/**
 * This class tests if the current player is able
 * to take a transporter from a network standpoint.
 *
 * @property rootServiceHost hold all important components from the host
 * @property rootServiceGuest hold all important components from the guest
 * @property hostRefresh refreshes after the host did an action
 * @property guestRefresh refreshes after the guest did an action
 */
class NetworkServiceTakeTruckTest {

    private var rootServiceHost = RootService()
    private var rootServiceGuest = RootService()
    private var hostRefresh = TestRefreshable()
    private var guestRefresh = TestRefreshable()

    /**
     * Instantiating all network variables before testing.
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
     * Testing if player is able to take a transporter.
     */
    @Test
    fun takeTruckNormalTest() {
        var gameStateHost = rootServiceHost.currentGame.currentGameState[rootServiceHost.currentGame.currentState]
        var gameStateGuest = rootServiceGuest.currentGame.currentGameState[rootServiceGuest.currentGame.currentState]

        gameStateHost.board.transporters[1].tiles.add(CoinTile(1))
        gameStateHost.board.transporters[1].tiles.add(AnimalTile(15, animalType = Animal.DOLPHIN))
        gameStateHost.board.transporters[1].tiles.add(AnimalTile(16, animalType = Animal.DOLPHIN))

        gameStateGuest.board.transporters[1] = gameStateHost.board.transporters[1].clone()

        rootServiceHost.playerActionService.actionB(
            1, mapOf(
                gameStateHost.board.transporters[1].tiles[1] to Pair(8, 9),
                gameStateHost.board.transporters[1].tiles[2] to Pair(9, 9)
            )
        )

        rootServiceGuest.networkService.waitForState(ConnectionState.WAITING_FOR_MY_TURN)
        gameStateHost = rootServiceHost.currentGame.currentGameState[rootServiceHost.currentGame.currentState]
        gameStateGuest = rootServiceGuest.currentGame.currentGameState[rootServiceGuest.currentGame.currentState]

        assertEquals(gameStateHost.players[0].waterPark, gameStateGuest.players[0].waterPark)
        assertEquals(gameStateHost.board.transporters, gameStateGuest.board.transporters)
    }

    /**
     * Testing if player is able to take a transporter and testing for offsprings.
     */
    @Test
    fun takeTruckOffspringTest() {
        var gameStateHost = rootServiceHost.currentGame.currentGameState[rootServiceHost.currentGame.currentState]
        var gameStateGuest = rootServiceGuest.currentGame.currentGameState[rootServiceGuest.currentGame.currentState]

        gameStateHost.board.transporters[1].tiles.add(AnimalTile(17, animalType = Animal.DOLPHIN))
        gameStateHost.board.transporters[1].tiles.add(AnimalTile(12, animalType = Animal.DOLPHIN, isFemale = true))
        gameStateHost.board.transporters[1].tiles.add(AnimalTile(14, animalType = Animal.DOLPHIN, isMale = true))

        gameStateGuest.board.transporters[1] = gameStateHost.board.transporters[1].clone()

        rootServiceHost.playerActionService.actionB(
            1, mapOf(
                gameStateHost.board.transporters[1].tiles[0] to Waterpark.DEPOT_POS,
                gameStateHost.board.transporters[1].tiles[1] to Pair(9, 9),
                gameStateHost.board.transporters[1].tiles[2] to Pair(8, 9)
            )
        )
        gameStateHost = rootServiceHost.currentGame.currentGameState[rootServiceHost.currentGame.currentState]
        rootServiceHost.playerActionService.placeOffspring(
            Pair(10, 9), gameStateHost.board.offspring
                .find { it.animalType == Animal.DOLPHIN }!!
        )

        rootServiceGuest.networkService.waitForState(ConnectionState.WAITING_FOR_MY_TURN)
        gameStateHost = rootServiceHost.currentGame.currentGameState[rootServiceHost.currentGame.currentState]
        gameStateGuest = rootServiceGuest.currentGame.currentGameState[rootServiceGuest.currentGame.currentState]

        assertEquals(gameStateHost.players[0].waterPark, gameStateGuest.players[0].waterPark)
        assertEquals(gameStateHost.players[0].depot, gameStateGuest.players[0].depot)
        assertEquals(gameStateHost.board.transporters, gameStateGuest.board.transporters)
    }

    /**
     * Testing if player is able to take a transporter and testing for offsprings.
     */
    @Test
    fun takeTruckCoworkerOffspringTest() {
        var gameStateHost = rootServiceHost.currentGame.currentGameState[rootServiceHost.currentGame.currentState]
        var gameStateGuest = rootServiceGuest.currentGame.currentGameState[rootServiceGuest.currentGame.currentState]

        gameStateHost.players[0].waterPark.fieldMap[Pair(9, 8)] = AnimalTile(15, animalType = Animal.DOLPHIN)
        gameStateHost.players[0].waterPark.fieldMap[Pair(10, 8)] = AnimalTile(16, animalType = Animal.DOLPHIN)
        gameStateHost.players[0].waterPark.fieldMap[Pair(11, 8)] = AnimalTile(
            12,
            animalType = Animal.DOLPHIN,
            isFemale = true
        )
        gameStateGuest.players[0].waterPark = gameStateHost.players[0].waterPark.clone()

        gameStateHost.board.transporters[1].tiles.add(AnimalTile(14, animalType = Animal.DOLPHIN, isMale = true))

        gameStateGuest.board.transporters[1] = gameStateHost.board.transporters[1].clone()

        rootServiceHost.playerActionService.actionB(
            1, mapOf(
                gameStateHost.board.transporters[1].tiles[0] to Pair(9, 9)
            )
        )
        gameStateHost = rootServiceHost.currentGame.currentGameState[rootServiceHost.currentGame.currentState]
        rootServiceHost.playerActionService.placeOffspring(
            Pair(10, 9), gameStateHost.board.offspring
                .find { it.animalType == Animal.DOLPHIN }!!
        )
        rootServiceHost.playerActionService.placeCoworker(Waterpark.DEPOT_POS, Coworker(CoworkerTask.MANAGER))

        rootServiceGuest.networkService.waitForState(ConnectionState.WAITING_FOR_MY_TURN)
        gameStateHost = rootServiceHost.currentGame.currentGameState[rootServiceHost.currentGame.currentState]
        gameStateGuest = rootServiceGuest.currentGame.currentGameState[rootServiceGuest.currentGame.currentState]

        assertEquals(gameStateHost.players[0].waterPark, gameStateGuest.players[0].waterPark)
        assertEquals(gameStateHost.players[0].depot, gameStateGuest.players[0].depot)
        assertEquals(gameStateHost.board.transporters, gameStateGuest.board.transporters)
    }

    /**
     * Testing if player is able to take a transporter and testing for offsprings.
     */
    @Test
    fun takeTruck2CoworkerTest() {
        var gameStateHost = rootServiceHost.currentGame.currentGameState[rootServiceHost.currentGame.currentState]
        var gameStateGuest = rootServiceGuest.currentGame.currentGameState[rootServiceGuest.currentGame.currentState]

        gameStateHost.players[0].waterPark.fieldMap[Pair(9, 8)] = AnimalTile(15, animalType = Animal.DOLPHIN)
        gameStateHost.players[0].waterPark.fieldMap[Pair(10, 8)] = AnimalTile(16, animalType = Animal.DOLPHIN)
        gameStateHost.players[0].waterPark.fieldMap[Pair(11, 8)] = AnimalTile(12, animalType = Animal.DOLPHIN)
        gameStateHost.players[0].waterPark.fieldMap[Pair(11, 9)] = AnimalTile(12, animalType = Animal.DOLPHIN)

        gameStateHost.players[0].waterPark.fieldMap[Pair(10, 10)] = AnimalTile(15, animalType = Animal.HIPPO)
        gameStateHost.players[0].waterPark.fieldMap[Pair(10, 11)] = AnimalTile(16, animalType = Animal.HIPPO)
        gameStateHost.players[0].waterPark.fieldMap[Pair(11, 11)] = AnimalTile(12, animalType = Animal.HIPPO)
        gameStateHost.players[0].waterPark.fieldMap[Pair(12, 11)] = AnimalTile(12, animalType = Animal.HIPPO)
        gameStateGuest.players[0].waterPark = gameStateHost.players[0].waterPark.clone()

        gameStateHost.board.transporters[1].tiles.add(AnimalTile(14, animalType = Animal.DOLPHIN, isMale = true))
        gameStateHost.board.transporters[1].tiles.add(AnimalTile(14, animalType = Animal.HIPPO, isMale = true))

        gameStateGuest.board.transporters[1] = gameStateHost.board.transporters[1].clone()

        rootServiceHost.playerActionService.actionB(
            1, mapOf(
                gameStateHost.board.transporters[1].tiles[0] to Pair(9, 9),
                gameStateHost.board.transporters[1].tiles[1] to Pair(12, 10)
            )
        )
        rootServiceHost.playerActionService.placeCoworker(Waterpark.CASH_POS_1, Coworker(CoworkerTask.CASHIER))
        rootServiceHost.playerActionService.placeCoworker(Waterpark.ANIM_POS_1, Coworker(CoworkerTask.ANIMAL_KEEPER))

        rootServiceGuest.networkService.waitForState(ConnectionState.WAITING_FOR_MY_TURN)
        gameStateHost = rootServiceHost.currentGame.currentGameState[rootServiceHost.currentGame.currentState]
        gameStateGuest = rootServiceGuest.currentGame.currentGameState[rootServiceGuest.currentGame.currentState]

        assertEquals(gameStateHost.players[0].waterPark, gameStateGuest.players[0].waterPark)
        assertEquals(gameStateHost.players[0].depot, gameStateGuest.players[0].depot)
        assertEquals(gameStateHost.board.transporters, gameStateGuest.board.transporters)
    }
}