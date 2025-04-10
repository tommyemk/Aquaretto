package service

import entity.*

import entity.AquarettoGameState
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString
import java.io.File

private val json1 = Json { allowStructuredMapKeys = true }

/**
 * A service class for managing the Aquaretto game state and interactions.
 * This class provides functionalities to manipulate and retrieve the game state,
 * facilitating operations like player actions, game state updates, and more.
 *
 * @property rootService An instance of RootService to access and modify the game's root state.
 */


class AquarettoGameService(private val rootService: RootService) : AbstractRefreshingService() {

    /**
     * Starts the AquarettoGame local or online.
     *
     * @param players the player names mapped to their player type
     * @param isOnline whether the player plays online or not
     * @param shuffle whether the player order is shuffled or not
     * @param speed how fast the KI animations are taking place
     * @param onlineMainPile the mainPile if the player is playing online
     * @param onlineFinalPile the finalPile if the player is playing online
     */
    fun startGame(
        players: List<Pair<String, PlayerType>>,
        isOnline: Boolean,
        shuffle: Boolean,
        speed: Int,
        onlineMainPile: List<Tile>? = null,
        onlineFinalPile: List<Tile>? = null
    ) {
        val game = rootService.currentGame
        game.currentGameState.clear()
        game.currentState = 0
        game.currentGameState.add(AquarettoGameState())
        val currentGameState = game.currentGameState[game.currentState]

        var shuffledPlayers = players
        // The player order can be shuffled in the game settings menu
        if (shuffle) {shuffledPlayers = players.shuffled()}
        // Player objects are getting created
        for (pair in shuffledPlayers) {
            currentGameState.players.add(Player(pair.first, 0, pair.second))
        }

        // play local or host online game
        if (onlineMainPile == null) {
            initializeBoard(shuffledPlayers.size)

            if (isOnline) {
                rootService.networkService.startHostedGame()
            }
        } else {
            // play online by joining another game
            currentGameState.board.mainPile = onlineMainPile.toMutableList()
            currentGameState.board.finalPile = onlineFinalPile!!.toMutableList()
            currentGameState.board.offspring = TileLoader.createOffspringList().toMutableList()

            initializeTransporters(shuffledPlayers.size)
        }
        game.speed = speed
        game.isOnlineGame = isOnline

        onAllRefreshables { refreshAfterStartGame() }
    }

    /**
     * Initializes the game board with all it's tiles.
     *
     * @param playerCount the amount of players which are playing the game
     */
    private fun initializeBoard(playerCount: Int) {
        val game = rootService.currentGame
        val currentGameState = game.currentGameState[game.currentState]

        // This is the representation of the mainPile from the board
        var animalTileList = mutableListOf<Tile>()
        // This is the representation of the offSpringPile from the board
        var offSpringList = listOf<Tile>()

        when (playerCount) {
            2 -> {
                val animalRemove1 = Animal.POLAR_BEAR
                val animalRemove2 = Animal.CROCODILE
                val animalRemove3 = Animal.PENGUIN

                offSpringList = TileLoader.createOffspringList()
                animalTileList = TileLoader.createDepotList().toMutableList()

                animalTileList.removeAll {
                    it is AnimalTile && (
                            it.animalType == animalRemove1 ||
                                    it.animalType == animalRemove2 ||
                                    it.animalType == animalRemove3
                            )
                }

                initializeTransporters(playerCount)
            }

            3 -> {
                val animalRemove1 = Animal.SEA_TURTLE
                val animalRemove2 = Animal.PENGUIN
                offSpringList = TileLoader.createOffspringList()
                animalTileList = TileLoader.createDepotList().toMutableList()
                animalTileList.removeAll {
                    it is AnimalTile && (it.animalType == animalRemove1 || it.animalType == animalRemove2)
                }

                initializeTransporters(playerCount)
            }

            4 -> {
                val animalRemove1 = Animal.HIPPO
                offSpringList = TileLoader.createOffspringList()
                animalTileList = TileLoader.createDepotList().toMutableList()
                animalTileList.removeAll {
                    it is AnimalTile && (it.animalType == animalRemove1)
                }

                initializeTransporters(playerCount)
            }

            5 -> {
                // No animal tiles has to be removed
                offSpringList = TileLoader.createOffspringList()
                animalTileList = TileLoader.createDepotList().toMutableList()

                initializeTransporters(playerCount)
            }
        }

        // The mainPile and the finalPile need to be shuffled before the game starts
        currentGameState.board.mainPile = animalTileList.shuffled().toMutableList()
        currentGameState.board.offspring = offSpringList.map { it as AnimalTile }.toMutableList()
        repeat(15) {
            currentGameState.board.finalPile.add(currentGameState.board.mainPile.removeFirst())
        }
    }

    /**
     * Initializes the transporter list from the board with the
     * right amount of transporters based on the amount of players
     * which are playing the game.
     *
     * @param playerCount the amount of players which are playing the game
     */
    private fun initializeTransporters(playerCount: Int) {
        val game = rootService.currentGame
        val currentGameState = game.currentGameState[game.currentState]

        // Creates the transporters
        when (playerCount) {

            // 2 Player
            2 -> {
                repeat(3) {
                    currentGameState.board.transporters.add(Transporter(it + 1))
                }
            }

            // 3 Player
            3 -> {
                repeat(3) {
                    currentGameState.board.transporters.add(Transporter())
                }
            }

            // 4 Player
            4 -> {
                repeat(4) {
                    currentGameState.board.transporters.add(Transporter())
                }
            }

            // 5 Player
            5 -> {
                repeat(5) {
                    currentGameState.board.transporters.add(Transporter())
                }
            }
        }
    }

    /**
     * Function to determine and set the next player (who didn't end their turn) to play a turn.
     * If all players have ended their turn (e.g. taken a transporter),
     * the function resets their endTurn-flags and the transporters for the next round.
     * In that case, the next player is the one who ended their turn last.
     */
    fun nextPlayer() {
        val game = rootService.currentGame
        val gameState = game.currentGameState[game.currentState]

        require(gameState.players.size > 0)

        // lastPlayer = player who just did an action
        val lastPlayer = gameState.currentPlayer

        if (gameState.players[lastPlayer].offspringsToPlace.values.sum() != 0 ||
            gameState.players[lastPlayer].coworkersToPlace != 0
        ) {
            // The player hasn't handled their special situation yet
            return
        }

        var currentPlayer = (lastPlayer + 1) % gameState.players.size

        // game chooses the next player who didn't end their turn through a loop
        while (gameState.players[currentPlayer].turnEnded) {
            if (currentPlayer == lastPlayer) {
                // we iterated through the entire player list and got to the lastPlayer again
                // also, everyone's turn, including the lastPlayer's, ended, so new round
                gameState.players.forEach { it.turnEnded = false }
                gameState.board.transporters.forEach {
                    it.taken = false
                    it.tiles.clear()
                }

                if (gameState.finalRound) {
                    endGame()
                } else {
                    // The round is over when every player has taken the transporter
                    onAllRefreshables { refreshAfterRoundEnds() }
                }
                break
            }

            currentPlayer = (currentPlayer + 1) % gameState.players.size
        }
        gameState.currentPlayer = currentPlayer

        //check if new state exists
        if (game.currentGameState.size - 2 == game.currentState) {
            game.currentState++
        }

        onAllRefreshables { refreshAfterNextPlayer() }
        if (game.isOnlineGame) {
            rootService.networkService.changeTurnState()
        }
    }

    /**
     * Saves the current [AquarettoGameState] to a file in the JSON-Format. The file using the [file]
     * parameter. If a file with the same name already exists, it will be overwritten.
     *
     * @param file The path to be saves as JSON
     */
    fun saveGame(file: File) {
        val game = rootService.currentGame
        val gameState = game.currentGameState[game.currentState]

        // Conversion to JSON
        val json = json1.encodeToString(gameState)
        //Write to file
        file.writeText(json)

        onAllRefreshables { refreshAfterSaveGame() }
    }

    /**
     * Loads an already saved game (as [AquarettoGameState]) by converting the JSON-data to
     * an [AquarettoGameState]-object. Also resets the state list.
     *
     * @param file The file to read from ad JSON
     */
    fun loadGame(file: File) {
        val game = rootService.currentGame

        // read from file
        val json = file.readText()
        // conversion to AquarettoGameState-object
        val gameState = json1.decodeFromString<AquarettoGameState>(json)

        // reset state-list (because we only saved the last gameState, so make sure it will be
        // the first and only one once we reload, may be subject to change)
        game.currentState = 0
        game.currentGameState.clear()
        game.currentGameState.add(gameState)

        onAllRefreshables { refreshAfterLoadGame() }
    }

    /**
     * Calculates the points for each player in the current game.
     *
     * @return A list of pairs where each pair consists of a player and their corresponding points.
     */
    fun calcPoints(): List<Pair<Player, Int>> {
        val playersScores = mutableListOf<Pair<Player, Int>>()
        val game = rootService.currentGame
        val players = game.currentGameState[game.currentState].players
        for (player in players) {
            val playerScore = calcPlayerPoints(player)

            val playerWithScore = Pair(player, playerScore)

            playersScores.add(playerWithScore)

        }
        return playersScores
    }

    /**
     * Calculates the points for a specific player.
     *
     * @param player The player for whom to calculate the points.
     * @return The total points earned by the player.
     */
    private fun calcPlayerPoints(player: Player): Int {
        var score = 0
        var keeperCount = 0
        var hasFischCount = 0
        var cashierCount = 0
        var mangerFound = false
        var trainableAnimal = 0

        val fieldMap = player.waterPark.fieldMap
        for (coordinatePlaceable in fieldMap) {
            if (coordinatePlaceable.value is AnimalTile) {
                val animal = coordinatePlaceable.value as AnimalTile
                if (animal.hasFish) {
                    hasFischCount += 1
                }
                score += 1
            } else if (coordinatePlaceable.value is Coworker) {
                val coworker = coordinatePlaceable.value as Coworker
                if (coworker.coworkerTask == CoworkerTask.ANIMAL_KEEPER) {
                    keeperCount += 1
                } else if (coworker.coworkerTask == CoworkerTask.TRAINER) {
                    val directions = listOf<Pair<Int, Int>>(
                        Pair(-1, 0), //left
                        Pair(1, 0), //right
                        Pair(0, 1), //down
                        Pair(0, -1), //top
                        Pair(-1, -1), //top-left
                        Pair(1, -1), //top right
                        Pair(-1, 1), //down left
                        Pair(1, 1), //down right
                    )

                    for (direction in directions) {
                        val aroundTrainerDirection: Pair<Int, Int> =
                            Pair(
                                coordinatePlaceable.key.first + direction.first,
                                coordinatePlaceable.key.second + direction.second
                            )

                        val tile: Placeable? = fieldMap[aroundTrainerDirection]
                        if (tile is AnimalTile && tile.isTrainable) {
                            trainableAnimal++
                        }
                    }
                } else if (coworker.coworkerTask == CoworkerTask.CASHIER) {
                    cashierCount += 1

                } else if (coworker.coworkerTask == CoworkerTask.MANAGER) {
                    mangerFound = true

                }
            }

        }
        val depotAnimalType = player.depot
        val gruppDepot: Map<Animal, List<Tile>> = depotAnimalType.groupBy {

            (it as AnimalTile).animalType
        }
        var gruppDepotSize = gruppDepot.size * -2


        if (mangerFound) {
            gruppDepotSize /= 2

        }
        val coins = player.numCoins * cashierCount
        score += (keeperCount * hasFischCount) + coins + gruppDepotSize + trainableAnimal
        return score
    }

    /**
     * Ends the game if it meets the specified conditions.
     *
     * If it's the final round and the current player is the first player, triggers the end of the game.
     */
    fun endGame() {
        onAllRefreshables { refreshAfterEndGame(calcPoints()) }
    }
}