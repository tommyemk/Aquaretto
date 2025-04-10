package service

import entity.*
import kotlin.test.*

/**
 * This class represents the test for the method actionA.
 *
 * @property rootService hold all game elements
 */
class ActionATest {

    private var rootService: RootService? = null

    /**
     * This method is getting called before the actual tests starts.
     */
    @BeforeTest
    fun beforeTest() {
        val game = AquarettoGame()
        game.currentGameState.add(
            AquarettoGameState(
                mutableListOf(
                    Player(
                        "Tommy", 0, PlayerType.LOCAL_HUMAN,
                    ),
                    Player(
                        "Edi", 2, PlayerType.LOCAL_HUMAN,

                        ),
                    Player(
                        "Max", 2, PlayerType.LOCAL_HUMAN,
                    )
                )
            )
        )
        val currentState = game.currentGameState[game.currentState]
        repeat(3) {
            currentState.board.transporters.add(Transporter())
        }
        rootService = RootService(game)
        currentState.board.mainPile.add(AnimalTile(0, animalType = Animal.HIPPO))
        currentState.board.finalPile.add(AnimalTile(0, animalType = Animal.HIPPO))
    }

    /**
     * This method tests the ActionA method from the
     * playerActionService with valid parameters and by going through all conditions.
     */
    @Test
    fun testActionA() {
        val game = rootService!!.currentGame
        var currentState = game.currentGameState[game.currentState]
        assertEquals(1, game.currentGameState.size)
        assertEquals(0, game.currentState)

        // Check with wrong transporter index
        assertFailsWith<IllegalStateException> {
            rootService!!.playerActionService.actionA(4)
        }

        val validTransporterIndex = 1

        // mainPile is not empty by standard
        val beforeMainPileLength = currentState.board.mainPile.size
        // transporterLength is getting increased by 1
        val beforeTransporterLength = currentState.board.transporters[validTransporterIndex].tiles.size

        assertTrue { currentState.board.mainPile.isNotEmpty() }

        rootService!!.playerActionService.actionA(validTransporterIndex)

        currentState = game.currentGameState[game.currentState]

        assertEquals(beforeMainPileLength - 1, currentState.board.mainPile.size)
        assertEquals(beforeTransporterLength + 1, currentState.board.transporters[validTransporterIndex].tiles.size)

        // game.currentGameState[rootService.currentGame].board.mainPile.clear()
        currentState.board.mainPile.clear()
        assertTrue { currentState.board.mainPile.isEmpty() }

        // finalPile is not empty by standard
        val beforeFinalPileLength = currentState.board.finalPile.size
        // transporterLength is getting increased by 1
        val beforeTransporterLengthNew = currentState.board.transporters[validTransporterIndex].tiles.size
        rootService!!.playerActionService.actionA(validTransporterIndex)

        currentState = game.currentGameState[game.currentState]

        assertEquals(beforeFinalPileLength - 1, currentState.board.finalPile.size)
        assertEquals(beforeTransporterLengthNew + 1, currentState.board.transporters[validTransporterIndex].tiles.size)
    }
}