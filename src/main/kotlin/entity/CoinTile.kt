package entity

import kotlinx.serialization.Serializable

/**
 * A class to represent those coins which can be drawn by chance from the draw pile.
 *
 * @property id uniquely identifies each [Tile] of the game
 */
@Serializable
data class CoinTile(override val id : Int) : Tile(){

    /**
     * Creates new CoinTile with the same id as this object.
     *
     * @return the new CoinTile
     */
    override fun clone(): Tile {
        val newCoinTile = CoinTile(
            id = this.id
        )
        return newCoinTile
    }
}
