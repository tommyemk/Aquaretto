package service

import entity.*
import kotlin.IllegalStateException
import kotlin.test.*

/**
 * Tests for the actionCMove-method, in which a player can either remove a tile from the depot and
 * place it in the waterpark or they move a Coworker to a different position.
 */
class ActionCMoveTest {

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
                    Player("Marcel", 0, PlayerType.LOCAL_HUMAN),
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

        // get current game state and set current player to 1
        val currentState = game.currentGameState[game.currentState]
        currentState.currentPlayer = 1
        // for 2 moves
        currentState.players[currentState.currentPlayer].numCoins = 2
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
     * Test actionCMove() on an "actual" game by trying to move an [AnimalTile] and
     * test specifically if the function fails in certain situations
     */
    @Test
    fun testActionCMoveAnimalTileFails(){
        // get game object and check for the game state
        val game = rootService!!.currentGame
        assertEquals(1, game.currentGameState.size)
        assertEquals(0, game.currentState)

        val currentState = game.currentGameState[game.currentState]

        assertEquals(2, currentState.players[currentState.currentPlayer].numCoins)
        // check player depot and get the uppermost tile (first(???)) to move
        assertEquals(2, currentState.players[currentState.currentPlayer].depot.size)
        val tile = (currentState.players[currentState.currentPlayer].depot.first() as AnimalTile)
        assertEquals(Animal.DOLPHIN, tile.animalType)
        assertEquals(1, tile.id)

        // should fail cause (1000, 1000) is not valid coordinate
        assertFailsWith<IllegalStateException> {
            rootService!!.playerActionService.actionCMove(tile, Pair(1000, 1000))
        }
        // should fail cause there is no extension there
        assertFailsWith<IllegalStateException> {
            rootService!!.playerActionService.actionCMove(tile, Pair(0, 0))
        }

        // should fail cause Tiles can't be placed in coworker spots
        assertFailsWith<IllegalStateException>{
            rootService!!.playerActionService.actionCMove(tile, Waterpark.CASH_POS_1)
        }
        assertFailsWith<IllegalStateException>{
            rootService!!.playerActionService.actionCMove(tile, Waterpark.CASH_POS_2)
        }
        assertFailsWith<IllegalStateException>{
            rootService!!.playerActionService.actionCMove(tile, Waterpark.ANIM_POS_1)
        }
        assertFailsWith<IllegalStateException>{
            rootService!!.playerActionService.actionCMove(tile, Waterpark.ANIM_POS_2)
        }
        assertFailsWith<IllegalStateException>{
            rootService!!.playerActionService.actionCMove(tile, Waterpark.DEPOT_POS)
        }

        // should fail cause no extension have been placed to make (8, 8) a legal coordinate
        assertFailsWith<IllegalStateException> {
            rootService!!.playerActionService.actionCMove(tile, Pair(8, 8))
        }

        // Fails cause dolphin-tile already exists on board (9, 9), so the next dolphin tile must be
        // placed next to it (diagonal doesn't count)
        assertFailsWith<IllegalStateException> {
            rootService!!.playerActionService.actionCMove(tile, Pair(10, 10))
        }

        // (9, 9) is already occupied
        assertFailsWith<IllegalStateException> {
            rootService!!.playerActionService.actionCMove(tile, Pair(9, 9))
        }

        // already 3 pre-existing animal types in waterpark, 4th not allowed
        assertFailsWith<IllegalStateException> {
            rootService!!.playerActionService.actionCMove(AnimalTile(5, animalType = Animal.HIPPO), Pair(11, 8))
        }

        currentState.players[currentState.currentPlayer].numCoins = 0
        // no money
        assertFailsWith<IllegalStateException> {
            rootService!!.playerActionService.actionCMove(tile, Pair(10, 9))
        }
    }

    /**
     * Test actionCMove() on an "actual" game by trying to move an [AnimalTile] and see
     * if it works correctly, e.g. the new state is correct
     */
    @Test
    fun testActionCMoveAnimalTiles(){
        // get game object and check for the game state
        val game = rootService!!.currentGame
        assertEquals(1, game.currentGameState.size)
        assertEquals(0, game.currentState)

        var currentState = game.currentGameState[game.currentState]
        currentState.players[currentState.currentPlayer]

        assertEquals(2, currentState.players[currentState.currentPlayer].numCoins)
        // check player depot and get the uppermost tile (first(???)) to move
        assertEquals(2, currentState.players[currentState.currentPlayer].depot.size)
        var tile = (currentState.players[currentState.currentPlayer].depot.first() as AnimalTile)
        assertEquals(Animal.DOLPHIN, tile.animalType)
        assertEquals(1, tile.id)


        // valid coordinate
        rootService!!.playerActionService.actionCMove(tile, Pair(8, 9))

        // nextPlayer sets currentPlayer, needs to be reset
        currentState.currentPlayer = 1
        //depot has now 1 tile
        assertEquals( 1, currentState.players[currentState.currentPlayer].depot.size)

        // tile has been placed
        assertEquals(Animal.DOLPHIN,
            (currentState.players[currentState.currentPlayer].waterPark.fieldMap[Pair(8, 9)] as AnimalTile).animalType)
        assertEquals(1,
            (currentState.players[currentState.currentPlayer].waterPark.fieldMap[Pair(8, 9)] as AnimalTile).id)

        currentState = game.currentGameState[game.currentState]
        currentState.currentPlayer--

        // 1 coin remaining
        assertEquals(1, currentState.players[currentState.currentPlayer].numCoins)
        tile = (currentState.players[currentState.currentPlayer].depot.first() as AnimalTile)
        assertEquals(Animal.PENGUIN, tile.animalType)
        assertEquals(2, tile.id)

        // can't place penguin next to dolphin
        assertFailsWith<IllegalStateException> {
            rootService!!.playerActionService.actionCMove(tile, Pair(10, 9))
        }

        // legal position
        rootService!!.playerActionService.actionCMove(tile, Pair(12, 10))
        // nextPlayer sets currentPlayer, needs to be reset
        currentState.currentPlayer = 1

        //depot now is empty
        assertTrue { currentState.players[currentState.currentPlayer].depot.isEmpty() }

        // tile has been placed
        assertEquals(Animal.PENGUIN,
            (currentState.players[currentState.currentPlayer].waterPark.fieldMap[Pair(12, 10)] as AnimalTile).animalType)
        assertEquals(2,
            (currentState.players[currentState.currentPlayer].waterPark.fieldMap[Pair(12, 10)] as AnimalTile).id)

        // 0 coins remaining
        currentState = game.currentGameState[game.currentState]
        assertEquals(0, currentState.players[currentState.currentPlayer-1].numCoins)
    }

    /**
     * Test moving a coworker
     */
    @Test
    fun testActionCMoveCoworker(){
        // get game object and check for the game state
        val game = rootService!!.currentGame
        assertEquals(1, game.currentGameState.size)
        assertEquals(0, game.currentState)

        var currentState = game.currentGameState[game.currentState]

        assertEquals(2, currentState.players[currentState.currentPlayer].numCoins)

        // check if coworker is there with correct job
        assertTrue {
            currentState.players[currentState.currentPlayer].waterPark.fieldMap[Pair(11, 11)] is Coworker
        }
        assertEquals(CoworkerTask.TRAINER,
            (currentState.players[currentState.currentPlayer].waterPark.fieldMap[Pair(11, 11)] as Coworker).
            coworkerTask)

        var coworker = currentState.players[currentState.currentPlayer].waterPark.fieldMap[Pair(11, 11)]

        // player shouldn't be able to move the coworker to the same coordinate it's already on
        assertFailsWith<IllegalStateException> {
            rootService!!.playerActionService.actionCMove(coworker!!, Pair(11, 11))
        }

        // player shouldn't be able to move the coworker to an already occupied coordinate
        assertFailsWith<IllegalStateException> {
            rootService!!.playerActionService.actionCMove(coworker!!, Waterpark.CASH_POS_1)
        }
        assertFailsWith<IllegalStateException> {
            rootService!!.playerActionService.actionCMove(coworker!!, Pair(9, 9))
        }


        // should NOT fail cause cash_pos_2 is empty
        rootService!!.playerActionService.actionCMove(coworker!!, Waterpark.CASH_POS_2)
        // nextPlayer sets currentPlayer, needs to be reset
        currentState.currentPlayer = 1

        // coworker moved
        assertEquals(coworker,
            currentState.players[currentState.currentPlayer].waterPark.fieldMap[Waterpark.CASH_POS_2])
        assertEquals(null,
            currentState.players[currentState.currentPlayer].waterPark.fieldMap[Pair(11, 11)])

        // coworker changed profession
        assertEquals(CoworkerTask.CASHIER, (coworker as Coworker).coworkerTask)
        // money reduced
        currentState = game.currentGameState[game.currentState]
        assertEquals(1, currentState.players[currentState.currentPlayer].numCoins)

        coworker = currentState.players[currentState.currentPlayer-1].waterPark.fieldMap[Waterpark.CASH_POS_1]
        rootService!!.playerActionService.actionCMove(coworker!!, Pair(11, 12))
        // nextPlayer sets currentPlayer, needs to be reset
        //currentState.currentPlayer = 1

        // coworker moved
        assertEquals(coworker,
            currentState.players[currentState.currentPlayer].waterPark.fieldMap[Pair(11, 12)])
        assertEquals(null,
            currentState.players[currentState.currentPlayer].waterPark.fieldMap[Waterpark.CASH_POS_1])

        // coworker changed profession
        assertEquals(CoworkerTask.TRAINER, (coworker as Coworker).coworkerTask)
        // money reduced
        currentState = game.currentGameState[game.currentState]
        assertEquals(0, currentState.players[2].numCoins)

        currentState.currentPlayer = 2
        //no money
        assertFailsWith<IllegalStateException> {
            rootService!!.playerActionService.actionCMove(coworker, Pair(10, 12))
        }
    }
}