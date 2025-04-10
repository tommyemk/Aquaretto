package entity

import kotlinx.serialization.Serializable

/**
 * Class to model each tile that is currently in the game. Both [AnimalTile] and [CoinTile]
 * inherit from this class.
 *
 * @property id Unique id for each possible tile in the game. Used for online games mainly to keep track
 * of the tiles.
 */
@Serializable
sealed class Tile: Placeable{ //class is sealed for serializer
    abstract val id: Int

    abstract override fun clone():Tile
}