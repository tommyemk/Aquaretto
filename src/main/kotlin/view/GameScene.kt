package view

import entity.*
import service.RootService
import service.TileLoader
import tools.aqua.bgw.animation.DelayAnimation
import tools.aqua.bgw.components.container.Area
import tools.aqua.bgw.components.container.GameComponentContainer
import tools.aqua.bgw.components.gamecomponentviews.TokenView
import tools.aqua.bgw.components.layoutviews.CameraPane
import tools.aqua.bgw.components.container.LinearLayout
import tools.aqua.bgw.components.gamecomponentviews.GameComponentView
import tools.aqua.bgw.components.layoutviews.GridPane
import tools.aqua.bgw.components.uicomponents.Button
import tools.aqua.bgw.components.uicomponents.ComboBox
import tools.aqua.bgw.components.uicomponents.Label
import tools.aqua.bgw.core.Alignment
import tools.aqua.bgw.core.BoardGameScene
import tools.aqua.bgw.util.BidirectionalMap
import tools.aqua.bgw.util.Font
import tools.aqua.bgw.visual.ColorVisual
import tools.aqua.bgw.visual.CompoundVisual
import tools.aqua.bgw.visual.ImageVisual
import tools.aqua.bgw.visual.SingleLayerVisual
import java.awt.Color
import java.io.File
import javax.swing.text.View

/**
 * The main scene of the Aquaretto game where the action can happen!
 * Depending on the number of players, several [PlayerZone]s are provided, as well as
 * commonly shared game elements such as [Transporter]s and the draw pile.
 * A [CameraPane] will display the current player's [WaterparkView].
 */
class GameScene(private val rootService: RootService) : BoardGameScene(1920, 1080), Refreshable {

    /**
     * First comes the visible stuff:
     */
    //Communication with user
    private val aquaretto = Label(
        posX = 900, posY = 10,
        width = 120, height = 30,
        text = "Aquaretto",
        font = Font(size = 24, color = Color.WHITE, family = "Penguin Attack", fontWeight = Font.FontWeight.BOLD)
    )
    private val notification = Label(
        posX = 630, posY = 10,
        width = 660,
        text = "A Coworker has joined your team!",
        font = Font(size = 24, color = Color.WHITE, family = FAMILY, fontWeight = Font.FontWeight.BOLD)
    ).apply {
        isVisible = false
    }

    private val demand = Demand(posX = 640, posY = 500).apply {
        stop.onMouseClicked = {
            this@apply.isVisible = false
            viewState = ViewState.StartTurn
        }
    }

    //4 Menu Buttons to undo and redo actions, to properly save a game, and to exit without saving
    private val undo = Button(
        posX = 20, posY = 20,
        width = 40, height = 40,
        //text = "Undo",
        visual = ImageVisual("undo.png", 40, 40)
    ).apply {
        onMouseClicked = {
            //rootService.playerActionService.undo()
            notification.text = "not implemented"
            notification.isVisible = true
            aquaretto.isVisible = false
            playAnimation(DelayAnimation(5000).apply {
                onFinished = {
                    notification.isVisible = false
                    aquaretto.isVisible = true
                }
            })
        }
    }
    private val redo = Button(
        posX = 85, posY = 20,
        width = 40, height = 40,
        //text = "Redo",
        visual = ImageVisual("redo.png", 40, 40)
    ).apply {
        onMouseClicked = {
            //rootService.playerActionService.redo()
            notification.text = "not implemented"
            notification.isVisible = true
            aquaretto.isVisible = false
            playAnimation(DelayAnimation(5000).apply {
                onFinished = {
                    notification.isVisible = false
                    aquaretto.isVisible = true
                }
            })
        }
    }
    val save = Button(
        posX = 150, posY = 20,
        width = 40, height = 40,
        visual = ImageVisual("saveIcon.png"),
        //text = "Save Game"
    )
    val exit = Button(
        posX = 205, posY = 20,
        width = 40, height = 40,
        visual = ImageVisual("exit_icon.png")
    ) //GameApplication is in charge of hiding this GameScene
    //Option to change speed
    val speed = ComboBox<Int>(
        posX = 260, posY = 20,
        width = 60, height = 40,
        items = listOf(1, 2, 3)
        //font = Font(size = 16, color = DARK, family = FAMILY)
    ).apply {
        selectedItem = 1
    }

    //Location in the center of the scene where the tiles appear that were 'delivered' by a taken transporter
    private val inventory = LinearLayout<GameComponentView>(
        posX = 805, posY = 520,
        width = 310, height = 90,
        spacing = 20
    ).apply {
        onAdd = { isDraggable = true }
    }

    //Transporter Court for up to five transporters
    private val transporters = GridPane<Transporter>(columns = 5, rows = 1, posX = 315, posY = 390, spacing = 20)

    //Players' personal area
    val playerZones = GridPane<PlayerZone>(columns = 5, rows = 1, posX = 960, posY = 850)

    //5 waterparks which will be accessed by the current player index, and will be displayed partially only
    private var waterparks = emptyList<WaterparkView>()
    private var cameras = renewWaterparks()

    //Location for the draw pile and the actual drawn tile
    private val drawPile = TokenView(
        width = 90, height = 90,
        visual = CompoundVisual(
            TILE,
            SQUARE_HIGHLIGHT
        ),
    ).apply {
        onMouseClicked = {
            draw()
        }
        isDisabledProperty.addListener { _, newValue ->
            println(newValue)
            visual = if (newValue) {
                TILE
            } else {
                CompoundVisual(
                    TILE,
                    SQUARE_HIGHLIGHT
                )
            }
        }
    }

    private val drawSpot = LinearLayout<TokenView>(
        posX = 175, posY = 110,
        width = 290, height = 80,
        spacing = 20,
        alignment = Alignment.CENTER
    )

    var waterparkPlayerExtensions = List(5){
        listOf(
            ExtensionToken(1400, 110, isSmall = true),
            ExtensionToken(1600, 310, isSmall = true),
            ExtensionToken(1600, 110, isSmall = false),
            ExtensionToken(1400, 310, isSmall = false)
        )
    }

    /**
     * Internal administration stuff:
     */

    //Map for placeable entities and their views
    val placeableMap: BidirectionalMap<Placeable, TokenView> = BidirectionalMap()
    private val tokenManager = TokenManager(this)

    //Properties to communicate with the game service and player action service,
    //i.e., arguments matching their methods
    var coordinateMap: MutableMap<Tile, Pair<Int, Int>> = mutableMapOf()
    var index: Int = -1 //seller, transporter or waterpark extension

    //observe the drag-and-drop spectacle
    var inventoryTracker = -1
    var ongoingActionC = false //denotes if Action C is ongoing

    var viewState = ViewState.StartTurn
        set(newValue) {
            field = newValue
            stateLabel.text = newValue.toString()

            val gameState = rootService.currentGame.currentGameState[rootService.currentGame.currentState]
            val player = gameState.players[gameState.currentPlayer]

            when (newValue) {
                ViewState.StartTurn -> {
                    drawPile.isDisabled = !rootService.playerActionService.isAPossible()
                    transporters.forEach { it.component?.enable() }
                    waterparkPlayerExtensions[gameState.currentPlayer].forEach {
                        it.isDisabled = false
                    }
                    playerZones.forEach {
                        it.component?.depotRequest?.isDisabled =
                            !(it.component?.depot?.isNotEmpty() ?: false &&
                                    ((player.numCoins >= 1 && it.component?.playerName == player.name) ||
                                            player.numCoins >= 2)
                                    )
                    }
                }
                ViewState.ActionA,
                ViewState.BuyConfirmation,
                ViewState.DiscardConfirmation -> {
                    drawPile.isDisabled = true
                    transporters.forEach { it.component?.disable() }
                    waterparkPlayerExtensions[gameState.currentPlayer].forEach {
                        it.isDisabled = true
                    }
                    playerZones.forEach { it.component?.depotRequest?.isDisabled = true }
                }
                ViewState.InventoryActionB,
                ViewState.InventorySpecial,
                ViewState.InventoryBuy,
                ViewState.InventoryMove-> {
                    drawPile.isDisabled = true
                    transporters.forEach { it.component?.disable() }
                    waterparkPlayerExtensions[gameState.currentPlayer].forEach {
                        it.isDisabled = true
                    }
                    playerZones.forEach {
                        it.component?.depotRequest?.isDisabled = it.component?.playerName != player.name
                    }
                }
                ViewState.EndTurn -> {
                    refreshAfterEndTurn()
                }
            }
        }


    private val stateLabel = Label(
        posX = 1800,
        posY = 1000
    )
    
    init {
        background = ColorVisual(SCENE)
        drawSpot.add(drawPile)


        addComponents(
            aquaretto, notification,
            undo, redo, save, exit, speed,
            drawSpot,
            inventory,
            demand,
            stateLabel
        )
    }

    /**
     * Several game components can use the following method to inform this game scene about drag-and-drop events.
     * It's now up to the game scene to decide in which way the service layer has to be called.
     * For example, the game scene will report to the service that a coworker has been moved.
     *
     * @param sender the token container which, for instance, has just added a new token
     * @param token the token in question
     */
    fun notify(sender: GameComponentContainer<TokenView>, token: TokenView) {
        val tile = placeableMap.backward(token)
        val gameState = rootService.currentGame.currentGameState[rootService.currentGame.currentState]

        when (viewState) {
            ViewState.InventoryActionB  -> {
                check(tile is AnimalTile)
                if (sender is WaterparkArea) {
                    coordinateMap[tile] = Pair(sender.x, sender.y)
                } else {
                    coordinateMap[tile] = Waterpark.DEPOT_POS
                }
                inventoryTracker--

                if (inventoryTracker == 0) {
                    inventoryTracker = -1
                    rootService.playerActionService.actionB(index, coordinateMap)
                    coordinateMap.clear()

                }
            }
            ViewState.InventoryBuy -> {
                check(tile is AnimalTile)

                val coordinate = if (sender is WaterparkArea) Pair(sender.x, sender.y)
                else Waterpark.DEPOT_POS

                val seller = gameState.players.find { it.depot.firstOrNull()?.id == tile.id }
                checkNotNull(seller) { "No one seems to have the tile you want to buy." }
                rootService.playerActionService.actionCPurchase(seller, coordinate)
            }
            ViewState.InventoryMove -> {
                if (tile is AnimalTile) {
                    val coordinate = if (sender is WaterparkArea) Pair(sender.x, sender.y)
                    else Waterpark.DEPOT_POS

                    rootService.playerActionService.actionCMove(tile, coordinate)
                } else {
                    check(tile is Coworker)
                    if (sender is Station) {
                        val coordCorrection: Int = (sender.components.size * 20)
                        rootService.playerActionService.actionCMove(tile, Pair(sender.x + coordCorrection, 10))
                    } else {
                        check(sender is WaterparkArea)
                        rootService.playerActionService.actionCMove(tile, Pair(sender.x, sender.y))
                    }

                }
            }
            ViewState.InventorySpecial -> {
                if (tile is AnimalTile) {
                    val coordinate = if (sender is WaterparkArea) Pair(sender.x, sender.y)
                    else Waterpark.DEPOT_POS

                    rootService.playerActionService.placeOffspring(coordinate, tile)
                } else {
                    check(tile is Coworker)
                    if (sender is Station) {
                        val coordCorrection: Int = (sender.components.size * 20)
                        rootService.playerActionService.placeCoworker(Pair(sender.x + coordCorrection, 10), tile)
                    } else {
                        check(sender is WaterparkArea)
                        rootService.playerActionService.placeCoworker(Pair(sender.x, sender.y), tile)
                    }
                }
            }
            else -> throw IllegalStateException()
        }
    }

    /**
     * A method that game components will mainly use to inform this game scene about transporter actions.
     * It's now up to the game scene to decide in which way the service layer has to be called.
     *
     * @param sender the token container wanting to notify the game scene about an user interaction
     */
    fun notify(sender: GameComponentContainer<TokenView>) {
        if (viewState == ViewState.ActionA) {
            check(sender is Transporter)
            println("notified by transporter ${sender.index}: loaded")
            rootService.playerActionService.actionA(sender.index)
        }
        //Case 1: A transporter has been taken
        else if (sender is Transporter) {
            check(viewState == ViewState.StartTurn)
            println("notified by transporter ${sender.index}: taken")
            unload(sender)
        }
        //Case 3: A player requests access to a depot
        else if (sender is Area) {
            val state = rootService.currentGame.currentGameState[rootService.currentGame.currentState]

            index = (sender.parent as PlayerZone).index
            println("notified by depot of player $index: someone requested access")

            if (state.currentPlayer == index) {
                val player = state.players[state.currentPlayer]
                viewState = ViewState.DiscardConfirmation
                demand.question.text = "Would you like to discard this tile or move it to your waterpark?"
                demand.yes.text = "Discard (-2 coins)"
                demand.yes.apply {
                    onMouseClicked = {
                        demand.isVisible = false
                        rootService.playerActionService.actionCDiscardTile()
                    }
                }
                    demand.yes.isDisabled = player.numCoins <= 1
                demand.move.apply {
                    onMouseClicked = {
                        viewState = ViewState.InventoryMove
                        val animalToken = playerZones[index, 0]?.pop()
                        if (animalToken != null) { //depot should be disabled anyway
                            inventory.add(animalToken)
                            animalToken.isDraggable = true
                        }
                        demand.isVisible = false
                    }
                }
            } else {
                viewState = ViewState.BuyConfirmation
                demand.question.text = "Would you like to move this animal to your water park? (-2 coins)"
                demand.move.isVisible = false
                demand.yes.text = "Yes"
                demand.yes.isDisabled = false
                demand.yes.apply {
                    onMouseClicked = {
                        val animalToken = playerZones[index, 0]?.pop()
                        if (animalToken != null) { //depot should be disabled anyway
                            inventory.add(animalToken)
                            animalToken.isDraggable = true
                        }
                        demand.isVisible = false
                        viewState = ViewState.InventoryBuy
                    }
                }
            }
            demand.isVisible = true
        }

    }

    fun coworkerClicked(token: CoworkerToken) {
        val gameState = rootService.currentGame.currentGameState[rootService.currentGame.currentState]
        val player = gameState.players[gameState.currentPlayer]
        if (viewState == ViewState.StartTurn && player.numCoins >= 1) {
            viewState = ViewState.InventoryMove
            token.removeFromParent()
            inventory.add(token)
        }
    }

    /**
     * Waterpark Areas (and Extenstion Areas) can request the placement of a tile with the following method.
     * The returned boolean decides whether the dragged tile can be dropped or not.
     */
    fun request(
        sender: GameComponentContainer<TokenView>, token: TokenView?, extension: ExtensionToken? = null
    ): Boolean {
        if (sender is WaterparkArea && token != null) {
            return rootService.playerActionService.isTilePlaceable(
                placeableMap.backward(token),
                Pair(sender.x, sender.y),
                coordinateMap.map { Pair(it.value, it.key) }.toMap()
            )
        } else if (sender is ExtensionArea && extension != null) {
            val waterparkExtension = when (extension.rotation.toInt()) {
                0 -> WaterparkExtension(
                    isSmall = extension.isSmall,
                    x = sender.x, y = sender.y,
                    rotation = 0
                )

                90 -> WaterparkExtension(
                    isSmall = extension.isSmall,
                    x = sender.x - 1, y = sender.y,
                    rotation = 1
                )

                180 -> WaterparkExtension(
                    isSmall = extension.isSmall,
                    x = sender.x - 1, y = sender.y - 1,
                    rotation = 2
                )

                270 -> WaterparkExtension(
                    isSmall = extension.isSmall,
                    x = sender.x, y = sender.y - 1,
                    rotation = 3
                )

                else -> throw IllegalStateException()
            }
            return rootService.playerActionService.isExtensionPossible(waterparkExtension).first
        }
        return false
    }

    fun placeExtension(sender: ExtensionArea, extension: ExtensionToken) {
        val game = rootService.currentGame
        val state = game.currentGameState[game.currentState]
        val player = state.players[state.currentPlayer]

        val entityExtension = player.waterParkExtensionList.find { it.x < 0 && it.y < 0 && it.isSmall == extension.isSmall }

        if(entityExtension != null){
            when (extension.rotation.toInt()){
                0 -> {
                    entityExtension.x = sender.x
                    entityExtension.y = sender.y
                    entityExtension.rotation = 0
                }

                90 -> {
                    entityExtension.x = sender.x - 1
                    entityExtension.y = sender.y
                    entityExtension.rotation = 1
                }

                180 -> {
                    entityExtension.x = sender.x - 1
                    entityExtension.y = sender.y - 1
                    entityExtension.rotation = 2
                }

                270 -> {
                    entityExtension.x = sender.x
                    entityExtension.y = sender.y - 1
                    entityExtension.rotation = 3
                }

                else -> throw IllegalStateException()
            }
        } else {
            throw IllegalStateException("Couldn't find non-placed tile.")
        }
        rootService.playerActionService.actionCExtendWaterPark(entityExtension)
    }

    override fun refreshAfterStartGame() {
        val state = rootService.currentGame.currentGameState[rootService.currentGame.currentState]

        tidyUp()

        waterparkPlayerExtensions = List(5){
            listOf(
                ExtensionToken(1400, 110, isSmall = true),
                ExtensionToken(1600, 310, isSmall = true),
                ExtensionToken(1600, 110, isSmall = false),
                ExtensionToken(1400, 310, isSmall = false)
            )
        }

        waterparkPlayerExtensions.forEach { list ->
            list.forEach {
                addComponents(it)
            }
        }
        for(i in waterparkPlayerExtensions.indices){
            val list = waterparkPlayerExtensions[i]
            list.forEach {
                apply {
                    it.onDragGestureEnded = { _, _ -> index = list.indexOf(it) }
                    it.isVisible = (i == 0)
                }
            }
        }

        //assign each player a seat and provide transporters depending on number of players
        val players = state.players
        for (i in players.indices) {
            playerZones[i, 0] = PlayerZone(playerName = players[i].name, targetSpot = inventory, index = i, scene = this)
            transporters[i, 0] = Transporter(index = i, scene = this)
        }
        if (players.size == 2) {
            transporters[0, 0]?.apply { cap = 1 }
            transporters[1, 0]?.apply { cap = 2 }
            transporters[2, 0] = Transporter(index = 2, scene = this)
        }
        transporters.forEach {
            it.component?.apply {
                isDraggable = false //they will be draggable after being loaded
                onDragGestureEnded =
                    { _, _ -> this@GameScene.index = this.index } //index that will be reported to service
            }
        }
        playerZones[0, 0]?.highlight?.isVisible = true
        addComponents(playerZones, transporters, cameras[0])

        //while preparing the draw pile, map the tiles to their token views
        val pile = state.board.mainPile + state.board.finalPile
        for (tile in pile) {
            if (tile is AnimalTile) {
                placeableMap.add(tile, tokenManager.tokenForTile(tile))
            }
        }
        viewState = ViewState.StartTurn
        if (state.players[state.currentPlayer].playerType == PlayerType.LOCAL_HUMAN) {
            unlock()
        } else {
            lock()
        }
    }

    override fun refreshAfterNextPlayer() {
        val state = rootService.currentGame.currentGameState[rootService.currentGame.currentState]

        val currentPlayer = state.currentPlayer
        println("refresh after next player $currentPlayer")
        val placeables = state.players[currentPlayer].waterPark.fieldMap.values
        val animals = placeables.filterIsInstance<AnimalTile>()
        playerZones[currentPlayer, 0]?.update(
            groups = animals.groupingBy { it.animalType }.eachCount().toList(),
            numCoworkers = placeables.size - animals.size,
            numCoins = state.players[currentPlayer].numCoins
        )
        playerZones[currentPlayer, 0]?.transporterZone?.isDisabled = false
        addComponents(cameras[currentPlayer])

        if (state.players[currentPlayer].playerType == PlayerType.LOCAL_HUMAN) {
            unlock()
        } else {
            lock()
        }
        viewState = ViewState.StartTurn

        for(i in waterparkPlayerExtensions.indices){
            val list = waterparkPlayerExtensions[i]
            for(j in list.indices){
                apply {
                    waterparkPlayerExtensions[i][j].isVisible = (i == currentPlayer
                            && state.players[i].waterParkExtensionList[j].x < 0
                            && state.players[i].waterParkExtensionList[j].y < 0)
                }
            }
        }
    }

    override fun refreshAfterActionA(transporterIndex: Int) {
        val state = rootService.currentGame.currentGameState[rootService.currentGame.currentState]

        val currentPlayer = state.currentPlayer
        if (state.players[currentPlayer].playerType != PlayerType.LOCAL_HUMAN) {
            val items = mutableListOf<TokenView>()
            state.board.transporters[transporterIndex].tiles.forEach {
                if (it is AnimalTile) {
                    items.add(placeableMap.forward(it))
                } else {
                    items.add(tokenManager.tokenForTile(it))
                }
            }
            transporters[transporterIndex, 0]?.addAll(items)
        }
        viewState = ViewState.EndTurn
    }

    override fun refreshAfterActionB(transporterIndex: Int, coordinatesMap: Map<Tile, Pair<Int, Int>>) {
        val state = rootService.currentGame.currentGameState[rootService.currentGame.currentState]

        val currentPlayer = state.currentPlayer
        val currentWaterpark = waterparks[currentPlayer]
        val player = state.players[currentPlayer]

        if (player.playerType != PlayerType.LOCAL_HUMAN) {
            val tileList = coordinatesMap.keys.toMutableList()

            transporters[transporterIndex, 0]?.unload()

            for (tile in tileList) {
                val tokenView = placeableMap.forward(tile)
                val xIndex = coordinatesMap[tile]!!.second
                val yIndex = coordinatesMap[tile]!!.first
                currentWaterpark[xIndex, yIndex]?.add(tokenView)

            }
        }
        if (player.coworkersToPlace > 0 || player.offspringsToPlace.values.sum() > 0) {
            handleSpecialSituation()
        } else {
            viewState = ViewState.EndTurn
        }
    }

    private fun handleSpecialSituation() {
        val state = rootService.currentGame.currentGameState[rootService.currentGame.currentState]
        val currentPlayer = state.currentPlayer
        val player = state.players[currentPlayer]

        if (!(player.coworkersToPlace > 0 || player.offspringsToPlace.values.sum() > 0)) {
            return
        }

        viewState = ViewState.InventorySpecial
        inventory.clear()
        repeat(player.coworkersToPlace) {
            val coworker = Coworker(coworkerTask = CoworkerTask.CASHIER)
            placeableMap.add(Pair(coworker, tokenManager.tokenForCoworker()))
            inventory.add(placeableMap.forward(coworker))
        }
        player.offspringsToPlace.forEach { (animal, amount) ->
            repeat(amount) {
                val offspring = state.board.offspring.find { it.isOffspring && it.animalType == animal }
                checkNotNull(offspring) { "There are no offsprings of this animal type left. (this shouldn't happen)" }

                placeableMap.add(offspring, tokenManager.tokenForTile(offspring))
                inventory.add(placeableMap.forward(offspring))
            }
        }
    }

    override fun refreshAfterSpecialPlaced() {
        val state = rootService.currentGame.currentGameState[rootService.currentGame.currentState]
        val currentPlayer = state.currentPlayer
        val player = state.players[currentPlayer]
        if (player.coworkersToPlace > 0 || player.offspringsToPlace.values.sum() > 0) {
            handleSpecialSituation()
        } else {
            viewState = ViewState.EndTurn
        }
    }

    override fun refreshAfterCDiscardTile() {
        val state = rootService.currentGame.currentGameState[rootService.currentGame.currentState]
        playerZones[state.currentPlayer, 0]?.pop()
        viewState = ViewState.EndTurn
    }

    override fun refreshAfterCExtendWaterPark(waterParkExtension: WaterparkExtension) {

        val state = rootService.currentGame.currentGameState[rootService.currentGame.currentState]
        val currentPlayer = state.currentPlayer
        val currentWaterpark = waterparks[currentPlayer]
        val listOfCoordinates = rootService.playerActionService.isExtensionPossible(waterParkExtension).second

        // update the zone of the player who just bought an extension -Marcel
        val placeables = state.players[currentPlayer].waterPark.fieldMap.values
        val animals = placeables.filterIsInstance<AnimalTile>()
        playerZones[currentPlayer, 0]?.update(
            groups = animals.groupingBy { it.animalType }.eachCount().toList(),
            numCoworkers = placeables.size - animals.size,
            numCoins = state.players[currentPlayer].numCoins
        )

        currentWaterpark.extend(listOfCoordinates)
        print("reached")

        viewState = ViewState.EndTurn
    }

    override fun refreshAfterRoundEnds() {
        transporters.forEach { it.component?.release() }
        this.draggedComponent?.let {
            if (it is Transporter) {
                it.release()
            }
        }
    }

    private fun refreshAfterEndTurn() {
        val gameState = rootService.currentGame.currentGameState[rootService.currentGame.currentState]

        val currentPlayer = gameState.currentPlayer
        println("Player $currentPlayer ended his/her turn")
        playerZones[currentPlayer, 0]?.open(false)
        playerZones[currentPlayer, 0]?.highlight?.isVisible = false
        playerZones[currentPlayer, 0]?.update(gameState.players[currentPlayer].numCoins)
        removeComponents(cameras[currentPlayer])
    }

    private fun tidyUp() {
        //visible components
        inventory.clear()
        transporters.forEach { it.component?.clear() }
        notification.isVisible = false
        removeComponents(transporters, playerZones)
        //internal stuff
        placeableMap.clear()
        coordinateMap.clear()
        waterparkPlayerExtensions.forEach { it.forEach { removeComponents(it) } }
        cameras.forEach { removeComponents(it) }
        cameras = renewWaterparks()
        index = -1
        inventoryTracker = -1
    }

    override fun refreshAfterActionCMove(tile: Placeable, coordinates: Pair<Int, Int>) {
        val state = rootService.currentGame.currentGameState[rootService.currentGame.currentState]

        val currentPlayer = state.currentPlayer
        val player = state.players[currentPlayer]

        val currentWaterpark = waterparks[currentPlayer]

        if (player.playerType != PlayerType.LOCAL_HUMAN) {
            // Removes tile from GUI
            val tokenView = placeableMap.forward(tile)
            tokenView.removeFromParent()

            // Places tile where the player placed in on the gui
            val xIndex = coordinates.second
            val yIndex = coordinates.first
            currentWaterpark[xIndex, yIndex]?.add(tokenView)
        }
        refreshAfterEndTurn()
    }

    override fun refreshAfterActionCPurchase(seller: Player, coordinates: Pair<Int, Int>) {
        val state = rootService.currentGame.currentGameState[rootService.currentGame.currentState]
        val currentPlayer = state.currentPlayer
        val player = state.players[currentPlayer]

        if (player.playerType != PlayerType.LOCAL_HUMAN) {
            //removing purchased Tile from GUI
            //using refreshAfterCMove for placement
            val purchase = seller.depot.last()
            val purchaseView = placeableMap.forward(purchase)
            purchaseView.removeFromParent()

            refreshAfterActionCMove(purchase,coordinates)
        }

        if (player.coworkersToPlace > 0 || player.offspringsToPlace.values.sum() > 0) {
            handleSpecialSituation()
        } else {
            viewState = ViewState.EndTurn
        }
    }

    /*override fun refreshAfterActionCPurchase(seller: Player, coordinates: Pair<Int, Int>) {
        val state = rootService.currentGame.currentGameState[rootService.currentGame.currentState]

        val currentPlayer = state.currentPlayer
        val player = state.players[currentPlayer]

        if (player.playerType != PlayerType.LOCAL_HUMAN) {
            //removing purchased Tile from GUI
            //using refreshAfterCMove for placement
            val purchase = seller.depot.last()
            val purchaseView = placeableMap.forward(purchase)
            purchaseView.removeFromParent()

            refreshAfterActionCMove(purchase,coordinates)
        }

        refreshAfterEndTurn()   //still needed bc is in refreshAfterActionCMove() included?
    }*/

    private fun renewWaterparks(): List<CameraPane<WaterparkView>> {
        waterparks = listOf(
            WaterparkView(this),
            WaterparkView(this),
            WaterparkView(this),
            WaterparkView(this),
            WaterparkView(this)
        )
        val cameras = listOf(
            CameraPane(posX = 630, posY = 60, width = 660, height = 440, target = waterparks[0]),
            CameraPane(posX = 630, posY = 60, width = 660, height = 440, target = waterparks[1]),
            CameraPane(posX = 630, posY = 60, width = 660, height = 440, target = waterparks[2]),
            CameraPane(posX = 630, posY = 60, width = 660, height = 440, target = waterparks[3]),
            CameraPane(posX = 630, posY = 60, width = 660, height = 440, target = waterparks[4])
        )
        cameras.forEach {
            it.apply {
                interactive = true
            }
        }
        return cameras
    }

    private fun unload(container: Transporter) {
        val state = rootService.currentGame.currentGameState[rootService.currentGame.currentState]
        //Clearing the transporter before adding its load elsewhere
        container.unload()
        //Fetch the transporter from the entity layer
        val transporter = state.board.transporters[container.index]
        var numCoins = 0
        transporter.tiles.forEach {
            if (it is AnimalTile) {
                inventory.add(placeableMap.forward(it).apply { scale(1.0) })
            } else {
                numCoins++
            }
        }
        inventoryTracker = inventory.components.size
        viewState = ViewState.InventoryActionB

        //Maybe there was no new animal to be placed at all
        if (inventoryTracker == 0) {
            refreshAfterEndTurn()
            inventoryTracker = -1
            coordinateMap.clear()
            rootService.playerActionService.actionB(container.index, coordinateMap)
        }
    }

    private fun reportServiceCMove(tile: Placeable, coords: Pair<Int, Int>) {
        //val state = rootService.currentGame.currentGameState[rootService.currentGame.currentState]

        ongoingActionC = false
        rootService.playerActionService.actionCMove(tile, coords)
    }

    private fun enableActionB(e: Boolean = true) {
        transporters.forEach { it.component?.isDraggable = e }
    }

    private fun draw() {
        check(viewState == ViewState.StartTurn)
        viewState = ViewState.ActionA

        val state = rootService.currentGame.currentGameState[rootService.currentGame.currentState]

        val pile = state.board.mainPile + state.board.finalPile
        if (pile.isNotEmpty()) {
            val tile = pile[0]
            val token = if (tile is CoinTile) tokenManager.tokenForTile(tile)
            else placeableMap.forward(tile)
            drawSpot.add(token.apply {
                scale(1.3)
                if (this.visual is SingleLayerVisual) {
                    visual = CompoundVisual(this.visual as SingleLayerVisual, SQUARE_HIGHLIGHT)
                }
            })
        }
    }

    private fun protoTiles(): List<TokenView> {
        val tiles = listOf(
            AnimalTile(2, animalType = Animal.ORCA),
            AnimalTile(60, animalType = Animal.HIPPO),
            CoinTile(10)
        )
        val tokens = mutableListOf<TokenView>()
        for (tile in tiles) {
            val token = tokenManager.tokenForTile(tile)
            tokens.add(token)
            if (tile is AnimalTile) {
                placeableMap.add(Pair(tile, token))
            }
        }
        val token = tokenManager.tokenForCoworker()
        placeableMap.add(Pair(Coworker(coworkerTask = CoworkerTask.CASHIER), token))
        return tokens.toList()
    }

    /**
     * Set up the loaded game
     */
    override fun refreshAfterLoadGame() {
        val state = rootService.currentGame.currentGameState[rootService.currentGame.currentState]

        tidyUp()
        //assign each player a seat and provide transporters depending on number of players
        viewState = ViewState.StartTurn
        val players = state.players
        for (i in players.indices) {
            playerZones[i, 0] = PlayerZone(playerName = players[i].name, targetSpot = inventory, index = i, scene = this)
            transporters[i, 0] = Transporter(index = i, scene = this)
        }
        if (players.size == 2) {
            transporters[0, 0]?.apply { cap = 1 }
            transporters[1, 0]?.apply { cap = 2 }
            transporters[2, 0] = Transporter(index = 2, scene = this)
        }

        // load transporters
        for(i in state.board.transporters.indices){
            val transporter = state.board.transporters[i]
            transporter.apply {
                transporters[i, 0]?.isDraggable = !transporter.taken && state.board.transporters[i].tiles.isNotEmpty()
                if(transporter.taken){
                    transporters[i, 0]?.unload()
                }else if(transporter.tiles.isEmpty()){
                    transporters[i, 0]?.clear()
                }
                transporters[i, 0]?.onDragGestureEnded =  { _, _ -> this@GameScene.index = i }

                for (j in 0 until transporter.tiles.size) {
                    val token = tokenManager.tokenForTile(transporter.tiles[j])
                    token.apply {
                        scale(0.8)
                        isDraggable = false
                    }
                    transporters[i, 0]?.add(token)
                    placeableMap.add(Pair(transporter.tiles[j], token))
                }
            }
        }

        playerZones[state.currentPlayer, 0]?.highlight?.isVisible = true
        addComponents(playerZones, transporters)

        //while preparing the draw pile, map the tiles to their token views
        val pile = state.board.mainPile + state.board.finalPile
        for (tile in pile) {
            if (tile is AnimalTile) {
                placeableMap.add(tile, tokenManager.tokenForTile(tile))
            }
        }

        //extensions
        waterparkPlayerExtensions = List(5){
            listOf(
                ExtensionToken(1400, 110, isSmall = true),
                ExtensionToken(1600, 310, isSmall = true),
                ExtensionToken(1600, 110, isSmall = false),
                ExtensionToken(1400, 310, isSmall = false)
            )
        }

        waterparkPlayerExtensions.forEach { list ->
            list.forEach {
                addComponents(it)
            }
        }
        for(i in waterparkPlayerExtensions.indices){
            val list = waterparkPlayerExtensions[i]
            list.forEach {
                apply {
                    it.onDragGestureEnded = { _, _ -> index = list.indexOf(it) }
                    it.isVisible = (i == state.currentPlayer)
                }
            }
        }

        for(i in waterparkPlayerExtensions.indices){
            val list = waterparkPlayerExtensions[i]
            for(j in list.indices){
                apply {
                    waterparkPlayerExtensions[i][j].isVisible =
                        (i == state.currentPlayer && state.players[i].waterParkExtensionList[j].x < 0
                                && state.players[i].waterParkExtensionList[j].y < 0)
                }
            }
        }


        // load waterparks and depots
        for (i in 0 until players.size) {
            val initialExtensionAreas = listOf(Pair(9, 8), Pair(10, 8), Pair(11, 8),
                Pair(8, 9), Pair(9, 9), Pair(10, 9), Pair(11, 9), Pair(12, 9),
                Pair(8, 10), Pair(9, 10), Pair(10, 10), Pair(11, 10), Pair(12, 10),
                Pair(9, 11), Pair(10, 11), Pair(11, 11), Pair(12, 11), Pair(10, 12), Pair(11, 12),
                Waterpark.CASH_POS_1, Waterpark.CASH_POS_2, Waterpark.ANIM_POS_1, Waterpark.ANIM_POS_2,
                Waterpark.DEPOT_POS
            )
            for(pair in players[i].waterPark.allowedExtensionList){
                if(pair !in initialExtensionAreas){
                    waterparks[i][pair.first, pair.second] = WaterparkArea(x = pair.first, y = pair.second, scene = this)
                }
            }

            val fieldMap = players[i].waterPark.fieldMap
            for (tile in fieldMap.values) {
                val tokenView = if (tile is AnimalTile) {
                    tokenManager.tokenForTile(tile)
                } else {
                    tokenManager.tokenForCoworker()
                }
                val coordinate = fieldMap.entries.filter { it.value == tile }[0].key
                waterparks[i][coordinate.first, coordinate.second]?.add(tokenView.apply { reposition(5, 5) })
                tokenView.isDraggable = false
                placeableMap.add(Pair(tile, tokenView))
            }

            for(tile in players[i].depot){
                val tokenView = tokenManager.tokenForTile(tile)
                playerZones[i, 0]?.depot?.add(tokenView.apply { reposition(5, 5) }, 0)
                tokenView.isDraggable = false
                placeableMap.add(Pair(tile, tokenView))
            }
        }
        addComponents(cameras[state.currentPlayer])
    }

    /**
     * Undo visuals
     */
    override fun refreshAfterUndo() {
        //val game = rootService.currentGame
        //val state = game.currentGameState[game.currentState]
    }
}