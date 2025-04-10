package service

import entity.*
import kotlin.IllegalStateException
import kotlin.test.*

/**
 * Tests for the actionCPurchase-method, in which a player buys and places the uppermost tile
 * from the depot of another player in their own Waterpark for 2 coins. One coin goes to the
 * other player.
 */
class ActionCPurchaseTest {

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
        val seller = currentState.players[1]

        // preexisting tile in the waterpark
        seller.waterPark.fieldMap[Pair(9, 9)] = AnimalTile(0, animalType = Animal.DOLPHIN)
        seller.waterPark.fieldMap[Pair(11, 11)] = Coworker(CoworkerTask.TRAINER)
        seller.waterPark.fieldMap[Waterpark.CASH_POS_1] = Coworker(CoworkerTask.CASHIER)

        rootService = RootService(game)
    }

    /**
     * Test actionCPurchase on the prepared [AquarettoGameState]. Since isPlaceable() and placeTile()
     * have already been indirectly tested in [ActionCMoveTest], I'm going to omit additional tests for those
     * functions.
     */
    @Test
    fun testActionCPurchase(){
        // get game object and check for the game state
        val game = rootService!!.currentGame
        assertEquals(1, game.currentGameState.size)
        assertEquals(0, game.currentState)

        var currentState = game.currentGameState[game.currentState]
        var currentPlayer = currentState.players[currentState.currentPlayer]
        var seller = currentState.players[currentState.currentPlayer]
        currentPlayer.numCoins = 4

        // player can't sell tile to himself
        assertFailsWith<IllegalStateException> {
            rootService!!.playerActionService.actionCPurchase(seller, Pair(9, 9))
        }

        seller = currentState.players[1]
        currentPlayer.numCoins = 0

        // not enough money
        assertFailsWith<IllegalStateException> {
            rootService!!.playerActionService.actionCPurchase(seller, Pair(9, 9))
        }
        currentPlayer.numCoins = 1
        assertFailsWith<IllegalStateException> {
            rootService!!.playerActionService.actionCPurchase(seller, Pair(9, 9))
        }
        currentPlayer.numCoins = 2

        // illegal coordinates
        assertFailsWith<IllegalStateException> {
            rootService!!.playerActionService.actionCPurchase(seller, Pair(10000, 10000))
        }

        assertEquals(2, currentPlayer.numCoins)
        assertEquals(1, seller.numCoins)

        assertEquals(1, seller.depot.first().id)
        assertEquals(Animal.DOLPHIN, (seller.depot.first() as AnimalTile).animalType)

        rootService!!.playerActionService.actionCPurchase(seller, Pair(9, 9))

        // uppermost tile removed from seller...
        assertEquals(2, seller.depot.first().id)
        assertEquals(Animal.PENGUIN, (seller.depot.first() as AnimalTile).animalType)

        // ...and then placed on currentPlayer's waterpark
        assertEquals(1, (currentPlayer.waterPark.fieldMap[Pair(9, 9)] as AnimalTile).id)
        assertEquals(Animal.DOLPHIN, (currentPlayer.waterPark.fieldMap[Pair(9, 9)] as AnimalTile).animalType)

        // new coin amounts
        currentState = game.currentGameState[game.currentState]
        currentPlayer = currentState.players[currentState.currentPlayer-1]
        assertEquals(0, currentPlayer.numCoins)
        assertEquals(2, currentState.players[1].numCoins)
    }
}