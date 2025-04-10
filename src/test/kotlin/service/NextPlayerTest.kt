package service

import entity.*
import kotlin.test.*

/**
 * Tests for the nextPlayer-function of [AquarettoGameService]
 */
class NextPlayerTest {
    private val aquarettoGame3Players = AquarettoGame()

    private val rootService0Players = RootService()
    private var rootService3Players: RootService? = null


    /**
     * Setup for the tests (adding a [AquarettoGameState] for each [RootService])
     */
    @BeforeTest
    fun setUpTests(){
        aquarettoGame3Players.currentGameState.add(
            AquarettoGameState(
                mutableListOf(
                    Player("Marcel", 4, PlayerType.LOCAL_HUMAN),
                    Player("R2D2", 4, PlayerType.LOCAL_AI),
                    Player("C3PO", 4, PlayerType.LOCAL_RANDOM_AI)
                )
            )
        )
        aquarettoGame3Players.currentGameState[aquarettoGame3Players.currentState].board.transporters.addAll(
            listOf(Transporter(), Transporter(), Transporter())
        )
        rootService3Players = RootService(aquarettoGame3Players)
    }

    /**
     * In case there are no players in the game, nextPlayer fails because there is no
     * possible next player.
     */
    @Test
    fun testNextPlayerFor0Players(){
        assertFails { rootService0Players.aquarettoGameService.nextPlayer() }
    }

    /**
     * Test for a game with 3 players where nobody ends their turn (e.g. nobody takes
     * a transporter), so nextPlayer() is will just choose the next player in the list (or
     * the first player if the list's end has been reached)
     */
    @Test
    fun testNextPlayerFor3PlayersNoTurnEnds(){
        val game = rootService3Players!!.currentGame
        val gameState = game.currentGameState[game.currentState]

        assertTrue { gameState.currentPlayer == 0}

        rootService3Players!!.aquarettoGameService.nextPlayer()
        assertTrue { gameState.currentPlayer == 1}

        rootService3Players!!.aquarettoGameService.nextPlayer()
        assertTrue { gameState.currentPlayer == 2}

        rootService3Players!!.aquarettoGameService.nextPlayer()
        assertTrue { gameState.currentPlayer == 0}
    }

    /**
     * Test for 3 player game. This time, the players can end their turn, which means that
     * those who ended their turn need to be skipped by nextPlayer().
     */
    @Test
    fun testNextPlayerFor3PlayersTurnEnds(){
        val game = rootService3Players!!.currentGame
        val gameState = game.currentGameState[game.currentState]

        assertTrue { gameState.currentPlayer == 0}

        rootService3Players!!.aquarettoGameService.nextPlayer()
        assertTrue { gameState.currentPlayer == 1}
        gameState.players[gameState.currentPlayer].turnEnded = true
        gameState.board.transporters[0].taken = true

        rootService3Players!!.aquarettoGameService.nextPlayer()
        assertTrue { gameState.currentPlayer == 2}

        rootService3Players!!.aquarettoGameService.nextPlayer()
        assertTrue { gameState.currentPlayer == 0}

        rootService3Players!!.aquarettoGameService.nextPlayer()
        // player with index 1 was skipped
        assertTrue { gameState.currentPlayer == 2}
    }

    /**
     * Test for 3 player game. This time, everyone ends their turn to test the
     * "round-reset".
     */
    @Test
    fun testNextPlayerFor3PlayersAllEndTurn1(){
        val game = rootService3Players!!.currentGame
        val gameState = game.currentGameState[game.currentState]

        assertTrue { gameState.currentPlayer == 0}
        gameState.players[gameState.currentPlayer].turnEnded = true
        gameState.board.transporters[0].taken = true

        rootService3Players!!.aquarettoGameService.nextPlayer()
        assertTrue { gameState.currentPlayer == 1}
        gameState.players[gameState.currentPlayer].turnEnded = true
        gameState.board.transporters[1].taken = true

        rootService3Players!!.aquarettoGameService.nextPlayer()
        assertTrue { gameState.currentPlayer == 2}
        gameState.players[gameState.currentPlayer].turnEnded = true
        gameState.board.transporters[2].taken = true

        rootService3Players!!.aquarettoGameService.nextPlayer()

        // new Round, because everyone ended their turn, transporters get reset
        // the last one to end their turn (2) will be the first player in the new round
        assertTrue { gameState.currentPlayer == 2}

        gameState.players.forEach{ assertFalse { it.turnEnded } }
        gameState.board.transporters.forEach{ assertFalse { it.taken } }
    }

    /**
     * Another test for 3 player game where everyone ends their turn, just in a different order.
     */
    @Test
    fun testNextPlayerFor3PlayersAllEndTurn2(){
        val game = rootService3Players!!.currentGame
        val gameState = game.currentGameState[game.currentState]

        assertTrue { gameState.currentPlayer == 0 }
        gameState.players[gameState.currentPlayer].turnEnded = true
        gameState.board.transporters[0].taken = true

        rootService3Players!!.aquarettoGameService.nextPlayer()
        assertTrue { gameState.currentPlayer == 1}

        rootService3Players!!.aquarettoGameService.nextPlayer()
        assertTrue { gameState.currentPlayer == 2}
        gameState.players[gameState.currentPlayer].turnEnded = true
        gameState.board.transporters[1].taken = true

        rootService3Players!!.aquarettoGameService.nextPlayer()
        assertTrue { gameState.currentPlayer == 1}
        gameState.players[gameState.currentPlayer].turnEnded = true
        gameState.board.transporters[2].taken = true

        //new round
        rootService3Players!!.aquarettoGameService.nextPlayer()

        assertTrue { gameState.currentPlayer == 1}

        gameState.players.forEach{ assertFalse { it.turnEnded } }
        gameState.board.transporters.forEach{ assertFalse { it.taken } }
    }
}