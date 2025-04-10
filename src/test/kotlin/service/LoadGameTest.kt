package service

import entity.*
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths
import kotlin.test.*

/**
 * Tests for the loadGame-function of [AquarettoGameService]
 */
class LoadGameTest {

    private val EMPTYPATH = "testSaves/emptyGame.json"
    private val ACTUALPATH = "testSaves/actualGame.json"
    private var emptyRootService: RootService? = null
    private var actualRootService: RootService? = null

    /**
     * Set up a few [AquarettoGameState]-JSON files in the testSaves directory for the loadGame()-function
     */
    @BeforeTest
    fun setupTests(){
        Files.createDirectory(Paths.get("./testSaves"))

        val emptyGame = AquarettoGame()
        val actualGame = AquarettoGame()

        val emptyState = AquarettoGameState()
        val actualState = AquarettoGameState(
            mutableListOf(
                Player("Marcel", 4, PlayerType.LOCAL_HUMAN),
                Player("R2D2", 2, PlayerType.LOCAL_AI,
                    mutableListOf(AnimalTile(0, animalType = Animal.DOLPHIN))),
                Player("C3PO", 1, PlayerType.LOCAL_RANDOM_AI,
                    mutableListOf(AnimalTile(1, animalType = Animal.PENGUIN)))
            )
        )
        actualState.board.transporters.addAll(listOf(Transporter(), Transporter(), Transporter()))
        actualState.finalRound = true
        actualState.currentPlayer = 2
        actualState.board.finalPile.addAll(listOf(CoinTile(2), CoinTile(3),
                                                    AnimalTile(4, animalType = Animal.ORCA)))
        actualState.board.transporters[0].tiles.add(AnimalTile(5, animalType = Animal.SEA_TURTLE))
        actualState.players[2].waterPark.fieldMap[Pair(11, 11)] = AnimalTile(6, animalType = Animal.POLAR_BEAR)


        actualGame.currentGameState.add(actualState)
        actualRootService = RootService(actualGame)

        emptyGame.currentGameState.add(emptyState)
        emptyRootService = RootService(emptyGame)

        emptyRootService!!.aquarettoGameService.saveGame(File(EMPTYPATH))
        actualRootService!!.aquarettoGameService.saveGame(File(ACTUALPATH))

        //set up one root service with 2 gameStates
        actualRootService!!.currentGame.currentGameState.add(emptyState)
    }

    /**
     * Test the loadGame()-function by using it to load the prepared saves and comparing the values
     * to the expected ones.
     */
    @Test
    fun testEmptyLoadGame(){
        val emptyGame = emptyRootService!!.currentGame
        assertEquals(1, emptyGame.currentGameState.size)

        // non-existent file
        assertFails {emptyRootService!!.aquarettoGameService.loadGame(File("testSaves/FileNotFound.json"))}

        emptyRootService!!.aquarettoGameService.loadGame(File(EMPTYPATH))

        // after loading, there should only be one state
        assertEquals(1, emptyGame.currentGameState.size)

        // test emptyGame:
        val emptyState = emptyGame.currentGameState[emptyGame.currentState]

        assertEquals(0, emptyState.players.size)
        assertEquals(0, emptyState.board.finalPile.size)
        assertEquals(0, emptyState.board.mainPile.size)
        assertEquals(0, emptyState.board.transporters.size)
    }

    /**
     * Testing is the game is able to loaded.
     */
    @Test
    fun testActualGameLoad(){
        val actualGame = actualRootService!!.currentGame
        // before the actual game gets loaded there are 2 gameStates
        // after loading, there should only be one (only one gameState is saved, not the entire list)
        assertEquals(2, actualGame.currentGameState.size)

        actualRootService!!.aquarettoGameService.loadGame(File(ACTUALPATH))

        // after loading, there should only be one state
        assertEquals(1, actualGame.currentGameState.size)

        // test actualGame (just check if the expected values from the setup were loaded):
        val actualState = actualGame.currentGameState[actualGame.currentState]

        assertEquals("Marcel", actualState.players[0].name)
        assertEquals("R2D2", actualState.players[1].name)
        assertEquals("C3PO", actualState.players[2].name)

        assertEquals(4, actualState.players[0].numberCoworker)
        assertEquals(2, actualState.players[1].numberCoworker)
        assertEquals(1, actualState.players[2].numberCoworker)

        assertEquals(PlayerType.LOCAL_HUMAN, actualState.players[0].playerType)
        assertEquals(PlayerType.LOCAL_AI, actualState.players[1].playerType)
        assertEquals(PlayerType.LOCAL_RANDOM_AI, actualState.players[2].playerType)

        assertTrue { actualState.players[0].depot.isEmpty() }
        assertEquals(Animal.DOLPHIN, (actualState.players[1].depot[0] as AnimalTile).animalType)
        assertEquals(0, actualState.players[1].depot[0].id)
        assertEquals(Animal.PENGUIN, (actualState.players[2].depot[0] as AnimalTile).animalType)
        assertEquals(1, actualState.players[2].depot[0].id)

        assertTrue{actualState.finalRound}
        assertEquals(2, actualState.currentPlayer)
        assertEquals(2, actualState.board.finalPile[0].id)
        assertTrue { actualState.board.finalPile[0] is CoinTile }
        assertEquals(3, actualState.board.finalPile[1].id)
        assertTrue { actualState.board.finalPile[1] is CoinTile }
        assertEquals(4, actualState.board.finalPile[2].id)
        assertEquals(Animal.ORCA, (actualState.board.finalPile[2] as AnimalTile).animalType)
        assertEquals(5, actualState.board.transporters[0].tiles[0].id)
        assertEquals(Animal.SEA_TURTLE, (actualState.board.transporters[0].tiles[0] as AnimalTile).animalType)

        assertEquals(6, (actualState.players[2].waterPark.fieldMap[Pair(11, 11)] as AnimalTile).id)
        assertEquals(Animal.POLAR_BEAR,
            (actualState.players[2].waterPark.fieldMap[Pair(11, 11)] as AnimalTile).animalType)
    }

    /**
     * Function to delete every save in the testSaves directory after testing as clean up.
     */
    @AfterTest
    fun deleteEveryTestSave(){
        val dir = File("./testSaves/")
        dir.deleteRecursively()
    }
}