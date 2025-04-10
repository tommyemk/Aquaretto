package service

import entity.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
/**
 * This class tests the point calculation functionality of the AquarettoGameService.
 * It sets up a mock game environment, populates it with predefined entities, and
 * verifies if the points are calculated correctly for a player.
 */

class CalcPointsTest {
    val rootService= RootService()
    private lateinit var aquarettoGameService: AquarettoGameService
    // Sample depot tiles for player1
    val depotPlayer1 = mutableListOf<Tile>(
       AnimalTile (0, animalType=Animal.POLAR_BEAR) ,
       AnimalTile (0, animalType=Animal.SEA_TURTLE) ,
       AnimalTile (0, animalType=Animal.SEA_TURTLE) ,

    )
    // Sample water park tiles for player1
    val waterParkPlayer1 = mutableMapOf<Pair<Int,Int>,Placeable>(
        Pair(1, 0) to AnimalTile(0, animalType = Animal.DOLPHIN, isTrainable = true),
        Pair(2, 0) to AnimalTile(0, animalType = Animal.DOLPHIN, isTrainable = true),
        Pair(0, 1) to AnimalTile(0, animalType = Animal.DOLPHIN, isTrainable = true),
        Pair(1, 1) to AnimalTile(0, animalType = Animal.DOLPHIN, isTrainable = true),
        Pair(2, 1) to AnimalTile(0, animalType = Animal.DOLPHIN, isTrainable = true),

        Pair(0, 2) to AnimalTile(0, animalType = Animal.PENGUIN, isTrainable = true),
        Pair(1, 3) to AnimalTile(0, animalType = Animal.PENGUIN, isTrainable = true),
        Pair(2, 3) to AnimalTile(0, animalType = Animal.PENGUIN, isTrainable = true),
        Pair(3, 3) to AnimalTile(0, animalType = Animal.PENGUIN),
        Pair(10, 2) to AnimalTile(0, animalType = Animal.PENGUIN, isTrainable = true),

        Pair(10, 3) to AnimalTile(0, animalType = Animal.DOLPHIN, isTrainable = true, hasFish = true),
        Pair(10, 4) to AnimalTile(0, animalType = Animal.DOLPHIN, isTrainable = true, hasFish = true),
        Pair(10, 5) to AnimalTile(0, animalType = Animal.DOLPHIN, isTrainable = true, hasFish = true),
        Pair(10, 6) to AnimalTile(0, animalType = Animal.DOLPHIN, isTrainable = true, hasFish = true),
        Pair(10, 7) to AnimalTile(0, animalType = Animal.DOLPHIN, isTrainable = true, hasFish = true),

        Pair(10, 8) to AnimalTile(0, animalType = Animal.DOLPHIN, isTrainable = true),
        Pair(10, 9) to AnimalTile(0, animalType = Animal.DOLPHIN, isTrainable = true),
        Pair(10, 10) to AnimalTile(0, animalType = Animal.DOLPHIN, isTrainable = true),
        Pair(10, 11) to AnimalTile(0, animalType = Animal.DOLPHIN, isTrainable = true),
        // Coworker
        Pair(1, 2) to Coworker(CoworkerTask.TRAINER),
        Pair(2, 2) to Coworker(CoworkerTask.TRAINER),
        Pair(1, 4) to Coworker(CoworkerTask.ANIMAL_KEEPER),
    )


    private var player1 = Player("Mila" , 3, PlayerType.LOCAL_HUMAN, depotPlayer1)



    @BeforeEach
            /**
             * Sets up the necessary environment for each test.
             * This includes initializing the AquarettoGameService and setting up the sample game state.
             */
    fun setUp() {
        aquarettoGameService = AquarettoGameService(rootService)

     player1.waterPark.fieldMap=waterParkPlayer1
    }

    @Test
      /**
       * Tests the calcPoints method to ensure it accurately calculates the total points for all players.
       * It specifically verifies the points calculation for a single player based on the predefined game state.
       */
    fun calcPoints() {
        val game = rootService.currentGame
        game.currentGameState.add(AquarettoGameState())
        game.currentGameState[game.currentState].players.add(player1)

        val scores = aquarettoGameService.calcPoints()
       assertEquals(scores[0].second,30)
    }


}
