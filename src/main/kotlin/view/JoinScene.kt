package view

import service.RootService
import tools.aqua.bgw.components.uicomponents.Button
import tools.aqua.bgw.components.uicomponents.Label
import tools.aqua.bgw.core.MenuScene
import tools.aqua.bgw.util.Font
import tools.aqua.bgw.visual.ColorVisual

/**
 *
 * class for the join scene. not very large
 */
class JoinScene : MenuScene(680, 1080), Refreshable {

    private val waitLabel = Label(
        posX = 120, posY = 40,
        width = 400,
        text = "Waiting for host to start game...",
        font = Font(size = 36, color = DARK, family = "Calibri", fontWeight = Font.FontWeight.BOLD)
    )

    private val playerList = List(5) {
        Label(
            posX = 120, posY = 140+it*50,
            width = 400,
            font = Font(size=22, color = DARK, family = "Calibri", fontWeight = Font.FontWeight.BOLD)
        )
    }

    val leaveButton = Button(
        posX = 215, posY = 750,
        width = 250, height = 50,
        visual = ColorVisual(LIGHT).apply { opacity = 0.5 },
        text = "Leave Game",
        font = Font(size=24, color=ACCENT, family = "Calibri",
            fontStyle = Font.FontStyle.ITALIC, fontWeight = Font.FontWeight.BOLD)
    )

    init {
        background = ColorVisual(BLUE)
        opacity = 0.6
        addComponents(
            waitLabel, leaveButton
        )
        playerList.forEach { addComponents(it) }
    }

    private fun updateNames(playerNames: List<String>) {
        playerList.forEachIndexed { i, it ->
            if (i in playerNames.indices) {
                it.text = playerNames[i]
            } else {
                it.text = ""
            }
        }
    }

    override fun refreshAfterJoinGame(playerNames: List<String>) {
        updateNames(playerNames)
    }

    override fun refreshAfterPlayerJoin(playerNames: List<String>) {
        updateNames(playerNames)
    }

    override fun refreshAfterPlayerLeave(playerNames: List<String>) {
        updateNames(playerNames)
    }
}