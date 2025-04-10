package service

import entity.Placeable
import entity.Player
import entity.Tile
import entity.WaterparkExtension
import view.Refreshable

/**
 * Tests all refreshes so that the gui is
 * able to display everything correctly.
 *
 * @property refreshAfterActionA refreshes after method call ActionA()
 * @property refreshAfterActionB refreshes after method call ActionB()
 * @property refreshAfterActionCMove refreshes after method call ActionCMove()
 * @property refreshAfterActionCPurchase refreshes after method call ActionCPurchase()
 * @property refreshAfterCDiscardTile refreshes after method call ActionCDiscardTile()
 * @property refreshAfterCExtendWaterPark refreshes after method call ActionCExtendWaterPark()
 * @property refreshAfterSaveGame refreshes after starting the game
 * @property refreshAfterNextPlayer refreshes after the next player is assigned
 * @property refreshAfterEndGame refreshes after the game ended
 * @property refreshAfterSaveGame refreshes after the player saves the game
 * @property refreshAfterLoadGame refreshes after the player loads a game
 */
class TestRefreshable : Refreshable {

    private var refreshAfterActionA = false
    private var refreshAfterActionB = false
    private var refreshAfterActionCMove = false
    private var refreshAfterActionCPurchase = false
    private var refreshAfterCDiscardTile = false
    private var refreshAfterCExtendWaterPark = false
    private var refreshAfterStartGame = false
    private var refreshAfterNextPlayer = false
    private var refreshAfterEndGame = false
    private var refreshAfterSaveGame = false
    private var refreshAfterLoadGame = false

    var refreshAfterHostGame = false
        private set

    var inviteCode: String? = null
    var playerNames: List<String>? = null

    var refreshAfterJoinGame = false
        private set

    var refreshAfterPlayerJoin = false
        private set

    private var refreshAfterPlayerLeave = false
    private var refreshAfterUndo = false
    private var refreshAfterRedo = false

    /**
     * Refreshes after calling the method ActionA()
     */
    override fun refreshAfterActionA(transporterIndex: Int) {
        refreshAfterActionA = true
    }

    /**
     * Refreshes after calling the method ActionB()
     */
    override fun refreshAfterActionB(transporterIndex: Int, coordinatesMap: Map<Tile, Pair<Int, Int>>) {
        refreshAfterActionB = true
    }

    /**
     * Refreshes after calling the method ActionCMove()
     */
    override fun refreshAfterActionCMove(tile: Placeable, coordinates: Pair<Int, Int>) {
        refreshAfterActionCMove = true
    }

    /**
     * Refreshes after calling the method ActionCPurchase()
     */
    override fun refreshAfterActionCPurchase(seller: Player, coordinates: Pair<Int, Int>) {
        refreshAfterActionCPurchase = true
    }

    /**
     * Refreshes after calling the method CDiscardTile()
     */
    override fun refreshAfterCDiscardTile() {
        refreshAfterCDiscardTile = true
    }

    /**
     * Refreshes after calling the method CExtendWaterPark()
     */
    override fun refreshAfterCExtendWaterPark(waterParkExtension: WaterparkExtension) {
        refreshAfterCExtendWaterPark = true
    }

    /**
     * Refreshes after starting the game.
     */
    override fun refreshAfterStartGame() {
        refreshAfterStartGame = true
    }

    /**
     * Refreshes after the next current player is assigned and therefore has the next turn.
     */
    override fun refreshAfterNextPlayer() {
        refreshAfterNextPlayer = true
    }

    /**
     * Refreshes after ending the game.
     */
    override fun refreshAfterEndGame(points: List<Pair<Player, Int>>) {
        refreshAfterEndGame = true
    }

    /**
     * Refreshes after saving the game.
     */
    override fun refreshAfterSaveGame() {
        refreshAfterSaveGame = true
    }

    /**
     * Refreshes after loading the game.
     */
    override fun refreshAfterLoadGame() {
        refreshAfterLoadGame = true
    }

    /**
     * Refreshes after hosting the game.
     */
    override fun refreshAfterHostGame(inviteCode: String) {
        refreshAfterHostGame = true
        this.inviteCode = inviteCode
    }

    /**
     * Refreshes after joining a game.
     */
    override fun refreshAfterJoinGame(playerNames: List<String>) {
        refreshAfterJoinGame = true
        this.playerNames = playerNames
    }

    /**
     * Refreshes after the player joins a game.
     */
    override fun refreshAfterPlayerJoin(playerNames: List<String>) {
        refreshAfterPlayerJoin = true
        this.playerNames = playerNames
    }

    /**
     * Refreshes after the player leaves the game.
     */
    override fun refreshAfterPlayerLeave(playerNames: List<String>) {
        refreshAfterPlayerLeave = true
        this.playerNames = playerNames
    }

    /**
     * Refreshes after the current player wants to undo his last actions.
     */
    override fun refreshAfterUndo() {
        refreshAfterUndo = true
    }

    /**
     * Refreshes after the current player wants to redo his last actions.
     */
    override fun refreshAfterRedo() {
        refreshAfterRedo = true
    }

    /**
     * Resets all refreshables.
     */
    fun reset() {
        refreshAfterActionA = false
        refreshAfterActionB = false
        refreshAfterActionCMove = false
        refreshAfterActionCPurchase = false
        refreshAfterCDiscardTile = false
        refreshAfterCExtendWaterPark = false
        refreshAfterStartGame = false
        refreshAfterNextPlayer = false
        refreshAfterEndGame = false
        refreshAfterSaveGame = false
        refreshAfterLoadGame = false
        refreshAfterHostGame = false
        inviteCode = null
        playerNames = null
        refreshAfterJoinGame = false
        refreshAfterPlayerJoin = false
        refreshAfterPlayerLeave = false
        refreshAfterUndo = false
        refreshAfterRedo = false
    }
}