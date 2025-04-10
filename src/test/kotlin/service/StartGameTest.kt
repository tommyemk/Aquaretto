package service

import entity.*
import kotlin.test.*

/**
 * This class tests the startGame method from the AquarettoGameService
 *
 * @property rootService hold all game elements
 */
class StartGameTest {

    private var rootService: RootService? = null

    /**
     * This method initialises all important game components for the test.
     */
    @BeforeTest
    fun beforeTest() {
        val game = AquarettoGame()
        game.currentGameState.add(AquarettoGameState())
        rootService = RootService(game)
    }

    /**
     * This class tests the startGame method with two player.
     */
    @Test
    fun testStartGameWithTwoPlayer() {
        val game = rootService!!.currentGame
        var currentState = game.currentGameState[game.currentState]

        assertEquals(1, game.currentGameState.size)
        assertEquals(0, game.currentState)

        val players = listOf(Pair("Edi", PlayerType.LOCAL_HUMAN), Pair("Tommy", PlayerType.LOCAL_HUMAN))
        assertFalse { game.isOnlineGame }
        assertTrue { game.speed == 0 }

        // Before the start all Piles and the transporter list should be empty
        assertTrue { currentState.board.mainPile.isEmpty() }
        assertTrue { currentState.board.finalPile.isEmpty() }
        assertTrue { currentState.board.offspring.isEmpty() }
        assertTrue { currentState.board.transporters.isEmpty() }

        rootService!!.aquarettoGameService.startGame(players, isOnline = false, shuffle = true, 1)

        currentState = game.currentGameState[game.currentState]

        assertEquals(2, currentState.players.size)

        assertTrue { currentState.board.mainPile.isNotEmpty() }
        assertTrue { currentState.board.finalPile.isNotEmpty() }
        assertTrue { currentState.board.offspring.isNotEmpty() }
        assertTrue { currentState.board.transporters.isNotEmpty() }

        assertEquals(3, currentState.board.transporters.size)
        assertTrue {
            !currentState.board.mainPile.any {
                it is AnimalTile
                        && it.animalType == Animal.POLAR_BEAR
                        && it.animalType == Animal.CROCODILE
                        && it.animalType == Animal.PENGUIN
            }
        }
        assertTrue {
            !currentState.board.finalPile.any {
                it is AnimalTile
                        && it.animalType == Animal.POLAR_BEAR
                        && it.animalType == Animal.CROCODILE
                        && it.animalType == Animal.PENGUIN
            }
        }
        assertTrue { currentState.board.finalPile.size == 15 }
        assertFalse { game.isOnlineGame }
        assertTrue { game.speed == 1 }
    }

    /**
     * This class tests the startGame method with three player.
     */
    @Test
    fun testStartGameWithThreePlayer() {
        val game = rootService!!.currentGame
        var currentState = game.currentGameState[game.currentState]

        assertEquals(1, game.currentGameState.size)
        assertEquals(0, game.currentState)

        val players = listOf(
            Pair("Edi", PlayerType.LOCAL_HUMAN),
            Pair("Tommy", PlayerType.LOCAL_HUMAN),
            Pair("Max", PlayerType.LOCAL_HUMAN)
        )
        assertFalse { game.isOnlineGame }

        // Before the start all Piles and the transporter list should be empty
        assertTrue { currentState.board.mainPile.isEmpty() }
        assertTrue { currentState.board.finalPile.isEmpty() }
        assertTrue { currentState.board.offspring.isEmpty() }
        assertTrue { currentState.board.transporters.isEmpty() }

        rootService!!.aquarettoGameService.startGame(players, isOnline = false, shuffle = true, 1)

        currentState = game.currentGameState[game.currentState]

        assertEquals(3, currentState.players.size)

        assertTrue { currentState.board.mainPile.isNotEmpty() }
        assertTrue { currentState.board.finalPile.isNotEmpty() }
        assertTrue { currentState.board.offspring.isNotEmpty() }
        assertTrue { currentState.board.transporters.isNotEmpty() }

        assertEquals(3, currentState.board.transporters.size)
        assertTrue {
            !currentState.board.mainPile.any {
                it is AnimalTile
                        && it.animalType == Animal.SEA_TURTLE
                        && it.animalType == Animal.PENGUIN
            }
        }
        assertTrue {
            !currentState.board.finalPile.any {
                it is AnimalTile
                        && it.animalType == Animal.SEA_TURTLE
                        && it.animalType == Animal.PENGUIN
            }
        }
        assertTrue { currentState.board.finalPile.size == 15 }
        assertFalse { game.isOnlineGame }

    }

    /**
     * This class tests the startGame method with four player.
     */
    @Test
    fun testStartGameWithFourPlayer() {
        val game = rootService!!.currentGame
        var currentState = game.currentGameState[game.currentState]

        assertEquals(1, game.currentGameState.size)
        assertEquals(0, game.currentState)

        val players = listOf(
            Pair("Edi", PlayerType.LOCAL_HUMAN),
            Pair("Tommy", PlayerType.LOCAL_HUMAN),
            Pair("Max", PlayerType.LOCAL_HUMAN),
            Pair("Lee", PlayerType.LOCAL_HUMAN)
        )
        assertFalse { game.isOnlineGame }
        assertTrue { game.speed == 0 }

        // Before the start all Piles and the transporter list should be empty
        assertTrue { currentState.board.mainPile.isEmpty() }
        assertTrue { currentState.board.finalPile.isEmpty() }
        assertTrue { currentState.board.offspring.isEmpty() }
        assertTrue { currentState.board.transporters.isEmpty() }

        rootService!!.aquarettoGameService.startGame(players, isOnline = false, shuffle = true, 1)

        currentState = game.currentGameState[game.currentState]

        assertEquals(4, currentState.players.size)

        assertTrue { currentState.board.mainPile.isNotEmpty() }
        assertTrue { currentState.board.finalPile.isNotEmpty() }
        assertTrue { currentState.board.offspring.isNotEmpty() }
        assertTrue { currentState.board.transporters.isNotEmpty() }

        assertEquals(4, currentState.board.transporters.size)
        assertTrue {
            !currentState.board.mainPile.any {
                it is AnimalTile && it.animalType == Animal.HIPPO
            }
        }
        assertTrue {
            !currentState.board.finalPile.any {
                it is AnimalTile && it.animalType == Animal.HIPPO
            }
        }
        assertTrue { currentState.board.finalPile.size == 15 }
        assertFalse { game.isOnlineGame }
        assertTrue { game.speed == 1 }
    }

    /**
     * This class tests the startGame method with five player.
     */
    @Test
    fun testStartGameWithFivePlayer() {
        val game = rootService!!.currentGame
        var currentState = game.currentGameState[game.currentState]

        assertEquals(1, game.currentGameState.size)
        assertEquals(0, game.currentState)

        val players = listOf(
            Pair("Edi", PlayerType.LOCAL_HUMAN),
            Pair("Tommy", PlayerType.LOCAL_HUMAN),
            Pair("Max", PlayerType.LOCAL_HUMAN),
            Pair("Lee", PlayerType.LOCAL_HUMAN),
            Pair("Marvin", PlayerType.LOCAL_HUMAN)
        )
        assertFalse { game.isOnlineGame }
        assertTrue { game.speed == 0 }

        // Before the start all Piles and the transporter list should be empty
        assertTrue { currentState.board.mainPile.isEmpty() }
        assertTrue { currentState.board.finalPile.isEmpty() }
        assertTrue { currentState.board.offspring.isEmpty() }
        assertTrue { currentState.board.transporters.isEmpty() }

        rootService!!.aquarettoGameService.startGame(players, isOnline = false, shuffle = true, 1)

        currentState = game.currentGameState[game.currentState]

        assertEquals(5, currentState.players.size)

        assertTrue { currentState.board.mainPile.isNotEmpty() }
        assertTrue { currentState.board.finalPile.isNotEmpty() }
        assertTrue { currentState.board.offspring.isNotEmpty() }
        assertTrue { currentState.board.transporters.isNotEmpty() }

        assertEquals(5, currentState.board.transporters.size)
        assertTrue { currentState.board.finalPile.size == 15 }
        assertFalse { game.isOnlineGame }
        assertTrue { game.speed == 1 }
    }
}