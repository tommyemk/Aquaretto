package entity

import kotlinx.serialization.Serializable

/**
 * Represents the game board in Aquaretto.
 *
 * @property finalPile The pile of tiles that have been completely used during the game.
 * @property mainPile The main pile of tiles that are drawn from during the game.
 * @property transporters The list of transporters used to carry tiles to the players' water parks.
 * @property offspring The list of AnimalTiles representing the offspring tiles in the game.
 */
@Serializable
class Board (var transporters : MutableList<Transporter> = mutableListOf()){
    var finalPile : MutableList<Tile>  = mutableListOf()
    var mainPile: MutableList<Tile>  = mutableListOf()
    var offspring : MutableList<AnimalTile> =  mutableListOf()

    /**
     * method to clone a board
     *
     * returns a board
     */
    fun clone(): Board{
        val newBoard = Board()
        newBoard.transporters = this.transporters.map { it.clone() }.toMutableList()
        newBoard.finalPile = this.finalPile.map { it.clone() }.toMutableList()
        newBoard.mainPile = this.mainPile.map { it.clone() }.toMutableList()
        newBoard.offspring = this.offspring.map { it.clone() as AnimalTile }.toMutableList()
        return newBoard
    }
}
