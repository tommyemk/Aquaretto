package service.AI

import entity.*
import service.AbstractRefreshingService
import service.RootService
import kotlin.random.Random

/**
 * Service for the A.I actions
 * @param rootService RootService
 * @constructor Initializes the A.I action service
 * @property rootService RootService
 */
class AIActionService(private val rootService: RootService) : AbstractRefreshingService() {

    var waterparkExtisPlaced1 = false
    var waterparkExtisPlaced2 = false
    var waterparkExtisPlaced3 = false
    var waterparkExtisPlaced4 = false

    /**
     * Evaluate the A.I move
     * @param player Player
     */
    fun evaluateAIMove(player: Player) {
        when (player.playerType) {
            PlayerType.LOCAL_AI -> smartMove()
            PlayerType.LOCAL_RANDOM_AI -> randomMove()
            else -> return
        }
    }

    private fun smartMove() {
        randomMove()
    }

    /**
     * Make a random move for the A.I
     */
    private fun randomMove() {
        val randomIndexBetween = Random.nextInt(0, 5)
        selectMove(randomIndexBetween)
    }

    /**
     * Select a move for the A.I
     * @param index Int with the index of the chosen move
     */
    private fun selectMove(index: Int) {
        val game = rootService.currentGame
        val currentGameState = game.currentGameState[game.currentState]
        val player = currentGameState.players[currentGameState.currentPlayer]
        val randomTransporter = Random.nextInt(0, currentGameState.board.transporters.size - 1)
        return when (index) {
            0 -> {
                try {
                    rootService.playerActionService.actionA(randomTransporter)
                } catch (e: Exception) {
                    println(e.message + "choose a different action")
                    val index1 = correctTransporter()
                    rootService.playerActionService.actionB(index1, placeTilesFromTransporterOnRandomPos(index1))
                }
            }

            1 -> {
                try {
                    val index0 = correctTransporter()
                    rootService.playerActionService.actionB(index0, placeTilesFromTransporterOnRandomPos(index0))
                } catch (e: Exception) {
                    println(e.message + "choose a different action")
                }
            }

            2 -> {
                try {
                    val randomPlayer = getRandomPlayer()
                    val randomPos = randomPosFromOtherPlayer(randomPlayer)
                    rootService.playerActionService.actionCPurchase(randomPlayer, randomPos)

                } catch (e: Exception) {
                    println(e.message + "choose a different action")
                    val index2 = correctTransporter()
                    rootService.playerActionService.actionB(index2, placeTilesFromTransporterOnRandomPos(index2))
                }
            }

            3 -> {
                try {
                    rootService.playerActionService.actionCDiscardTile()
                } catch (e: Exception) {
                    println(e.message + "choose a different action")
                    val index3 = correctTransporter()
                    rootService.playerActionService.actionB(index3, placeTilesFromTransporterOnRandomPos(index3))
                }
            }

            4 -> {
                try {
                    val randomIndex = choseBetweenCMoves()
                    when (randomIndex) {
                        0 -> {
                            val randomAnimalPos = cMoveRandomAnimalFromDepot()
                            val animalFromDepot = player.depot.first() as AnimalTile
                            rootService.playerActionService.actionCMove(animalFromDepot, randomAnimalPos)
                        }

                        1 -> {
                            val randomCoworkerPos = cMoveRandomCoworker()
                            rootService.playerActionService.actionCMove(getRandomCoworker(), randomCoworkerPos)
                        }

                        else -> {
                            println("No Coworker or Animal in Depot")
                            val index4 = correctTransporter()
                            rootService.playerActionService.actionB(
                                index4,
                                placeTilesFromTransporterOnRandomPos(index4)
                            )
                        }
                    }
                } catch (e: Exception) {
                    println(e.message + "choose a different action")
                    val index4 = correctTransporter()
                    rootService.playerActionService.actionB(index4, placeTilesFromTransporterOnRandomPos(index4))
                }
            }

            5 -> {
                try {
                    if (player.numCoins >= 1) {

                        val waterparkExt = choseRandomWaterParkExtension()
                        rootService.playerActionService.actionCExtendWaterPark(waterparkExt)
                    } else {
                        throw IllegalStateException("Not enough coins")
                    }
                } catch (e: Exception) {
                    println(e.message + "choose a different action")
                    val index5 = correctTransporter()
                    rootService.playerActionService.actionB(index5, placeTilesFromTransporterOnRandomPos(index5))
                }
            }

            else -> {

                throw IllegalStateException("Invalid move for A.I")
            }
        }
    }

    /**
     * Get the index of the first transporter that is not taken
     * @return Int with the index of the first transporter that is not taken
     */
    private fun correctTransporter(): Int {
        val game = rootService.currentGame
        val currentGameState = game.currentGameState[game.currentState]
        for (transporter in currentGameState.board.transporters) {
            if (!transporter.taken && transporter.tiles.size > 0)
                return currentGameState.board.transporters.indexOf(transporter)
        }
        return -1
    }

    /**
     * Place the tiles from a transporter on random positions
     * @param transporterIndex Index of the transporter
     * @return Map<Tile, Pair<Int, Int>> with the placed tiles and their coordinatess
     */
    private fun placeTilesFromTransporterOnRandomPos(transporterIndex: Int): Map<Tile, Pair<Int, Int>> {
        val game = rootService.currentGame
        val currentGameState = game.currentGameState[game.currentState]
        val player = currentGameState.players[currentGameState.currentPlayer]

        val transporter = currentGameState.board.transporters[transporterIndex]
        val outputMap = mutableMapOf<Tile, Pair<Int, Int>>()

        for (tile in transporter.tiles) {
            if (tile is AnimalTile) {
                val differentAnimalTyped =
                    player.waterPark.fieldMap.values.filterIsInstance<AnimalTile>().groupBy { it.animalType }

                if (tile.animalType in differentAnimalTyped) {
                    outputMap[tile] = placeTileRandom(tile)
                } else {
                    if (differentAnimalTyped.size >= player.waterPark.allowedAnimalType) {
                        outputMap[tile] = Waterpark.DEPOT_POS
                    } else {
                        outputMap[tile] = placeTileRandom(tile)
                    }
                }
            }
        }
        return outputMap
    }

    /**
     * Place a random tile from the depot
     * @param animalTile AnimalTile from the depot
     * @return Pair<Int, Int> with the x and y coordinates of the placed tile
     */
    fun placeTileRandom(animalTile: AnimalTile): Pair<Int, Int> {
        val game = rootService.currentGame
        val currentGameState = game.currentGameState[game.currentState]
        val player = currentGameState.players[currentGameState.currentPlayer]
        val waterPark = player.waterPark

        // alle platzierbaren Felder ohne die bereits besetzten Felder
        var randomTileCoordinates = waterPark.allowedExtensionList.filter { it !in player.waterPark.fieldMap }

        val coworkerPlaces = listOf(
            Waterpark.CASH_POS_1,
            Waterpark.CASH_POS_2,
            Waterpark.ANIM_POS_1,
            Waterpark.ANIM_POS_2
        )
        // entferne dann die Coworker-Felder
        randomTileCoordinates = randomTileCoordinates.filter { it !in coworkerPlaces }

        // weitere Felder entfernen, auf denen das Tile nicht platziert werden darf (Nachbar von anderen
        // AnimalTypes etc.)
        randomTileCoordinates = randomTileCoordinates.filter {
            val x = it.first
            val y = it.second
            val listOfNeighbours = listOf(Pair(x + 1, y), Pair(x - 1, y), Pair(x, y + 1), Pair(x, y - 1))

            var isPlaceable = true
            var hasNeighbour = false

            for (neighbour in listOfNeighbours) {
                val neighbourTile = waterPark.fieldMap[neighbour]

                // nicht neben anderes Tier legen
                if (neighbourTile is AnimalTile && neighbourTile.animalType != animalTile.animalType) {
                    isPlaceable = false
                }

                if (neighbourTile is AnimalTile) {
                    hasNeighbour = true
                }
            }

            // nicht gleiches Tier weit weg platzieren
            var fieldContainsAnimal = false
            for (aniTile in waterPark.fieldMap.values) {
                if (aniTile is AnimalTile && aniTile.animalType == animalTile.animalType) {
                    fieldContainsAnimal = true
                }
            }

            if (fieldContainsAnimal && !hasNeighbour) {
                isPlaceable = false
            }
            isPlaceable
        }

        if (randomTileCoordinates.isEmpty()) {
            return Waterpark.DEPOT_POS
        }
        return randomTileCoordinates[Random.nextInt(0, randomTileCoordinates.size - 1)]
    }

    /**
     * Place a random tile from the depot
     * @return Pair<Int, Int> with the x and y coordinates of the placed tile
     */
    private fun cMoveRandomAnimalFromDepot(): Pair<Int, Int> {
        val game = rootService.currentGame
        val currentGameState = game.currentGameState[game.currentState]
        val player = currentGameState.players[currentGameState.currentPlayer]
        val animal = player.depot.first() as AnimalTile
        return placeTileRandom(animal)
    }

    /**
     * Place a random coworker from the depot
     * @return Pair<Int, Int> with the x and y coordinates of the placed coworker
     */
    private fun cMoveRandomCoworker(): Pair<Int, Int> {
        val game = rootService.currentGame
        val currentGameState = game.currentGameState[game.currentState]
        val player = currentGameState.players[currentGameState.currentPlayer]
        val waterPark = player.waterPark

        // alle platzierbaren Felder ohne die bereits besetzten Felder
        val randomTileCoordinates = waterPark.allowedExtensionList.filter { it !in player.waterPark.fieldMap }

        if (randomTileCoordinates.isEmpty()) {
            throw IllegalStateException("No placeable field for Coworker")
        }

        return randomTileCoordinates[Random.nextInt(0, randomTileCoordinates.size - 1)]
    }

    /**
     * Get a random coworker from the waterpark
     * @return Coworker from the waterpark of the current player
     */
    private fun getRandomCoworker(): Coworker {
        val game = rootService.currentGame
        val currentGameState = game.currentGameState[game.currentState]
        val player = currentGameState.players[currentGameState.currentPlayer]
        val waterPark = player.waterPark

        var listOfCoworker = waterPark.fieldMap.filter { it.value is Coworker }.values.toList()

        //cast to Coworker
        listOfCoworker = listOfCoworker.map { it as Coworker }

        return listOfCoworker[Random.nextInt(0, listOfCoworker.size - 1)]
    }

    /**
     * Chose between the two possible C-Moves
     * @return Int with the index of the chosen move
     */
    private fun choseBetweenCMoves(): Int {
        val game = rootService.currentGame
        val currentGameState = game.currentGameState[game.currentState]
        val player = currentGameState.players[currentGameState.currentPlayer]

        return if (player.numberCoworker > 0 && player.depot.size > 0) {
            Random.nextInt(0, 1)
        } else if (player.numberCoworker > 0) {
            1
        } else if (player.depot.size > 0) {
            0
        } else {
            -1
        }
    }

    /**
     * Get a random player from the current game state
     * @return Player from the current game state
     */
    private fun getRandomPlayer(): Player {
        val game = rootService.currentGame
        val currentGameState = game.currentGameState[game.currentState]
        val players = currentGameState.players.filter { it != currentGameState.players[currentGameState.currentPlayer] }
        return players[Random.nextInt(0, players.size - 1)]
    }

    /**
     * Place a random tile from the depot of another player
     * @param player Player from which the tile should be taken
     * @return Pair<Int, Int> with the x and y coordinates of the placed tile
     */
    private fun randomPosFromOtherPlayer(player: Player): Pair<Int, Int> {
        val topAnimalTileFromDepot = player.depot.removeFirst() as AnimalTile

        return placeTileRandom(topAnimalTileFromDepot)
    }

    /**
     * Chose a random WaterparkExtension from the current player
     * @return WaterparkExtension from the current player
     */
    private fun choseRandomWaterParkExtension(): WaterparkExtension {
        val game = rootService.currentGame
        val currentGameState = game.currentGameState[game.currentState]
        val player = currentGameState.players[currentGameState.currentPlayer]

        val waterparkExt1 = player.waterParkExtensionList[0]
        val waterparkExt2 = player.waterParkExtensionList[1]
        val waterparkExt3 = player.waterParkExtensionList[2]
        val waterparkExt4 = player.waterParkExtensionList[3]

        if (!waterparkExtisPlaced1) {
            waterparkExt1.rotation = 3
            waterparkExt1.x = 13
            waterparkExt1.y = 10
            waterparkExtisPlaced1 = true
            return waterparkExt1
        } else if (!waterparkExtisPlaced2) {
            waterparkExt2.rotation = 2
            waterparkExt2.x = 8
            waterparkExt2.y = 6
            waterparkExtisPlaced2 = true
            return waterparkExt2
        } else if (!waterparkExtisPlaced3) {
            waterparkExt3.x = 6
            waterparkExt3.y = 9
            waterparkExtisPlaced3 = true
            return waterparkExt3
        } else {
            if (!waterparkExtisPlaced4) {
                waterparkExt4.x = 10
                waterparkExt4.y = 13
                waterparkExtisPlaced4 = true
                return waterparkExt4
            }
        }
        return waterparkExt4

    }

}