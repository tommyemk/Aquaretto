package service

import entity.*
import kotlin.IllegalStateException
import kotlin.test.*

/**
 * Tests for the actionCExtendWaterPark-method, in which a player extends their waterpark by placing
 * an extension for either one or two coins.
 */
class ActionCExtendWaterParkTest {

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
                    Player(
                        "Marcel", 0, PlayerType.LOCAL_HUMAN,
                        mutableListOf(
                            AnimalTile(3, animalType = Animal.HIPPO)
                        )
                    ),
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

        val currentState = game.currentGameState[game.currentState]
        val player = currentState.players[1]

        // preexisting tile in the waterpark
        player.waterPark.fieldMap[Pair(9, 9)] = AnimalTile(0, animalType = Animal.DOLPHIN)
        player.waterPark.fieldMap[Pair(11, 11)] = Coworker(CoworkerTask.TRAINER)
        player.waterPark.fieldMap[Waterpark.CASH_POS_1] = Coworker(CoworkerTask.CASHIER)

        rootService = RootService(game)
    }

    /**
     * Test actionCExtendWaterPark() (with fails) for a big Extension
     */
    @Test
    fun testActionCExtendWaterParkBig(){
        // get game object and check for the game state
        val game = rootService!!.currentGame
        assertEquals(1, game.currentGameState.size)
        assertEquals(0, game.currentState)

        var currentState = game.currentGameState[game.currentState]
        currentState.currentPlayer = 1
        var currentPlayer = currentState.players[currentState.currentPlayer]

        assertEquals(3, currentPlayer.waterPark.allowedAnimalType)

        val bigExtension = currentPlayer.waterParkExtensionList[2]
        bigExtension.x = 7
        bigExtension.y = 7

        assertFalse{ bigExtension.isSmall }
        // not enough money
        assertFails {
            rootService!!.playerActionService.actionCExtendWaterPark(bigExtension)
        }
        currentPlayer.numCoins = 2

        //trying to place extension over already placeable waterpark tiles
        bigExtension.x = 8
        bigExtension.y = 7
        assertFails {
            rootService!!.playerActionService.actionCExtendWaterPark(bigExtension)
        }

        //trying to place extension to far away from waterpark
        bigExtension.x = 7
        bigExtension.y = 6
        assertFails{
            rootService!!.playerActionService.actionCExtendWaterPark(bigExtension)
        }

        assertEquals(2, currentPlayer.numCoins)

        // no fail
        bigExtension.x = 7
        bigExtension.y = 7
        rootService!!.playerActionService.actionCExtendWaterPark(bigExtension)

        currentState = game.currentGameState[game.currentState]
        currentPlayer = currentState.players[currentState.currentPlayer-1]

        assertEquals(0, currentPlayer.numCoins)
        assertTrue {
            currentPlayer.waterPark.allowedExtensionList.containsAll(
                listOf(Pair(7, 7), Pair(8, 7), Pair(7, 8), Pair(8, 8))
            )
        }
        assertEquals(0, currentPlayer.numCoins)
        assertEquals(4, currentPlayer.waterPark.allowedAnimalType)
    }

    /**
     * Test actionCExtendWaterPark() (with fails) for a small Extension
     */
    @Test
    fun testActionCExtendWaterParkSmall(){
        // get game object and check for the game state
        val game = rootService!!.currentGame
        assertEquals(1, game.currentGameState.size)
        assertEquals(0, game.currentState)

        var currentState = game.currentGameState[game.currentState]
        currentState.currentPlayer = 1
        var currentPlayer = currentState.players[currentState.currentPlayer]

        assertEquals(3, currentPlayer.waterPark.allowedAnimalType)

        val smallExtension = currentPlayer.waterParkExtensionList[0]
        smallExtension.x = 7
        smallExtension.y = 7

        assertTrue{ smallExtension.isSmall }
        currentPlayer.numCoins = 0
        // not enough money
        assertFails {
            rootService!!.playerActionService.actionCExtendWaterPark(smallExtension)
        }
        currentPlayer.numCoins = 1

        //trying to place extension over already placeable waterpark tiles
        smallExtension.x = 8
        smallExtension.y = 7
        assertFails {
            rootService!!.playerActionService.actionCExtendWaterPark(smallExtension)
        }

        //trying to place extension to far away from waterpark
        smallExtension.rotation = 1
        smallExtension.x = 7
        smallExtension.y = 7
        assertFails {
            rootService!!.playerActionService.actionCExtendWaterPark(smallExtension)
        }
        smallExtension.x = 7
        smallExtension.y = 6
        assertFails {
            rootService!!.playerActionService.actionCExtendWaterPark(smallExtension)
        }

        // illegal rotations
        smallExtension.rotation = 4
        assertFails {
            rootService!!.playerActionService.actionCExtendWaterPark(smallExtension)
        }
        smallExtension.rotation = -1
        assertFails {
            rootService!!.playerActionService.actionCExtendWaterPark(smallExtension)
        }

        assertEquals(1, currentPlayer.numCoins)

        // no fail (x = 8, y = 7, rotation = 1)
        smallExtension.rotation = 1
        smallExtension.x = 8
        smallExtension.y = 7
        rootService!!.playerActionService.actionCExtendWaterPark(smallExtension)

        currentState = game.currentGameState[game.currentState]
        currentPlayer = currentState.players[currentState.currentPlayer-1]

        assertEquals(0, currentPlayer.numCoins)
        assertTrue {
            currentPlayer.waterPark.allowedExtensionList.containsAll(
                listOf(Pair(8, 7), Pair(9, 7), Pair(8, 8))
            )
        }
        assertEquals(0, currentPlayer.numCoins)
    }
}