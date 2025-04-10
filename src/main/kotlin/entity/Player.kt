package entity

import kotlinx.serialization.Serializable

/**
 * Class to model each player in the game
 *
 * @property name Name of the player
 * @property numberCoworker Number of coworkers the player has placed
 * @property playerType Type of the player (Human, AI, Local, Online)
 * @property depot Depot of the player as a [MutableList] of Tiles
 * @property numCoins NUmber of coins the player has
 * @property turnEnded Flag to signify if the player is done with the current turn (took a transporter)
 * @property waterPark The [Waterpark] of the player
 * @property waterParkExtensionList List of the Waterpark-Extensions
 */

@Serializable
class Player(
    val name: String, var numberCoworker: Int, val playerType: PlayerType,
    val depot: MutableList<Tile> = mutableListOf()
) {
    var numCoins = 1
    var turnEnded: Boolean = false
    var waterPark = Waterpark()
    var waterParkExtensionList = listOf(
        WaterparkExtension(isSmall = true), WaterparkExtension(isSmall = true),
        WaterparkExtension(), WaterparkExtension()
    )
    var offspringsToPlace: MutableMap<Animal, Int> = mutableMapOf()
    var coworkersToPlace: Int = 0


    /**
     * clones a player. who could have thought of that :D
     */
    fun clone():Player{
        val newPlayer = Player(
            name = this.name,
            numberCoworker = this.numberCoworker,
            playerType = this.playerType,
            depot = this.depot
        )
        newPlayer.numCoins = this.numCoins
        newPlayer.turnEnded = this.turnEnded
        newPlayer.waterPark = this.waterPark
        newPlayer.waterParkExtensionList = this.waterParkExtensionList.map { it.clone() }.toMutableList()

        return newPlayer
    }
}