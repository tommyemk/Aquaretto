package service


import entity.AquarettoGameState
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

/**
 * testing undo and redo method from PlayerActionService
 *
 * doing it in one test for both methods
 */
class ActionTestUndoRedo {

    /**
     * testing undo and redo method in one test
     *
     * first setting a game and adding two states
     * setting currentGameState to 1
     * call undo() method and checking if currentState went back like it should
     * call redo() method and checking in currentState went forward like it should
     *
     * apparently player is not necessarily needed for this test lol
     */
    @Test
    fun undoRedoTest(){
        val rs = RootService()
        val game1 = rs.currentGame
        /*val player1 = Player(
            "p1",
            2,
            PlayerType.LOCAL_HUMAN
        )*/
        val state1 = AquarettoGameState()
        //state1.players.add(player1)
        val state2 = AquarettoGameState()
        //state2.players.add(player1)
        val state3 = AquarettoGameState()
        game1.currentGameState.add(state1)
        game1.currentGameState.add(state2)
        game1.currentGameState.add(state3)
        game1.currentState = 1


        rs.playerActionService.redo()
        //check if currentState went one forward
        assertEquals(2,game1.currentState)

        rs.playerActionService.undo()
        //check if currentState went backward one
        assertEquals(1, game1.currentState)

    }


}