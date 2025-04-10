package service

import edu.udo.cs.sopra.ntf.*
import entity.*
import java.util.Collections
import kotlin.IllegalStateException

val SMALL_EXTENSION_POSITIONS = listOf(
    listOf(
        PositionPair(0, 0),
        PositionPair(0, 1),
        PositionPair(1, 1)
    ),
    listOf(
        PositionPair(0, 0),
        PositionPair(0, 1),
        PositionPair(1, 0)
    ),
    listOf(
        PositionPair(0, 0),
        PositionPair(1, 0),
        PositionPair(1, 1)
    ),
    listOf(
        PositionPair(0, 1),
        PositionPair(1, 0),
        PositionPair(1, 1)
    )
)
val BIG_EXTENSION_POSITIONS =
    listOf(
        PositionPair(0, 0),
        PositionPair(0, 1),
        PositionPair(1, 0),
        PositionPair(1, 1)
    )

/**
 *
 * class doing network stuff
 */
class NetworkService(private val rootService: RootService) : AbstractRefreshingService() {

    companion object {
        const val SERVER_ADDRESS = "sopra.cs.tu-dortmund.de:80/bgw-net/connect"
        const val SECRET = "aqua24a"
        const val GAME_ID = "Aquaretto"

        /**
         * Converts a coordinate of the format used in this implementation to a network coordinate
         */
        fun localToNetworkCoordinate(coordinate: PositionPair): PositionPair {
            return if (coordinate == Waterpark.DEPOT_POS.toPos()) {
                PositionPair(0, 0)
            } else {
                PositionPair(coordinate.x - 8, -(coordinate.y - 12))
            }
        }

        /**
         * Converts a network coordinate to the format used in this implementation
         */
        fun networkToLocalCoordinate(coordinate: PositionPair): PositionPair {
            return if (coordinate == PositionPair(0, 0)) {
                Waterpark.DEPOT_POS.toPos()
            } else {
                PositionPair(coordinate.x + 8, -coordinate.y + 12)
            }
        }
    }

    /** Network client null if offline game */
    private var client: AquarettoNetworkClient? = null

    /**
     * Current state of the network game
     */
    var connectionState: ConnectionState = ConnectionState.DISCONNECTED

    /**
     * Connects to server, sets [NetworkService.client] and returns true if successful
     *
     * @param name Player name. Must not be blank
     *
     * @throws IllegalArgumentException if [name] is blank
     * @throws IllegalStateException if already connected to a game
     */
    private fun connect(name: String): Boolean {
        check(connectionState == ConnectionState.DISCONNECTED && client == null) { "already connected to a game" }
        require(name.isNotBlank()) { "player name must be given" }

        val netClient = AquarettoNetworkClient(
            playerName = name,
            host = SERVER_ADDRESS,
            secret = SECRET,
            networkService = this
        )

        if (!netClient.connect()) {
            return false
        }
        this.client = netClient
        connectionState = ConnectionState.CONNECTED
        return true
    }

    /**
     * Disconnects from the server. Can be called anytime
     */
    fun disconnect() {
        client?.apply {
            if (sessionID != null) {
                leaveGame("byeeeeee")
            }
            if (isOpen) {
                disconnect()
            }
            client = null
            connectionState = ConnectionState.DISCONNECTED
        }
    }

    /**
     * Hosts a game
     *
     * @param name Player name
     * @param playerType Player type of the local player
     *
     *  @throws IllegalStateException if the connection fails
     *  @throws IllegalArgumentException if the name is blank
     */
    fun hostGame(name: String, playerType: PlayerType) {
        if (!connect(name)) {
            throw IllegalStateException("Connection failed")
        }

        val allowedChars = (('A'..'Z') + ('a'..'z') + ('0'..'9')).toMutableList()
        allowedChars.remove('l')
        allowedChars.remove('I')
        allowedChars.remove('O')

        val inviteCode = (0..4)
            .map { allowedChars.random() }
            .joinToString("")

        client?.createGame(GAME_ID, inviteCode, "meow :3", )
        client?.playerType = playerType
        connectionState = ConnectionState.WAITING_FOR_HOST_CONFIRMATION
    }

    /**
     *  Joins a game
     *
     *  @param inviteCode Invite code of the game
     *  @param name Player name
     *  @param playerType Player type of the local player
     *
     *  @throws IllegalStateException if the connection fails
     *  @throws IllegalArgumentException if the name is blank
     */
    fun joinGame(name: String, playerType: PlayerType, inviteCode: String) {
        if (!connect(name)) {
            throw IllegalStateException("Connection failed")
        }

        client?.joinGame(inviteCode, "meow :3")
        client?.playerType = playerType
        connectionState = ConnectionState.WAITING_FOR_JOIN_CONFIRMATION
    }

    /**
     * Waits until [connectionState] changes to [state] or more than [timeout] ms passed.
     * Used for testing purposes
     *
     * @throws IllegalStateException when [timeout] ms passed without [connectionState] changing to [state]
     */
    fun waitForState(state: ConnectionState, timeout: Int = 5000) {
        var timePassed = 0
        while (timePassed < timeout) {
            if (connectionState == state)
                return
            else {
                Thread.sleep(100)
                timePassed += 100
            }
        }
        error("Did not arrive at state $state after waiting $timeout ms")
    }


    /**
     * changes turning state
     */
    fun changeTurnState() {
        checkNotNull(rootService.currentGame) {
            "There has to be a game to check which turn it is"
        }

        val game = rootService.currentGame
        val gameState = game.currentGameState[game.currentState]
        connectionState = if (gameState.players[gameState.currentPlayer].playerType == PlayerType.ONLINE)
            ConnectionState.WAITING_FOR_OPPONENT
        else
            ConnectionState.WAITING_FOR_MY_TURN
    }

    private fun checkPlayerTurn(player: String) {
        checkNotNull(rootService.currentGame) {
            "There has to be a game to check which turn it is"
        }

        val game = rootService.currentGame
        val gameState = game.currentGameState[game.currentState]
        if (gameState.players[gameState.currentPlayer].name != player) {
            throw IllegalStateException("The wrong player tried to make a turn")
        }
    }

    /**
     * Sends the init message to every connected client. Should be called after the game is initialized.
     *
     * @throws IllegalStateException if [connectionState] != [ConnectionState.WAITING_FOR_GUESTS]
     */
    fun startHostedGame() {
        check(connectionState == ConnectionState.WAITING_FOR_GUESTS)

        val game = rootService.currentGame
        val gameState = game.currentGameState[game.currentState]

        val playerNames = gameState.players.map { it.name }
        Collections.rotate(playerNames, -gameState.currentPlayer) // Current player must be first in list

        client!!.sendGameActionMessage(InitGameMessage(
            players = playerNames,
            drawPile = gameState.board.mainPile.map { it.id } + gameState.board.finalPile.map { it.id }
        ))
        changeTurnState()
    }

    /**
     * Initializes the game after the host started it
     */
    fun receiveInitGame(msg: InitGameMessage) {
        check(connectionState == ConnectionState.WAITING_FOR_INIT) {
            "unexpected init message"
        }

        val tiles = TileLoader.createTileList()
        val mainPile = msg.drawPile.dropLast(15)
            .map { tiles[it - 1] }
        val finalPile = msg.drawPile.takeLast(15)
            .map { tiles[it - 1] }
        val players = msg.players.map {
            Pair(
                it,
                if (it == client?.playerName) client?.playerType ?: PlayerType.LOCAL_HUMAN else PlayerType.ONLINE
            )
        }

        rootService.aquarettoGameService.startGame(
            players = players,
            isOnline = true,
            shuffle = false,
            speed = 1,
            onlineMainPile = mainPile,
            onlineFinalPile = finalPile
        )
        changeTurnState()
    }

    /**
     * Send a message to the other players when action A is used
     */
    fun sendAddTileToTruck(transporterIndex: Int) {
        check(connectionState == ConnectionState.WAITING_FOR_MY_TURN) {
            "It's the turn of a remote player"
        }

        client!!.sendGameActionMessage(AddTileToTruckMessage(transporterIndex))
    }

    /**
     * Called when a remote player uses action A
     */
    fun receiveAddTileToTruck(msg: AddTileToTruckMessage, sender: String) {
        check(connectionState == ConnectionState.WAITING_FOR_OPPONENT) {
            "It's the turn of the local player"
        }
        checkPlayerTurn(sender)

        rootService.playerActionService.actionA(msg.truckId)
    }


    /**
     * sends take truck
     *
     */
    fun sendTakeTruck(
        transporterIndex: Int,
        coordinatesMap: Map<Int, Pair<Int, Int>>
    ) {
        check(connectionState == ConnectionState.WAITING_FOR_MY_TURN) {
            "It's the turn of a remote player"
        }

        val msg = TakeTruckMessage(
            transporterIndex,
            coordinatesMap.mapTo(mutableListOf()) {
                val pos = localToNetworkCoordinate(it.value.toPos())
                AnimalTriple(pos.x, pos.y, it.key)
            },
            listOf(),
            listOf()

        )

        val client = client!!
        val gameState = rootService.currentGame.currentGameState[rootService.currentGame.currentState]
        val player = gameState.players[gameState.currentPlayer]

        if (player.offspringsToPlace.values.sum() == 0 && player.coworkersToPlace == 0) {
            client.sendGameActionMessage(msg)
            client.partialMessage = null
        } else {
            client.partialMessage = msg
        }
    }

    /**
     * Called when a remote player uses action B
     */
    @Suppress("UnreachableCode")
    fun receiveTakeTruck(msg: TakeTruckMessage, sender: String) {
        check(connectionState == ConnectionState.WAITING_FOR_OPPONENT) {
            "It's the turn of the local player"
        }
        checkPlayerTurn(sender)

        val game = rootService.currentGame
        val currentGameState = game.currentGameState[game.currentState]
        val transTiles = currentGameState.board.transporters[msg.truckId].tiles
        val player = currentGameState.players[currentGameState.currentPlayer]

        rootService.playerActionService.actionB(
            msg.truckId,
            msg.animalList.associate {
                Pair(transTiles[it.truck], networkToLocalCoordinate(it.getPos()).toPair())
            }
        )

        msg.offspringList.forEach { offspring ->
            val offspringTile = currentGameState.board.offspring.find { it.id == offspring.tileId }
                ?: throw IllegalArgumentException("Network player is trying to place a non existing offspring")
            rootService.playerActionService.placeOffspring(
                networkToLocalCoordinate(offspring.getPos()).toPair(),
                offspringTile
            )
        }

        msg.workerList.forEach { worker ->
            val localWorker = convertWorkerTriple(worker, player)
            rootService.playerActionService.placeCoworker(localWorker.first, Coworker(localWorker.second))
        }
    }

    /**
     * converting worker triple
     */
    private fun convertWorkerTriple(
        worker: WorkerTriple,
        player: Player
    ): Pair<Pair<Int, Int>, CoworkerTask> {
        val localWorker = when (worker.jobEnum) {
            JobEnum.CASHIER -> {
                Pair(
                    if (player.waterPark.fieldMap.containsKey(Waterpark.CASH_POS_1)) Waterpark.CASH_POS_2
                    else Waterpark.CASH_POS_1,
                    CoworkerTask.CASHIER
                )
            }

            JobEnum.KEEPER -> {
                Pair(
                    if (player.waterPark.fieldMap.containsKey(Waterpark.ANIM_POS_1)) Waterpark.ANIM_POS_2
                    else Waterpark.ANIM_POS_1,
                    CoworkerTask.ANIMAL_KEEPER
                )
            }

            JobEnum.TRAINER -> Pair(networkToLocalCoordinate(worker.getPos()).toPair(), CoworkerTask.TRAINER)
            JobEnum.MANAGER -> Pair(Waterpark.DEPOT_POS, CoworkerTask.MANAGER)
        }
        return localWorker
    }


    /**
     * send bought expansion
     */
    fun sendBuyExpansion(waterparkExtension: WaterparkExtension) {
        check(connectionState == ConnectionState.WAITING_FOR_MY_TURN) {
            "It's the turn of a remote player"
        }

        client!!.sendGameActionMessage(BuyExpansionMessage(
            (if (waterparkExtension.isSmall) SMALL_EXTENSION_POSITIONS[waterparkExtension.rotation]
            else BIG_EXTENSION_POSITIONS)
                .map { localToNetworkCoordinate(it + PositionPair(waterparkExtension.x, waterparkExtension.y)) }
        ))
    }

    /**
     * receives bought expansion
     */
    @Suppress("UnreachableCode")
    fun receiveBuyExpansion(msg: BuyExpansionMessage, sender: String) {
        check(connectionState == ConnectionState.WAITING_FOR_OPPONENT) {
            "It's the turn of the local player"
        }
        checkPlayerTurn(sender)

        val game = rootService.currentGame
        val gameState = game.currentGameState[game.currentState]
        val player = gameState.players[gameState.currentPlayer]

        val sortedPositions = msg.positionList
            .map { networkToLocalCoordinate(it) }
            .sortedBy { it.x * 100 + it.y }
        var placePosition = sortedPositions[0]
        val localPositions = sortedPositions.map { it - placePosition }

        if (localPositions == BIG_EXTENSION_POSITIONS) {
            val extension = player.waterParkExtensionList.find { it.x == -1 && it.y == -1 && !it.isSmall }
                ?: throw IllegalArgumentException(
                    "There is no large waterpark extension left"
                )
            extension.x = placePosition.x
            extension.y = placePosition.y
            extension.rotation = 0
            rootService.playerActionService.actionCExtendWaterPark(extension)

            return
        }

        if (localPositions.size != 3) {
            throw IllegalArgumentException("There is no waterpark extension with this shape")
        }

        val extension = player.waterParkExtensionList.find { it.x == -1 && it.y == -1 && it.isSmall }
            ?: throw IllegalArgumentException("There is no small waterpark extension left")

        var rotation = SMALL_EXTENSION_POSITIONS.indexOf(localPositions)
        if (rotation == -1) {
            // The smallest position isn't in the top left corner for rotation 3
            val adjustedPositions = localPositions.map { it - PositionPair(0, -1) }

            if (adjustedPositions != SMALL_EXTENSION_POSITIONS[3]) {
                throw IllegalArgumentException("There is no waterpark extension with this shape")
            }
            rotation = 3
            placePosition -= PositionPair(0, 1)
        }

        extension.rotation = rotation
        extension.x = placePosition.x
        extension.y = placePosition.y

        rootService.playerActionService.actionCExtendWaterPark(extension)
    }

    /**
     * sends moven coworker
     */
    fun sendMoveCoworker(startPosition: Pair<Int, Int>, destPosition: Pair<Int, Int>) {
        check(connectionState == ConnectionState.WAITING_FOR_MY_TURN) {
            "It's the turn of a remote player"
        }

        client!!.sendGameActionMessage(
            MoveCoworkerMessage(startPosition.toWorkerTriple(), destPosition.toWorkerTriple())
        )
    }

    /**
     * receives moven coworker
     */
    fun receiveMoveCoworker(msg: MoveCoworkerMessage, sender: String) {
        check(connectionState == ConnectionState.WAITING_FOR_OPPONENT) {
            "It's the turn of the local player"
        }
        checkPlayerTurn(sender)

        val game = rootService.currentGame
        val gameState = game.currentGameState[game.currentState]
        val player = gameState.players[gameState.currentPlayer]

        val startPosition = when (msg.start.jobEnum) {
            JobEnum.CASHIER -> if (player.waterPark.fieldMap.containsKey(Waterpark.CASH_POS_1)) Waterpark.CASH_POS_1
            else Waterpark.CASH_POS_2

            JobEnum.KEEPER -> if (player.waterPark.fieldMap.containsKey(Waterpark.ANIM_POS_1)) Waterpark.ANIM_POS_1
            else Waterpark.ANIM_POS_2

            JobEnum.TRAINER -> networkToLocalCoordinate(msg.start.getPos()).toPair()
            JobEnum.MANAGER -> Waterpark.DEPOT_POS
        }

        val tile = player.waterPark.fieldMap[startPosition]
        if (tile !is Coworker) {
            throw IllegalArgumentException("There is no coworker in this position")
        }

        val destPosition = when (msg.destination.jobEnum) {
            JobEnum.CASHIER -> if (!player.waterPark.fieldMap.containsKey(Waterpark.CASH_POS_1)) Waterpark.CASH_POS_1
            else Waterpark.CASH_POS_2

            JobEnum.KEEPER -> if (!player.waterPark.fieldMap.containsKey(Waterpark.ANIM_POS_1)) Waterpark.ANIM_POS_1
            else Waterpark.ANIM_POS_2

            JobEnum.TRAINER -> networkToLocalCoordinate(msg.destination.getPos()).toPair()
            JobEnum.MANAGER -> Waterpark.DEPOT_POS
        }

        rootService.playerActionService.actionCMove(tile, destPosition)
    }

    /**
     * sends discard
     */
    fun sendDiscard() {
        check(connectionState == ConnectionState.WAITING_FOR_MY_TURN) {
            "It's the turn of a remote player"
        }

        client!!.sendGameActionMessage(DiscardMessage())
    }

    /**
     * receives discard
     */
    fun receiveDiscard(sender: String) {
        check(connectionState == ConnectionState.WAITING_FOR_OPPONENT) {
            "It's the turn of the local player"
        }
        checkPlayerTurn(sender)

        rootService.playerActionService.actionCDiscardTile()
    }


    /**
     * sends move tile
     */
    fun sendMoveTile(
        position: Pair<Int, Int>,
        playerName: String?
    ) {
        check(connectionState == ConnectionState.WAITING_FOR_MY_TURN) {
            "It's the turn of a remote player"
        }
        val game = rootService.currentGame
        val gameState = game.currentGameState[game.currentState]
        val player = gameState.players[gameState.currentPlayer]
        val client = client!!

        val msg = MoveTileMessage(
            playerName ?: player.name,
            localToNetworkCoordinate(position.toPos()),
            listOf(),
            listOf()
        )
        if (player.offspringsToPlace.values.sum() == 0 && player.coworkersToPlace == 0) {
            client.sendGameActionMessage(msg)
            client.partialMessage = null
        } else {
            client.partialMessage = msg
        }
    }


    /**
     * receives move tile
     */
    @Suppress("UnreachableCode")
    fun receiveMoveTile(msg: MoveTileMessage, sender: String) {
        check(connectionState == ConnectionState.WAITING_FOR_OPPONENT) {
            "It's the turn of the local player"
        }
        checkPlayerTurn(sender)

        val game = rootService.currentGame
        val gameState = game.currentGameState[game.currentState]
        val currentPlayer = gameState.players[gameState.currentPlayer]

        if (msg.playerName == currentPlayer.name) {
            rootService.playerActionService.actionCMove(
                currentPlayer.depot[0],
                networkToLocalCoordinate(msg.position).toPair()
            )
        } else {
            val seller = gameState.players.find { it.name == msg.playerName }
                ?: throw IllegalArgumentException("Player does not exist")
            rootService.playerActionService.actionCPurchase(
                seller,
                networkToLocalCoordinate(msg.position).toPair()
            )
        }

        msg.offspringList.forEach { offspring ->
            val offspringTile = gameState.board.offspring.find { it.id == offspring.tileId }
                ?: throw IllegalArgumentException("Network player is trying to place a non existing offspring")
            rootService.playerActionService.placeOffspring(
                networkToLocalCoordinate(offspring.getPos()).toPair(),
                offspringTile
            )
        }

        msg.workerList.forEach { worker ->
            val localWorker = convertWorkerTriple(worker, currentPlayer)
            rootService.playerActionService.placeCoworker(localWorker.first, Coworker(localWorker.second))
        }
    }

    /**
     * sends place offspring
     */
    fun sendPlaceOffspring(coordinates: Pair<Int, Int>, offspring: AnimalTile) {
        check(connectionState == ConnectionState.WAITING_FOR_MY_TURN) {
            "It's the turn of a remote player"
        }
        val client = client!!
        val partialMsg = client.partialMessage
        val gameState = rootService.currentGame.currentGameState[rootService.currentGame.currentState]
        val player = gameState.players[gameState.currentPlayer]

        when (partialMsg) {
            is TakeTruckMessage -> {
                val newOffsprings = partialMsg.offspringList.toMutableList()
                val pos = localToNetworkCoordinate(coordinates.toPos())
                newOffsprings.add(OffspringTriple(pos.x, pos.y, offspring.id))

                client.partialMessage = partialMsg.copy(offspringList = newOffsprings)
            }

            is MoveTileMessage -> {
                val newOffsprings = partialMsg.offspringList.toMutableList()
                val pos = localToNetworkCoordinate(coordinates.toPos())
                newOffsprings.add(OffspringTriple(pos.x, pos.y, offspring.id))

                client.partialMessage = partialMsg.copy(offspringList = newOffsprings)
            }

            else -> throw IllegalStateException("Unexpected offspring placed")
        }

        if (player.offspringsToPlace.values.sum() == 0 && player.coworkersToPlace == 0) {
            client.sendGameActionMessage(client.partialMessage!!)
            client.partialMessage = null
        }
    }

    /**
     * sends place coworker
     */
    fun sendPlaceCoworker(coordinates: Pair<Int, Int>) {
        check(connectionState == ConnectionState.WAITING_FOR_MY_TURN) {
            "It's the turn of a remote player"
        }
        val client = client!!
        val partialMsg = client.partialMessage
        val gameState = rootService.currentGame.currentGameState[rootService.currentGame.currentState]
        val player = gameState.players[gameState.currentPlayer]

        when (partialMsg) {
            is TakeTruckMessage -> {
                val newCoworkers = partialMsg.workerList.toMutableList()
                newCoworkers.add(coordinates.toWorkerTriple())

                client.partialMessage = partialMsg.copy(workerList = newCoworkers)
            }

            is MoveTileMessage -> {
                val newCoworkers = partialMsg.workerList.toMutableList()
                newCoworkers.add(coordinates.toWorkerTriple())

                client.partialMessage = partialMsg.copy(workerList = newCoworkers)
            }

            else -> throw IllegalStateException("Unexpected coworker placed")
        }

        if (player.offspringsToPlace.values.sum() == 0 && player.coworkersToPlace == 0) {
            client.sendGameActionMessage(client.partialMessage!!)
            client.partialMessage = null
        }
    }
}

/**
 * Adds the x and the y coordinates from this and a second pair together and creates PositionPair
 */
operator fun PositionPair.plus(other: PositionPair) = PositionPair(this.x + other.x, this.y + other.y)

/**
 * Subtracts the x and the y coordinates from this and a second pair together and creates PositionPair
 */
operator fun PositionPair.minus(other: PositionPair) = PositionPair(this.x - other.x, this.y - other.y)

/**
 * Creates PositionPair with this x and y coordinates
 */
fun PositionPair.toPair() = Pair(this.x, this.y)

/**
 * Creates AnimalTriple with this x and y coordinates
 */
fun AnimalTriple.getPos() = PositionPair(this.x, this.y)

/**
 * Creates OffspringTriple with this x and y coordinates
 */
fun OffspringTriple.getPos() = PositionPair(this.x, this.y)

/**
 * Creates WorkerTriple with this x and y coordinates
 */
fun WorkerTriple.getPos() = PositionPair(this.x, this.y)

/**
 * Creates Pair with this x and y coordinates
 */
fun Pair<Int, Int>.toPos() = PositionPair(this.first, this.second)

/**
 * Creates WorkerTriple depending on which type of coworker should be used
 */
fun Pair<Int, Int>.toWorkerTriple() = when (this) {
    Waterpark.CASH_POS_1, Waterpark.CASH_POS_2 -> WorkerTriple(0, 0, JobEnum.CASHIER)
    Waterpark.ANIM_POS_1, Waterpark.ANIM_POS_2 -> WorkerTriple(0, 0, JobEnum.KEEPER)
    Waterpark.DEPOT_POS -> WorkerTriple(0, 0, JobEnum.MANAGER)
    else -> {
        val pos = NetworkService.localToNetworkCoordinate(this.toPos())
        WorkerTriple(pos.x, pos.y, JobEnum.TRAINER)
    }
}