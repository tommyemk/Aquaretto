package service

import entity.*
import kotlin.IllegalStateException
import kotlin.test.*

/**
 * Tests for the actionCDiscardTile-method, in which a player discards the uppermost tile on their
 * depot for 2 coins.
 */
class ActionCDiscardTile {

    private var rootService: RootService? = null

    /**
     * Prepare an [AquarettoGameState] for testing
     */
    @BeforeTest
    fun setupTest(){
        val game = AquarettoGame()
        game.currentGameState.add(
            AquarettoGameState(
                mutableListOf(
                    Player("Marcel", 0, PlayerType.LOCAL_HUMAN,
                        mutableListOf(
                            AnimalTile(3, animalType = Animal.HIPPO)
                        )
                    ),
                    Player("R2D2", 2, PlayerType.LOCAL_AI,
                        mutableListOf(
                            AnimalTile(1, animalType = Animal.DOLPHIN),
                            AnimalTile(2, animalType = Animal.PENGUIN)
                        )
                    ),
                    Player("C3PO", 0, PlayerType.LOCAL_RANDOM_AI)
                )
            )
        )

        val currentState = game.currentGameState[game.currentState]
        val player = currentState.players[1]

        // preexisting tile in the waterpark
        player.waterPark.fieldMap[Pair(9, 9)] = AnimalTile(0, animalType = Animal.DOLPHIN)
        player.waterPark.fieldMap[Pair(11, 11)] = Coworker(CoworkerTask.TRAINER)
        player.waterPark.fieldMap[Waterpark.CASH_POS_1] = Coworker(CoworkerTask.CASHIER)

        rootService = RootService(game)
    }

    /**
     * Test actionCDiscardPile() (with fails)
     */
    @Test
    fun testActionCDiscardPile(){
        // get game object and check for the game state
        val game = rootService!!.currentGame
        assertEquals(1, game.currentGameState.size)
        assertEquals(0, game.currentState)

        var currentState = game.currentGameState[game.currentState]
        var currentPlayer = currentState.players[currentState.currentPlayer]

        // not enough money
        currentPlayer.numCoins = 0
        assertFailsWith<IllegalStateException> {
            rootService!!.playerActionService.actionCDiscardTile()
        }
        currentPlayer.numCoins = 1
        assertFailsWith<IllegalStateException> {
            rootService!!.playerActionService.actionCDiscardTile()
        }

        currentPlayer.numCoins = 2

        assertEquals(2, currentPlayer.numCoins)
        assertEquals(3, currentPlayer.depot.first().id)
        assertEquals(Animal.HIPPO, (currentPlayer.depot.first() as AnimalTile).animalType)

        rootService!!.playerActionService.actionCDiscardTile()

        currentState = game.currentGameState[game.currentState]
        currentPlayer = currentState.players[currentState.currentPlayer-1]

        assertEquals(0, currentPlayer.numCoins)
        assertTrue{ currentPlayer.depot.isEmpty() }
    }
}