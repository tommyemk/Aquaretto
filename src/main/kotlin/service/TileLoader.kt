package service

import entity.Animal
import entity.AnimalTile
import entity.CoinTile
import entity.Tile

private const val TILES_FILE = "/tiles.csv"

/**
 * class to load the tiles
 */
object TileLoader {

    /**
     * Creates a list of all tiles sorted by ID
     */
    fun createTileList(): List<Tile> {
        val reader = TileLoader::class.java.getResource(TILES_FILE)!!.openStream().bufferedReader()
        reader.readLine() // Ignore header

        return reader.lineSequence()
            .filter { it.isNotBlank() }
            .map {
                val line = it.split(";", ignoreCase = false, limit = 3)
                val id = line[0].trim().toInt()
                var name = line[1].trim().uppercase()
                val option = line[2].trim()
                if (name == "HIPPOPOTAMUS") {
                    name = "HIPPO"
                }

                when (name.trim()) {
                    "COIN" -> CoinTile(id)
                    else -> AnimalTile(
                        id = id,
                        animalType = Animal.valueOf(name),
                        isFemale = option == "w",
                        isMale = option == "m",
                        isTrainable = option != "l" &&
                                Animal.valueOf(name) in listOf(Animal.DOLPHIN, Animal.ORCA, Animal.SEA_LION),
                        isOffspring = option == "o",
                        hasFish = option == "f"
                    )
                }
            }
            .sortedBy { it.id }
            .toList()
    }

    /**
     * Creates a list of every tile that's not an offspring (not shuffled)
     */
    fun createDepotList() = createTileList().filter { !(it is AnimalTile && it.isOffspring) }

    /**
     * Creates a list of every offspring tile (not shuffled)
     */
    fun createOffspringList() = createTileList().filter { it is AnimalTile && it.isOffspring }.map { it as AnimalTile }
}