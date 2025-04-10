package service

import entity.*

/**
 * This class is responsible for handling the player's actions
 * @param [rootService] the root service
 */
class PlayerActionService(private val rootService: RootService) : AbstractRefreshingService() {

    /**
     * attaches new state
     */
    private fun attachNewState(newState: AquarettoGameState) {
        val game = rootService.currentGame

        // Undo/Redo shouldn't work in online games
        if (game.isOnlineGame) {
            game.currentGameState[0] = newState
            game.currentState = 0
        }

        //checking if there are moves that could be redone and remove them
        if (game.currentState != (game.currentGameState.size - 1)) {
            var i = game.currentGameState.size - 1
            while (i != game.currentState) {
                game.currentGameState.removeAt(i)
                i--
            }
        }
        //adding the new state to list
        game.currentGameState.add(newState)
        game.currentState++
    }


    /**
     * Action A: Places one tile on top of one transporter which is selected by the player.
     */
    fun actionA(transporterIndex: Int) {
        val game = rootService.currentGame
        val currentGameState = game.currentGameState[game.currentState]
        //copying gameState
        val newState = currentGameState.clone()
        val player = newState.players[newState.currentPlayer]

        // Hier pruefen wir ob der uebergebene Transporter Platz hat
        if (!isAPossible(transporterIndex)) {
            throw IllegalStateException("All transporters are full!")
        }

        if (newState.board.mainPile.isNotEmpty()) {
            newState.board.transporters[transporterIndex].tiles.add(
                newState.board.mainPile.removeFirst()
            )
        } else {
            newState.finalRound = true
            newState.board.transporters[transporterIndex].tiles.add(
                newState.board.finalPile.removeFirst()
            )
        }

        attachNewState(newState)

        if (game.isOnlineGame && player.playerType != PlayerType.ONLINE) {
            rootService.networkService.sendAddTileToTruck(transporterIndex)
        }

        onAllRefreshables { refreshAfterActionA(transporterIndex) }
        rootService.aquarettoGameService.nextPlayer()
    }

    /**
     * Checks whether the transporter index is valid and if the transporter at
     * the [transporterIndex] is usable and has one space left.
     *
     * @return whether the transporter is usable
     */
    private fun isAPossible(transporterIndex: Int): Boolean {
        val game = rootService.currentGame
        val currentGameState = game.currentGameState[game.currentState]
        val transp = currentGameState.board.transporters

        return transporterIndex in transp.indices && !transp[transporterIndex].taken
                && transp[transporterIndex].tiles.size < transp[transporterIndex].tilesCapacity
    }

    /**
     * Checks whether any transporter can be taken
     */
    fun isAPossible(): Boolean {
        val game = rootService.currentGame
        val currentGameState = game.currentGameState[game.currentState]
        val transp = currentGameState.board.transporters

        return transp.indices.map { isAPossible(it) }.contains(true)
    }

    /**
     * Action B: The current player takes one transporter with all it's
     * tiles and therefore exits the current round.
     */
    fun actionB(transporterIndex: Int, coordinatesMap: Map<Tile, Pair<Int, Int>>) {
        val game = rootService.currentGame
        val currentGameState = game.currentGameState[game.currentState]
        //copying state
        val newState = currentGameState.clone()
        val player = newState.players[newState.currentPlayer]

        // Hier pruefen wir ob der uebergebene Transporter Platz hat
        if (!isBPossible(transporterIndex)) {
            throw IllegalStateException("B: Invalid transporterIndex, transporter already taken or transporter empty!")
        }

        val transporter = newState.board.transporters[transporterIndex]

        val networkCoordinateMap = mutableMapOf<Int, Pair<Int, Int>>()

        for ((i, tile) in transporter.tiles.withIndex()) {
            if (tile !is CoinTile) {
                coordinatesMap[tile]?.let {
                    networkCoordinateMap[i] = it
                }
                if (coordinatesMap[tile] == Waterpark.DEPOT_POS) {
                    player.depot.add(0, tile)
                } else {
                    coordinatesMap[tile]?.let {
                        networkCoordinateMap[i] = it
                    }
                    check(tile in coordinatesMap.keys) { "Not every animal tile on the transporter placed" }
                }                                                 // if placeTile fails, already placed
            } else {                                              // tiles and coins will remain placed
                player.numCoins++                                 // (unwanted behavior?) -Marcel
            }
        }

        // Place the tiles in the order as they appear in the coordinatesMap to make sure isTilePlaceable
        coordinatesMap.forEach {
            check(it.key in transporter.tiles) { "Trying to place a tile that's not in the transporter" }
            if (it.value != Waterpark.DEPOT_POS) {
                placeTile(it.key, it.value)
            }
        }

        transporter.tiles.clear()
        transporter.taken = true
        player.turnEnded = true // added those flags changes -Marcel

        attachNewState(newState)

        handleSpecialSituation(player, coordinatesMap.keys.filterIsInstance<AnimalTile>())

        if (game.isOnlineGame && player.playerType != PlayerType.ONLINE) {
            rootService.networkService.sendTakeTruck(transporterIndex, networkCoordinateMap)
        }

        onAllRefreshables { refreshAfterActionB(transporterIndex, coordinatesMap) }
        rootService.aquarettoGameService.nextPlayer()
    }

    /**
     * Checks whether the transporter index is valid and if the transporter at
     * the [transporterIndex] has at least one tile.
     *
     * @return whether the player can take the transporter
     */
    private fun isBPossible(transporterIndex: Int): Boolean {
        val game = rootService.currentGame
        val currentGameState = game.currentGameState[game.currentState]
        val transp = currentGameState.board.transporters

        return transporterIndex in transp.indices &&
                !transp[transporterIndex].taken && transp[transporterIndex].tiles.size > 0
    }

    /**
     * Places one tile at a specific index in the waterpark.
     */
    private fun placeTile(tile: Placeable, coordinates: Pair<Int, Int>) {
        val game = rootService.currentGame
        val currentGameState = game.currentGameState[game.currentState]
        if (!isTilePlaceable(tile, coordinates)) {
            throw IllegalStateException("Tile can't be placed!")
        }
        currentGameState.players[currentGameState.currentPlayer].waterPark.fieldMap[coordinates] = tile
    }

    /**
     * Checks whether the tile is able to be placed at the given coordinates.
     *
     * @param notYetPlacedTiles In case of action b multiple tiles can get placed before the entity layer is updatet.
     * This list contains all the not yet placed tiles, to check if the positioning might work
     *
     * @return whether the tile is placeable
     */
    fun isTilePlaceable(
        tile: Placeable,
        coordinates: Pair<Int, Int>,
        notYetPlacedTiles: Map<Pair<Int, Int>, Tile> = mapOf()
    ): Boolean {
        val game = rootService.currentGame
        val currentGameState = game.currentGameState[game.currentState]

        val x = coordinates.first
        val y = coordinates.second
        val listOfNeighbours = listOf(Pair(x + 1, y), Pair(x - 1, y), Pair(x, y + 1), Pair(x, y - 1))
        val notYetPlacedTilesWithoutDepot = notYetPlacedTiles.filter { it.key != Waterpark.DEPOT_POS }

        val waterPark = currentGameState.players[currentGameState.currentPlayer].waterPark

        if (waterPark.fieldMap[coordinates] != null || !waterPark.allowedExtensionList.contains(coordinates)) {
            return false
        }

        if (tile is AnimalTile) {
            // number of different animal types in a waterpark is limited, thus check if the number of tiles is
            // below the limit
            val animalTypes = waterPark.fieldMap.values.filterIsInstance<AnimalTile>().groupBy { it.animalType }
            if (tile.animalType !in animalTypes && animalTypes.size >= waterPark.allowedAnimalType) {
                return false
            }

            // make sure AnimalTile can't be placed on a coworker-Space
            val coworkerPlaces = listOf(
                Waterpark.CASH_POS_1,
                Waterpark.CASH_POS_2,
                Waterpark.ANIM_POS_1,
                Waterpark.ANIM_POS_2,
                Waterpark.DEPOT_POS
            )

            if (coordinates in coworkerPlaces) {
                return false
            }

            var hasNeighbour = false
            for (neighbour in listOfNeighbours) {
                var neighbourTile = waterPark.fieldMap[neighbour]
                if (neighbourTile == null) {
                    neighbourTile = notYetPlacedTilesWithoutDepot[neighbour]
                }

                // nicht neben anderes Tier legen
                if (neighbourTile is AnimalTile && neighbourTile.animalType != tile.animalType) {
                    return false
                }

                if (neighbourTile is AnimalTile) {
                    hasNeighbour = true
                }
            }

            // nicht gleiches Tier weit weg platzieren
            var fieldContainsAnimal = false
            for (aniTile in waterPark.fieldMap.values) {
                if (aniTile is AnimalTile && aniTile.animalType == tile.animalType) {
                    fieldContainsAnimal = true
                }
            }
            for (aniTile in notYetPlacedTilesWithoutDepot.values) {
                if (aniTile is AnimalTile && aniTile.animalType == tile.animalType) {
                    fieldContainsAnimal = true
                }
            }

            if (fieldContainsAnimal && !hasNeighbour) {
                return false
            }
        }
        return true
    }

    /**
     * Moves top tile from depot or moves any coworker by the cost of one coin.
     */
    fun actionCMove(tile: Placeable, coordinates: Pair<Int, Int>) {
        val game = rootService.currentGame
        val currentGameState = game.currentGameState[game.currentState]
        //copying gameState
        val newState = currentGameState.clone()
        val player = newState.players[newState.currentPlayer]
        val waterPark = newState.players[newState.currentPlayer].waterPark

        check(player.numCoins > 0) { "Not enough money" }

        var startPos: Pair<Int, Int>? = null // Start pos in case it's a coworker for network purposes
        this.placeTile(tile, coordinates) // after that, tile is in 2 different places

        if (tile is Coworker) {

            val tileKey = waterPark.fieldMap.entries.find { it.value == tile && it.key != coordinates }?.key
            startPos = tileKey
            waterPark.fieldMap.remove(tileKey)

            waterPark.fieldMap.remove(tileKey)                    // make sure we only remove the position
            // where the Coworker came from -Marcel
            // Coworker profession needs to be changed -Marcel
            when (coordinates) {
                Waterpark.CASH_POS_1 -> tile.coworkerTask = CoworkerTask.CASHIER
                Waterpark.CASH_POS_2 -> tile.coworkerTask = CoworkerTask.CASHIER
                Waterpark.ANIM_POS_1 -> tile.coworkerTask = CoworkerTask.ANIMAL_KEEPER
                Waterpark.ANIM_POS_2 -> tile.coworkerTask = CoworkerTask.ANIMAL_KEEPER
                Waterpark.DEPOT_POS -> tile.coworkerTask = CoworkerTask.MANAGER
                else -> tile.coworkerTask = CoworkerTask.TRAINER // every other possible placeable tile is just
            }                                                    // normal waterpark territory
        } else {
            player.depot.removeFirst()
        }

        // this.placeTile(tile, coordinates)

        // Großes Problem: das Tile auf dem Depot wurde zuerst entfernt und dann platziert.
        // Wenn das Platzieren fehlschlägt, dann wurde das Tile nicht mehr zurück auf das
        // Depot gelegt. Quickfix: placeTile() wird nun zuerst aufgerufen

        player.numCoins--

        attachNewState(newState)

        if (tile is AnimalTile) {
            handleSpecialSituation(player, listOf(tile))
        }

        if (game.isOnlineGame && player.playerType != PlayerType.ONLINE) {
            if (tile is Coworker && startPos != null) {
                rootService.networkService.sendMoveCoworker(startPos, coordinates)
            } else {
                rootService.networkService.sendMoveTile(coordinates, null)
            }
        }

        onAllRefreshables { refreshAfterActionCMove(tile, coordinates) }
        rootService.aquarettoGameService.nextPlayer()
    }

    /**
     * Player buys one tile from another player by the cost of 2 coins.
     */
    fun actionCPurchase(seller: Player, coordinates: Pair<Int, Int>) {
        val game = rootService.currentGame
        val currentGameState = game.currentGameState[game.currentState]

        //copying state
        val newState = currentGameState.clone()
        val player = newState.players[newState.currentPlayer]

        //Added check to see if the seller and buyer are the same -Marcel
        check(player.name != seller.name) { "Player can't sell tile to themselves" }
        check(player.numCoins >= 2) { "Not enough money" }

        val sellerTile = seller.depot.first() // changed this because before my change, the first tile was removed
        this.placeTile(sellerTile, coordinates) // before it is placed, so if placing it failed, it still
        seller.depot.removeFirst() // was removed from the depot and disappeared completely (wrong behavior)
        // now it gets removed after it gets placed on the waterpark
        player.numCoins -= 2
        val newSeller = newState.players.find { it.name == seller.name }
        checkNotNull(newSeller)
        newSeller.numCoins++

        attachNewState(newState)

        handleSpecialSituation(player, listOf(sellerTile as AnimalTile))

        if (game.isOnlineGame && player.playerType != PlayerType.ONLINE) {
            rootService.networkService.sendMoveTile(coordinates, seller.name)
        }

        onAllRefreshables { refreshAfterActionCPurchase(seller, coordinates) }
        rootService.aquarettoGameService.nextPlayer()
    }

    /**
     * Player removes the top tile from his depot.
     */
    fun actionCDiscardTile() {
        val game = rootService.currentGame
        val currentGameState = game.currentGameState[game.currentState]
        //copying state
        val newState = currentGameState.clone()
        val player = newState.players[newState.currentPlayer]

        check(player.numCoins >= 2) { "Not enough money" }

        player.depot.removeFirst()

        player.numCoins -= 2

        attachNewState(newState)

        if (game.isOnlineGame && player.playerType != PlayerType.ONLINE) {
            rootService.networkService.sendDiscard()
        }

        onAllRefreshables { refreshAfterCDiscardTile() }
        rootService.aquarettoGameService.nextPlayer()
    }

    fun isExtensionPossible(waterParkExtension: WaterparkExtension): Pair<Boolean, List<Pair<Int, Int>>> {
        val game = rootService.currentGame
        val currentGameState = game.currentGameState[game.currentState]
        val player = currentGameState.players[currentGameState.currentPlayer]
        val waterPark = currentGameState.players[currentGameState.currentPlayer].waterPark


        val x = waterParkExtension.x
        val y = waterParkExtension.y
        var coordsList: List<Pair<Int, Int>> = listOf()

        if (waterParkExtension.isSmall) {
            coordsList = when (waterParkExtension.rotation) {
                0 -> listOf(Pair(x, y), Pair(x, y + 1), Pair(x + 1, y + 1))
                1 -> listOf(Pair(x, y), Pair(x + 1, y), Pair(x, y + 1))         //90
                2 -> listOf(Pair(x, y), Pair(x + 1, y), Pair(x + 1, y + 1))     //180
                3 -> listOf(Pair(x + 1, y), Pair(x, y + 1), Pair(x + 1, y + 1)) //270
                else -> throw IllegalStateException()
            }

            if (player.numCoins < 1) {
                return Pair(false, coordsList)
            }

        } else {
            coordsList = listOf(Pair(x, y), Pair(x + 1, y), Pair(x, y + 1), Pair(x + 1, y + 1))

            if (player.numCoins < 2) {
                return Pair(false, coordsList)
            }
        }

        var hasNeighbour = false
        for (coords in coordsList) {

            if (coords in player.waterPark.allowedExtensionList) {
                return Pair(false, coordsList)
            }

            val coordsNeighbours = listOf(
                Pair(coords.first + 1, coords.second),
                Pair(coords.first - 1, coords.second),
                Pair(coords.first, coords.second + 1),
                Pair(coords.first, coords.second - 1)
            )

            for (neighbour in coordsNeighbours) {
                if (waterPark.allowedExtensionList.contains(neighbour)) {
                    hasNeighbour = true
                }
            }
        }

        return Pair(hasNeighbour, coordsList)
    }

    /**
     * Player extends his waterpark by the use of a small or big extension
     * and at the cost of 1 or 2 coins depending on the size of the extension.
     */
    fun actionCExtendWaterPark(waterParkExtension: WaterparkExtension) {
        // Rotation auf Mausklick mittels modulo
        val game = rootService.currentGame
        val currentGameState = game.currentGameState[game.currentState]
        //copying state
        val newState = currentGameState.clone()
        val player = newState.players[newState.currentPlayer]
        val waterPark = newState.players[newState.currentPlayer].waterPark

        val (extensionPossible, coordsList) = isExtensionPossible(waterParkExtension)

        if (extensionPossible) {
            waterPark.allowedExtensionList.addAll(coordsList)
        } else {
            throw IllegalArgumentException("Extension can't be placed") // added this Exception
        }

        if (waterParkExtension.isSmall) {
            player.numCoins--
        } else {
            player.numCoins -= 2
            player.waterPark.allowedAnimalType++ // one more animal-type allowed
        }

        attachNewState(newState)

        if (game.isOnlineGame && player.playerType != PlayerType.ONLINE) {
            rootService.networkService.sendBuyExpansion(waterParkExtension)
        }

        onAllRefreshables { refreshAfterCExtendWaterPark(waterParkExtension) }
        rootService.aquarettoGameService.nextPlayer()
    }

    /**
     * method to check if special situation with offspring occurs
     *
     * checks if two animals of same type and opposite sex are placed on map
     *      returns true if so to signal that offspring is released
     *      returns false elsewise
     */
    private fun specialSituationOffspring(player: Player, placedAnimalTypes: Set<Animal>): MutableMap<Animal, Int> {

        val park = player.waterPark
        val fieldMap = park.fieldMap

        val returnList = mutableMapOf<Animal, Int>()

        fieldMap
            .values
            .filterIsInstance<AnimalTile>()
            .filter {
                it.animalType in placedAnimalTypes && it.isFemale && !it.hasOffspring
            }
            .forEach { femaleAnimal ->
                val maleAnimal = fieldMap
                    .values
                    .filterIsInstance<AnimalTile>()
                    .find {
                        it.animalType == femaleAnimal.animalType && it.isMale && !it.hasOffspring
                    }

                if (maleAnimal != null) {
                    femaleAnimal.hasOffspring = true
                    maleAnimal.hasOffspring = true
                    returnList[femaleAnimal.animalType] = returnList.getOrDefault(femaleAnimal.animalType, 0) + 1
                }
            }
        return returnList
    }

    /**
     * @param placedAnimals the animal that got placed this round (we only need to check these types)
     */
    private fun handleSpecialSituation(player: Player, placedAnimals: List<AnimalTile>) {
        val offsprings = specialSituationOffspring(player, placedAnimals.map { it.animalType }.toSet())
        offsprings.forEach { (key, value) ->
            player.offspringsToPlace.merge(key, value) { old, new ->
                old + new
            }
        }

        player.coworkersToPlace += specialSituationCoworkerAndCoins(player, placedAnimals)
    }


    /**
     * places offspring ...
     */
    fun placeOffspring(coordinates: Pair<Int, Int>, offspring: AnimalTile) {
        val gameState = rootService.currentGame.currentGameState[rootService.currentGame.currentState]
        val player = gameState.players[gameState.currentPlayer]

        val offspringsToPlaceOfThisType = player.offspringsToPlace.getOrDefault(offspring.animalType, 0)

        check(offspringsToPlaceOfThisType > 0) {
            "You didn't have this offspring"
        }

        if (!isTilePlaceable(offspring, coordinates)) {
            throw IllegalStateException("Tile can't be placed!")
        }
        placeTile(offspring, coordinates)
        gameState.board.offspring.remove(offspring)

        player.offspringsToPlace[offspring.animalType] = offspringsToPlaceOfThisType - 1

        handleSpecialSituation(player, listOf(offspring))

        onAllRefreshables { refreshAfterSpecialPlaced() }

        if (rootService.currentGame.isOnlineGame && player.playerType != PlayerType.ONLINE) {
            rootService.networkService.sendPlaceOffspring(coordinates, offspring)
        }
        rootService.aquarettoGameService.nextPlayer()
    }

    /**
     * method to check if special situation with coworker occurs
     *
     * checks map if animalCount is 5 or 10
     * if so returns true
     * otherwise returns false
     */
    private fun specialSituationCoworkerAndCoins(player: Player, placedAnimals: List<AnimalTile>): Int {
        val park = player.waterPark
        val fieldmap = park.fieldMap
        var newCoworkerNum = 0

        placedAnimals.map { it.animalType }.toSet().forEach { animal ->
            val newCount = fieldmap.values.count { it is AnimalTile && it.animalType == animal }
            val previousCount = newCount - placedAnimals.count { it.animalType == animal }

            listOf(3, 6, 9, 12).forEach {
                if (newCount >= it && previousCount < it) {
                    player.numCoins++
                }
            }
            listOf(5, 10).forEach {
                if (newCount >= it && previousCount < it) {
                    newCoworkerNum++
                }
            }
        }

        return newCoworkerNum
    }

    /**
     * places coworker
     */
    fun placeCoworker(coordinates: Pair<Int, Int>, coworker: Coworker) {
        val gameState = rootService.currentGame.currentGameState[rootService.currentGame.currentState]
        val player = gameState.players[gameState.currentPlayer]

        check(player.coworkersToPlace > 0) {
            "You didn't have this offspring"
        }

        when (coordinates) {
            Waterpark.CASH_POS_1, Waterpark.CASH_POS_2 -> coworker.coworkerTask = CoworkerTask.CASHIER
            Waterpark.DEPOT_POS -> coworker.coworkerTask = CoworkerTask.MANAGER
            Waterpark.ANIM_POS_1, Waterpark.ANIM_POS_2 -> coworker.coworkerTask = CoworkerTask.ANIMAL_KEEPER
            else -> coworker.coworkerTask = CoworkerTask.TRAINER
        }


        if (!isTilePlaceable(coworker, coordinates)) {
            throw IllegalStateException("Tile can't be placed!")
        }

        placeTile(coworker, coordinates)

        player.coworkersToPlace--

        onAllRefreshables { refreshAfterSpecialPlaced() }

        if (rootService.currentGame.isOnlineGame && player.playerType != PlayerType.ONLINE) {
            rootService.networkService.sendPlaceCoworker(coordinates)
        }
        rootService.aquarettoGameService.nextPlayer()
    }

    /**
     * method to undo a players move
     *
     * checks first, if currentState is first state. if so unable to undo and throws exception
     * checks secondly if currentPlayer would change after an undo. in that case we would
     *      go back to far, so exception is thrown
     * otherwise everything seems okay we decrement currentState Int, so we go back one
     *      index in currentGameState List
     */
    fun undo() {
        val rs = rootService
        val game = rs.currentGame
        //val players = game.currentGameState[game.currentGameState.size-1].players
        //val player = players[game.currentGameState[game.currentGameState.size-1].currentPlayer]
        //val playerAfterUndo = players[game.currentGameState[game.currentGameState.size-2].currentPlayer]

        if (game.currentState == 0) {
            throw IllegalStateException("Moves have to be taken first!")
        } else {
            game.currentState--
        }
        onAllRefreshables { refreshAfterUndo() }
    }


    /**
     * method to redo an undone move
     *
     * checking first, if nextState is null. should be only then not null if a move was undone.
     * in case nextState is null an exception is thrown. otherwise currentState is  incremented
     *      and we would 'go foward'
     */
    fun redo() {
        val rs = rootService
        val game = rs.currentGame
        val nextState = game.currentState + 1
        val nextStateExists = nextState < game.currentGameState.size
        if (nextStateExists) {
            game.currentState++
        } else {
            throw IllegalStateException("No Moves to redo")
        }
        onAllRefreshables { refreshAfterRedo() }
    }
}