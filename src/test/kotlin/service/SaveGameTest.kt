package service

import entity.*
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths
import kotlin.test.*

/**
 * Tests for the saveGame-function of [AquarettoGameService]
 */
class SaveGameTest {

    private val dummyGame = AquarettoGame()
    private val actualGame = AquarettoGame()

    private var dummyRootService: RootService? = null
    private var actualRootService: RootService? = null


    /**
     * Setup for the tests (adding a [AquarettoGameState] for each [RootService])
     */
    @BeforeTest
    fun setupTests(){
        Files.createDirectory(Paths.get("./testSaves"))
        dummyGame.currentGameState.add(AquarettoGameState())

        actualGame.currentGameState.add(
            AquarettoGameState(
                mutableListOf(
                    Player("Marcel", 4, PlayerType.LOCAL_HUMAN),
                    Player("R2D2", 4, PlayerType.LOCAL_AI),
                    Player("C3PO", 4, PlayerType.LOCAL_RANDOM_AI)
                )
            )
        )

        dummyRootService = RootService(dummyGame)
        actualRootService = RootService(actualGame)
    }

    /**
     * Quick test to see if a new file is generated
     */
    @Test
    fun testSaveGameExists(){
        dummyRootService!!.aquarettoGameService.saveGame(File("testSaves/test.json"))

        assertTrue{File("testSaves/test.json").exists()}
    }

    /**
     * Test the save feature for an "actual" game by saving the game, loading it with the
     * JSON-function and comparing the values in the new [AquarettoGameState]-object with
     * the expected values (the ones that should have been saved).
     */
    @Test
    fun testActualSaveGame(){
        val game = actualRootService!!.currentGame
        game.currentGameState[game.currentState].players[1].turnEnded = true
        game.currentGameState[game.currentState].players[0].numCoins = 10
        game.currentGameState[game.currentState].players[2].depot.add(AnimalTile(0, animalType = Animal.CROCODILE))
        game.currentGameState[game.currentState].currentPlayer = 2
        game.currentGameState[game.currentState].players[2].waterPark.fieldMap[Pair(10, 10)] = AnimalTile(
            1, animalType = Animal.HIPPO
        )
        actualRootService!!.aquarettoGameService.saveGame(File("testSaves/actualGame.json"))

        // change the game after the save to see if it influences the save (it shouldn't)
        game.currentGameState[game.currentState].players[1].turnEnded = false

        val loadedGameState = Json{allowStructuredMapKeys = true}.decodeFromString<AquarettoGameState>(
            File("testSaves/actualGame.json").readText()
        )
        assertEquals(3, loadedGameState.players.size)
        assertEquals(2, loadedGameState.currentPlayer)
        assertFalse { loadedGameState.finalRound }

        assertEquals("Marcel", loadedGameState.players[0].name)
        assertEquals(PlayerType.LOCAL_HUMAN, loadedGameState.players[0].playerType)

        assertEquals("R2D2", loadedGameState.players[1].name)
        assertEquals(PlayerType.LOCAL_AI, loadedGameState.players[1].playerType)

        assertEquals("C3PO", loadedGameState.players[2].name)
        assertEquals(PlayerType.LOCAL_RANDOM_AI, loadedGameState.players[2].playerType)

        assertFalse{ loadedGameState.players[0].turnEnded }
        assertTrue{ loadedGameState.players[1].turnEnded }   //change in line 40 didn't affect the save game
        assertFalse{ loadedGameState.players[2].turnEnded }  //(as it should)

        assertEquals(10, loadedGameState.players[0].numCoins)
        assertEquals(1, loadedGameState.players[1].numCoins)
        assertEquals(1, loadedGameState.players[2].numCoins)

        assertTrue{ loadedGameState.players[0].depot.isEmpty() }
        assertTrue{ loadedGameState.players[1].depot.isEmpty() }
        assertTrue{ loadedGameState.players[2].depot.isNotEmpty()
                && loadedGameState.players[2].depot[0] is AnimalTile }

        assertEquals(0, loadedGameState.players[2].depot[0].id)
        assertEquals(Animal.CROCODILE, (loadedGameState.players[2].depot[0] as AnimalTile).animalType)

        assertEquals(1, (loadedGameState.players[2].waterPark.fieldMap[Pair(10, 10)] as AnimalTile).id)
        assertEquals(Animal.HIPPO,
            (loadedGameState.players[2].waterPark.fieldMap[Pair(10, 10)] as AnimalTile).animalType)
    }


    /**
     * Function to delete testSaves directory after testing as clean up.
     */
    @AfterTest
    fun deleteEveryTestSave(){
        val dir = File("./testSaves/")
        dir.deleteRecursively()
    }
}