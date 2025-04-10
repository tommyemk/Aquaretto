package entity

import kotlinx.serialization.Serializable

/**
 * The state of the game
 * @property finalRound Whether the game is in the final round or not.
 * @property currentPlayer The index of the current player.
 */

@Serializable
class AquarettoGameState(var players: MutableList<Player> = mutableListOf()) {
    var finalRound = false
    var currentPlayer = 0
    var board = Board()

    /**
     * method to clone a state
     *
     * returns a AquarettoGameState
     */
    fun clone() : AquarettoGameState {
        val newState = AquarettoGameState()
        newState.finalRound = this.finalRound
        newState.currentPlayer = this.currentPlayer
        newState.players = this.players.map { it.clone() }.toMutableList()
        newState.board = this.board.clone()
        return newState
    }
}