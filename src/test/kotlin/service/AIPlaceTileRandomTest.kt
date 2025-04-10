package service

import entity.*
import kotlin.test.*

/**
 * Tests for the AI's ability to randomly place a tile in the game.
 */
class AIPlaceTileRandomTest {

    private var rootService: RootService? = null

    /**
     * Prepare an [AquarettoGameState] for testing
     */
    @BeforeTest
    fun setupTest() {
        val game = AquarettoGame()
        game.currentGameState.add(
            AquarettoGameState(
                mutableListOf(
                    Player("Marcel", 0, PlayerType.LOCAL_HUMAN),
                    Player(
                        "R2D2", 2, PlayerType.LOCAL_AI,
                        mutableListOf(
                            AnimalTile(1, animalType = Animal.DOLPHIN),
                            AnimalTile(2, animalType = Animal.PENGUIN)
                        )
                    ),
                    Player("C3PO", 0, PlayerType.LOCAL_RANDOM_AI)
                )
            )
        )

        // get current game state and set current player to 1
        val currentState = game.currentGameState[game.currentState]
        currentState.currentPlayer = 1
        // for 2 moves
        currentState.players[currentState.currentPlayer].numCoins = 6
        // preexisting tiles in the waterpark
        currentState.players[currentState.currentPlayer].waterPark.fieldMap[Pair(9, 9)] =
            AnimalTile(0, animalType = Animal.DOLPHIN)
        currentState.players[currentState.currentPlayer].waterPark.fieldMap[Pair(12, 11)] =
            AnimalTile(3, animalType = Animal.PENGUIN)
        currentState.players[currentState.currentPlayer].waterPark.fieldMap[Pair(9, 11)] =
            AnimalTile(4, animalType = Animal.ORCA)
        currentState.players[currentState.currentPlayer].waterPark.fieldMap[Pair(11, 11)] =
            Coworker(CoworkerTask.TRAINER)
        currentState.players[currentState.currentPlayer].waterPark.fieldMap[Waterpark.CASH_POS_1] =
            Coworker(CoworkerTask.CASHIER)

        rootService = RootService(game)
    }

    /**
     * Testing if the AI is able to randomly place a tile on the board.
     */
    @Test
    fun testAIPlaceTileRandom() {
        // get game object and check for the game state
        val game = rootService!!.currentGame
        assertEquals(1, game.currentGameState.size)
        assertEquals(0, game.currentState)

        val tile = AnimalTile(1, animalType = Animal.DOLPHIN)

        val randomPos = rootService!!.AIActionService.placeTileRandom(tile)
        // check if the random position is within the bounds of the waterpark
        assertTrue { randomPos.first in 0..19 }
        assertTrue { randomPos.second in 0..19 }
        println("Random Position: $randomPos")
    }
}
