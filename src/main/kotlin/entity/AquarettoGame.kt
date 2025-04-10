package entity

/**
 * This class represents a game of Aquaretto
 * @property isOnlineGame Whether the game is an online game or not.
 * @property speed The speed of the game.
 * @property currentState The current state of the game.
 * @property currentGameState list of the current game state.
 */
class AquarettoGame{
    var isOnlineGame = false
    var speed = 0
    var currentState = 0
    var currentGameState = MutableList<AquarettoGameState>(0) { AquarettoGameState() }
}