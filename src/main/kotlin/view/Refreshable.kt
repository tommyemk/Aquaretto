package view

import entity.Placeable
import entity.Player
import entity.Tile
import entity.WaterparkExtension

/**
 * here you will find refresh methods
 */
interface Refreshable {

    /**
     * refreshes action a
     *
     * @param transporterIndex the index from
     * the transporter which the player has chosen
     */
    fun refreshAfterActionA(transporterIndex: Int) {}

    /**
     * refreshes action b
     *
     * @param transporterIndex the index from
     * the transporter which the player has chosen
     * @param coordinatesMap a map which holds tiles
     * with their specific coordinates
     */
    fun refreshAfterActionB(transporterIndex: Int, coordinatesMap: Map<Tile, Pair<Int, Int>>) {}

    /**
     * refreshes c move
     *
     * @param tile which wants to be removed
     * @param coordinates the coordinates where the tile should be placed
     */
    fun refreshAfterActionCMove(tile: Placeable, coordinates: Pair<Int, Int>) {}

    /**
     * refreshes c purchase
     *
     * @param seller the player from which the
     * current player wants to buy
     * @param coordinates the coordinates where the bought tile should be placed
     */
    fun refreshAfterActionCPurchase(seller: Player, coordinates: Pair<Int, Int>) {}

    /**
     * refreshes c discard
     */
    fun refreshAfterCDiscardTile() {}

    /**
     * refreshes c extension
     *
     * @param waterParkExtension the waterpark extension
     */
    fun refreshAfterCExtendWaterPark(waterParkExtension: WaterparkExtension) {}

    /**
     * refreshes start game
     */
    fun refreshAfterStartGame() {}

    /**
     * refreshes next player
     */
    fun refreshAfterNextPlayer() {}

    /**
     * refreshes endGame
     */
    fun refreshAfterEndGame(points: List<Pair<Player, Int>>) {}

    /**
     * refreshes saveGame
     */
    fun refreshAfterSaveGame() {}

    /**
     * refreshes load
     */
    fun refreshAfterLoadGame() {}

    /**
     * refreshes after a coworker or offspring from a special situation gets placed
     */
    fun refreshAfterSpecialPlaced() {}

    /**
     * refreshes host
     */
    fun refreshAfterHostGame(inviteCode: String) {}

    /**
     * refreshes join
     */
    fun refreshAfterJoinGame(playerNames: List<String>) {}

    /**
     * refreshes playerjoin
     */
    fun refreshAfterPlayerJoin(playerNames: List<String>) {}

    /**
     * refreshes player leave
     */
    fun refreshAfterPlayerLeave(playerNames: List<String>) {}

    /**
     * refreshes undo
     */
    fun refreshAfterUndo() {}

    /**
     * refreshes redo
     */
    fun refreshAfterRedo() {}

    /**
     * refreshes after one round ended
     */
    fun refreshAfterRoundEnds() {}
}