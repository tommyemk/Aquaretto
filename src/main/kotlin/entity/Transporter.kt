package entity

import kotlinx.serialization.Serializable

/**
 * Class that models the transporters that contain tiles in the middle of the board.
 *
 * @property taken Flag to signify if a player has taken the transporter
 * @property tiles [MutableList] of tiles that have been placed on the Transporter (3 at most)
 */
@Serializable
data class Transporter(var tilesCapacity: Int = 3) {
    var taken: Boolean = false
    var tiles: MutableList<Tile> = mutableListOf()


    /**
     * clones a transporter
     */
    fun clone():Transporter{
        val newTransporter = Transporter( tilesCapacity = this.tilesCapacity)
        newTransporter.taken = this.taken
        newTransporter.tiles = this.tiles.map { it.clone() }.toMutableList()

        return newTransporter
    }
}