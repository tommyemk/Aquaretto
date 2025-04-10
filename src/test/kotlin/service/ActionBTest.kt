package service

import entity.*
import kotlin.test.*

/**
 * This class represents the test for the method actionB.
 *
 * @property rootService hold all game elements
 */
class ActionBTest {

    private var rootService: RootService? = null

    /**
     * This method is getting called before actionB is getting tested.
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
    }

    /**
     * This method tests ActionB with invalid and valid input.
     */
    @Test
    fun testActionB() {
        val game = rootService!!.currentGame
        var currentState = game.currentGameState[game.currentState]
        var currentPlayer = currentState.players[currentState.currentPlayer]
        assertEquals(1, game.currentGameState.size)
        assertEquals(0, game.currentState)

        val animal1 = AnimalTile(0, animalType = Animal.HIPPO)
        val animal2 = AnimalTile(1, animalType = Animal.CROCODILE)
        val coinTile = CoinTile(3)
        val coords = mapOf<Tile, Pair<Int, Int>>(animal1 to Waterpark.DEPOT_POS, animal2 to Pair(10, 10))

        // Check with wrong transporter index
        assertFailsWith<IllegalStateException> {
            rootService!!.playerActionService.actionB(4, coords)
        }

        // Check with right transporter index but the transporter at the transporterIndex is empty
        assertFailsWith<IllegalStateException> {
            rootService!!.playerActionService.actionB(0, coords)
        }

        assertFalse { currentState.board.transporters[0].taken }
        assertFalse { currentPlayer.turnEnded }

        // Adds AnimalTiles to the first transporter
        currentState.board.transporters[0].tiles.add(animal1)
        currentState.board.transporters[0].tiles.add(animal2)
        // Adds CoinTile to the first transporter
        currentState.board.transporters[0].tiles.add(coinTile)

        // Player has 1 coin at the start
        assertEquals(1, currentPlayer.numCoins)

        assertTrue { currentPlayer.depot.isEmpty() }
        assertFalse { currentPlayer.waterPark.fieldMap.contains(Pair(10, 10)) }

        rootService!!.playerActionService.actionB(0, coords)

        currentState = game.currentGameState[game.currentState]
        currentPlayer = currentState.players[currentState.currentPlayer - 1]

        assertTrue { currentPlayer.waterPark.fieldMap.contains(Pair(10, 10)) }
        assertTrue { currentPlayer.depot.isNotEmpty() }

        // Player receives 1 coin
        assertEquals(2, currentPlayer.numCoins)

        assertTrue { currentState.board.transporters[0].taken }
        assertTrue { currentPlayer.turnEnded }
    }
}