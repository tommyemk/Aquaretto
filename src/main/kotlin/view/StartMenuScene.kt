package view

import entity.PlayerType
import service.RootService
import tools.aqua.bgw.components.uicomponents.Button
import tools.aqua.bgw.components.uicomponents.CheckBox
import tools.aqua.bgw.components.uicomponents.Label
import tools.aqua.bgw.components.uicomponents.TextField
import tools.aqua.bgw.core.MenuScene
import tools.aqua.bgw.style.*
import tools.aqua.bgw.util.Font
import tools.aqua.bgw.visual.ColorVisual

/**
 * The start menu of the game app which will be displayed directly at program start.
 * A user can choose between starting a new game, joining an online game, or resuming a saved game.
 */
class StartMenuScene(private val rootService: RootService) : MenuScene(680, 1080), Refreshable {

    private val aquaretto = Label(
        posX = 220, posY = 40,
        width = 240,
        text = "Aquaretto",
        font = Font(size = 36, color = DARK, family = "Calibri", fontWeight = Font.FontWeight.BOLD)
    )

    private val new = RegularButton(
        posX = 240, posY = 125,
        width = 200,
        text = "New Game"
    ).apply {
        onMouseClicked = {
            showNewGameOptions()
            showJoinGameOptions(false)
        }
    }

    val local = Button(
        posX = 260, posY = 195,
        width = 70, height = 25,
        visual = ColorVisual(LIGHT),
        text = "local",
        font = Font(color = DARK, family = "Calibri")
    ).apply { isVisible = false } //GameApplication will config some stuff onMouseClicked

    val online = Button(
        posX = 350, posY = 195,
        width = 70, height = 25,
        visual = ColorVisual(LIGHT),
        text = "online",
        font = Font(color = DARK, family = "Calibri")
    ).apply { isVisible = false } //GameApplication will config some stuff onMouseClicked

    private val join = RegularButton(
        posX = 240, posY = 240,
        width = 200,
        text = "Join Game"
    ).apply {
        onMouseClicked = {
            showJoinGameOptions()
            showNewGameOptions(false)
        }
    }

    private val playerLabel = Label(
        posX = 200, posY = 290,
        width = 80,
        text = "Name:",
        font = Font(color = DARK, size = 16, family = "Calibri")
    ).apply { isVisible = false }

    private val player = TextField(
        posX = 200, posY = 315,
        width = 80,
        prompt = "Player", // Prompt buggy in bgw 0.9 :c
        text = ""
        //font = Font(color = NEUTRAL)
    ).apply {
        isVisible = false
    }

    private val aiBox = CheckBox(
        posX = 290, posY = 315
    ).apply { isVisible = false }

    private val aiLabel = Label(
        posX = 280, posY = 290,
        width = 40,
        text = "AI",
        font = Font(color = DARK, size = 16, family = "Calibri")
    ).apply { isVisible = false }

    private val codeLabel = Label(
        posX = 320, posY = 290,
        width = 80,
        text = "Code:",
        font = Font(color = DARK, size = 16, family = "Calibri")
    ).apply {
        isVisible = false
    }

    private val invCode = TextField(
        posX = 320, posY = 315,
        width = 80,
        prompt = "Invite Code", // Prompt buggy in bgw 0.9 :c
        //font = Font(color = NEUTRAL)
    ).apply {
        isVisible = false
    }

    private val startJoined = Button(
        posX = 410, posY = 315,
        width = 80,
        height = 30,
        font = Font(size=16, color=DARK, family = FAMILY, fontStyle = Font.FontStyle.ITALIC),
        visual = ColorVisual(LIGHT).apply {
            borderRadius = BorderRadius(5)
            backgroundRadius = BackgroundRadius(100)
        },
        text = "Join"
    ).apply {
        isVisible = false
        onMouseClicked = {
            rootService.networkService.joinGame(
                player.text,
                if (aiBox.isChecked) PlayerType.LOCAL_AI else PlayerType.LOCAL_HUMAN,
                invCode.text
            )
        }

    }

    val exit = Button(
        posX = 240, posY = 900,
        width = 200, height = 50,
        visual = ColorVisual(NEUTRAL).apply { opacity = 0.5 },
        text = "Exit",
        font = Font(size = 24, color = DARK, family = "Calibri", fontStyle = Font.FontStyle.ITALIC),
    )

    val resumeGame = RegularButton(
        posX = 240, posY = 360,
        width = 200,
        text = "Load Game"
    )

    init {
        background = ColorVisual(BLUE)
        opacity = 0.6
        addComponents(
            aquaretto,
            new, join,
            local, online,
            player, playerLabel, aiBox, aiLabel, invCode, codeLabel, startJoined,
            exit, resumeGame
        )
    }

    /**
     * A method to reset the start menu, meaning certain options will be hidden at first
     */
    fun reset() {
        showNewGameOptions(false)
        showNewGameOptions(false)
        invCode.text = "Invite Code"
    }

    /**
     * Helper functions to bundle operations on similar objects
     */

    private fun showNewGameOptions(e: Boolean = true) {
        local.isVisible = e
        online.isVisible = e
    }

    private fun showJoinGameOptions(e: Boolean = true) {

        player.isVisible = e
        aiBox.isVisible = e
        aiLabel.isVisible = e
        invCode.isVisible = e
        codeLabel.isVisible = e
        playerLabel.isVisible = e
        startJoined.isVisible = e
    }
}