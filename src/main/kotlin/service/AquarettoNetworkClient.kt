package service

import edu.udo.cs.sopra.ntf.*
import entity.PlayerType
import tools.aqua.bgw.core.BoardGameApplication
import tools.aqua.bgw.net.client.BoardGameClient
import tools.aqua.bgw.net.client.NetworkLogging
import tools.aqua.bgw.net.common.GameAction
import tools.aqua.bgw.net.common.annotations.GameActionReceiver
import tools.aqua.bgw.net.common.notification.PlayerJoinedNotification
import tools.aqua.bgw.net.common.notification.PlayerLeftNotification
import tools.aqua.bgw.net.common.response.CreateGameResponse
import tools.aqua.bgw.net.common.response.CreateGameResponseStatus
import tools.aqua.bgw.net.common.response.JoinGameResponse
import tools.aqua.bgw.net.common.response.JoinGameResponseStatus

/**
 *
 * class setting network client
 *
 * @property playerName obvious ...
 * @property host is the host
 * @property networkService doing the networkservice
 *
 */
class AquarettoNetworkClient(
    playerName: String,
    host: String,
    secret: String,
    private val networkService: NetworkService,
) : BoardGameClient(playerName, host, secret, NetworkLogging.VERBOSE) {

    var sessionID: String? = null

    private var connectedPlayerNames = mutableListOf(playerName)
    var playerType: PlayerType? = null

    var partialMessage: GameAction? = null


    /**
     * Handles server response after hosting a game was requested
     */
    override fun onCreateGameResponse(response: CreateGameResponse) {
        BoardGameApplication.runOnGUIThread {
            check(networkService.connectionState == ConnectionState.WAITING_FOR_HOST_CONFIRMATION) {
                "unexpected CreateGameResponse"
            }

            when (response.status) {
                CreateGameResponseStatus.SUCCESS -> {
                    sessionID = response.sessionID
                    networkService.onAllRefreshables { refreshAfterHostGame(response.sessionID!!) }
                    networkService.connectionState = ConnectionState.WAITING_FOR_GUESTS
                }
                else -> {
                    disconnectAndError(response.status)
                }
            }
        }
    }

    /**
     * Handles server response after joining a game was requested
     */
    override fun onJoinGameResponse(response: JoinGameResponse) {
        BoardGameApplication.runOnGUIThread {
            check(networkService.connectionState == ConnectionState.WAITING_FOR_JOIN_CONFIRMATION) {
                "unexpected JoinGameResponse"
            }

            when (response.status) {
                JoinGameResponseStatus.SUCCESS -> {
                    sessionID = response.sessionID
                    connectedPlayerNames.addAll(response.opponents)
                    networkService.onAllRefreshables { refreshAfterJoinGame(connectedPlayerNames) }
                    networkService.connectionState = ConnectionState.WAITING_FOR_INIT
                }
                else -> {
                    disconnectAndError(response.status)
                }
            }
        }
    }

    /**
     * Handles server response when a player joins
     */
    override fun onPlayerJoined(notification: PlayerJoinedNotification) {
        BoardGameApplication.runOnGUIThread {
            check(networkService.connectionState == ConnectionState.WAITING_FOR_GUESTS ||
                    networkService.connectionState == ConnectionState.WAITING_FOR_INIT) {
                "unexpected player joined"
            }

            connectedPlayerNames.add(notification.sender)
            networkService.onAllRefreshables { refreshAfterPlayerJoin(connectedPlayerNames) }
        }
    }

    /**
     * Handles server response when a player leaves
     */
    override fun onPlayerLeft(notification: PlayerLeftNotification) {
        BoardGameApplication.runOnGUIThread {
            check(networkService.connectionState == ConnectionState.WAITING_FOR_GUESTS ||
                    networkService.connectionState == ConnectionState.WAITING_FOR_INIT) {
                "player left after game started"
            }

            connectedPlayerNames.remove(notification.sender)
            networkService.onAllRefreshables { refreshAfterPlayerLeave(connectedPlayerNames) }
        }
    }

    /**
     * Handles InitGameMessage
     */
    @Suppress("UNUSED_PARAMETER", "unused")
    @GameActionReceiver
    fun onInitGame(msg: InitGameMessage, sender: String) {
        BoardGameApplication.runOnGUIThread {
            networkService.receiveInitGame(msg)
        }
    }

    /**
     * Handles action A
     */
    @Suppress("unused")
    @GameActionReceiver
    fun onAddTileToTruck(msg: AddTileToTruckMessage, sender: String) {
        BoardGameApplication.runOnGUIThread {
            networkService.receiveAddTileToTruck(msg, sender)
        }
    }

    /**
     * Handles action B
     */
    @Suppress("unused")
    @GameActionReceiver
    fun onTakeTruck(msg: TakeTruckMessage, sender: String) {
        BoardGameApplication.runOnGUIThread {
            networkService.receiveTakeTruck(msg, sender)
        }
    }

    /**
     * Handles action C (buy expansion)
     */
    @Suppress("unused")
    @GameActionReceiver
    fun onBuyExpansion(msg: BuyExpansionMessage, sender: String) {
        BoardGameApplication.runOnGUIThread {
            networkService.receiveBuyExpansion(msg, sender)
        }
    }

    /**
     * Handles action C (move coworker)
     */
    @Suppress("unused")
    @GameActionReceiver
    fun onMoveCoworker(msg: MoveCoworkerMessage, sender: String) {
        BoardGameApplication.runOnGUIThread {
            networkService.receiveMoveCoworker(msg, sender)
        }
    }

    /**
     * Handles action C (discard)
     */
    @Suppress("UNUSED_PARAMETER", "unused")
    @GameActionReceiver
    fun onDiscard(msg: DiscardMessage, sender: String) {
        BoardGameApplication.runOnGUIThread {
            networkService.receiveDiscard(sender)
        }
    }

    /**
     * Handles action C (move tile)
     */
    @Suppress("unused")
    @GameActionReceiver
    fun onDiscard(msg: MoveTileMessage, sender: String) {
        BoardGameApplication.runOnGUIThread {
            networkService.receiveMoveTile(msg, sender)
        }
    }

    /**
     * disconets and send error
     */
    private fun disconnectAndError(message: Any) {
        networkService.disconnect()
        error(message)
    }

}
