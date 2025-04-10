package service

import entity.Animal
import entity.AnimalTile
import entity.CoinTile
import kotlin.test.*

/**
 * Tests if all piles are correctly able to created with the TileLoader.
 */
class TestTileLoader {

    /**
     * Tests is the method is able to create a tile list.
     */
    @Test
    fun testTileLoader() {
        val tileList = TileLoader.createTileList()

        // Check if sorted
        tileList.forEachIndexed { i, tile ->
            assertEquals(i, tile.id - 1)
        }

        // Check number of tile types
        assertEquals(88+10+16, tileList.size)
        assertEquals(10, tileList.count { it is CoinTile })

        Animal.values().forEach {  animalType ->
            assertEquals(11+2, tileList.count {it is AnimalTile && it.animalType == animalType})
            assertEquals(2, tileList.count {it is AnimalTile && it.animalType == animalType && it.isFemale })
            assertEquals(2, tileList.count {it is AnimalTile && it.animalType == animalType && it.isMale })
        }

        assertEquals(6*5, tileList.count { it is AnimalTile && it.hasFish})
        assertEquals(2*3+(11+2)*5, tileList.count { it is AnimalTile && !it.isTrainable})
    }

    /**
     * Tests the method is able to create a depot list.
     */
    @Test
    fun testDepotList() {
        val tileList = TileLoader.createDepotList()

        assertEquals(88+10, tileList.size)

        // There shouldn't be offsprings on the depot
        assertEquals(0, tileList.count { it is AnimalTile && it.isOffspring})

        // Check number of tile types
        Animal.values().forEach {  animalType ->
            assertEquals(11, tileList.count {it is AnimalTile && it.animalType == animalType})
        }
    }

    /**
     * Tests whether an offspringList can be created.
     */
    @Test
    fun testOffspringList() {
        val tileList = TileLoader.createOffspringList()

        // Every Tile should be an offspring
        assertEquals(16, tileList.size)
        assertEquals(16, tileList.count { it.isOffspring })

        // Check number of tile types
        Animal.values().forEach {  animalType ->
            assertEquals(2, tileList.count { it.animalType == animalType })
        }
    }
}